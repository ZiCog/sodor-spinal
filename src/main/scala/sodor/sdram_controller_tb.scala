package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._


// Create the test bench top level and instantiate the SD RAM controller
class sdram_controller_tb extends Component{
  val io = new Bundle {
    //////////// Clock and reset ///
    val CLOCK_50 = in Bool
    val CLOCK_100 = out Bool
    val rst_n = out Bool

    //////////// Leds //////////////
    val LED = out Bits (8 bit)

    //////////// Keys //////////
    val KEY = in Bits (2 bit)

    //////////// SDRAM host bus ////
    val sdram = new Bundle {
      val rd_addr = out Bits (24 bit)
      val wr_addr = out Bits (24 bit)
      val wr_data = out Bits (16 bit)
      val wr_enable = out Bool
      val rd_enable = out Bool
      val rd_data = in Bits (16 bit)
      val rd_ready = in Bool
      val busy = in Bool
    }
  }

  // Instantiate a PLL for the SDRAM controller 100MHz clock
  val pll = new pll_sys() 
  pll.io.inclk0 := io.CLOCK_50
  io.CLOCK_100 := pll.io.c0

  // Create a new clock domain named 'core' to use the 100MHz clock from the PLL
  val coreClockDomain = ClockDomain.internal (
    name = "core",
    frequency = FixedFrequency(100 MHz)  // This frequency specification can be used
  )              

  // Drive clock and reset signals of the core clock domain previously created
  coreClockDomain.clock := pll.io.c0
  coreClockDomain.reset := ResetCtrl.asyncAssertSyncDeassert(
    input = !io.KEY(0) || !pll.io.locked,
    clockDomain = coreClockDomain
  )

  // Create a clocking area which will be under the effect of the core clock domain
  val core = new ClockingArea(coreClockDomain) {

    // Registers to drive sdram_controller inputs
    val wr_data = Reg(Bits(16 bit)) init 0
    val wr_enable = Reg(Bool) init False
    val rd_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] rd_addr;
    val wr_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] wr_addr;
    val rd_enable = Reg(Bool) init False

    // Registers to receive sdram_controller outputs
    val rd_data = Reg(Bits(16 bit)) init 0
    val rd_ready = Reg(Bool) init False

    // Connect all host side SDRAM signals to the test bench
    io.sdram.rd_addr := rd_addr
    io.sdram.wr_addr := wr_addr
    io.sdram.wr_data := wr_data
    io.sdram.wr_enable := wr_enable
    io.sdram.rd_enable := rd_enable
    io.rst_n := !coreClockDomain.reset

    // Suppress the "io_" prefix on module connection names in Verilog output.
    noIoPrefix()

    // Test bench logic to go here.

    val count = Reg(UInt(64 bit)) init 0
    count := count + 1

    val sdramAddress = Reg(UInt(24 bit)) init 0
    val sdramWriteData = Reg(UInt(16 bit)) init 0
    val sdramCycles = Reg(UInt (4 bits)) init 0

    val led = Reg(Bits (8 bit)) init 0
    io.LED := rd_data(15 downto 8)

    def hash(n: UInt): UInt = {
      return n * 27146105
    }

    when ((sdramAddress === 0).rise) {
      when (sdramCycles < 4) {
        sdramCycles := sdramCycles + 1
      }
    }

    val SDRAMReadWriteFSM = new StateMachine {
      val stateIdle = new State with EntryPoint
      val stateWrite = new State
      val stateWaitWriteBusy = new State
      val stateRead = new State
      val stateWaitReadReady = new State
      val stateAbend = new State

      stateIdle
        .onEntry {
          sdramAddress := sdramAddress + 1
          sdramWriteData := hash(sdramAddress + 1)(15 downto 0)
        }
        .whenIsActive {
          when (!io.sdram.busy) {
            when (sdramCycles < U(1)) {
              goto(stateWrite)
            } otherwise {
              goto(stateRead)
            }
          }
        }

      stateWrite
        .onEntry {
          wr_addr := sdramAddress.asBits
          wr_data :=  sdramWriteData.asBits
          wr_enable := True
        }
        .whenIsActive {
          when(io.sdram.busy) {
            goto(stateWaitWriteBusy)
          }
        }
        .onExit {
            wr_enable := False
        }
      stateWaitWriteBusy
        .whenIsActive {
          when(!io.sdram.busy) {
            goto(stateRead)
          }
        }

      stateRead
        .onEntry {
          rd_addr := sdramAddress.asBits
          rd_enable := True
        }
        .whenIsActive {
          when(io.sdram.busy) {
            goto(stateWaitReadReady)
          }
        }
        .onExit {
          rd_enable := False
        }
      stateWaitReadReady
        .whenIsActive {
          when(io.sdram.rd_ready) {
            when (io.sdram.rd_data === hash(sdramAddress).asBits(15 downto 0)) {
              rd_data := io.sdram.rd_data
              goto(stateIdle)
            } otherwise {
              goto(stateAbend)
            }
          }
        }
      stateAbend
        .onEntry {
          rd_data := B"1010101010101010"
        }
    }
  }
}

object SDRAMVerilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram_controller_tb)
    report.mergeRTLSource("quartus/sdram_controller_tb/sdram_controller_tb") // Merge all rtl sources into sdram_controller_tb.v file
    report.printPruned()
  }
}
