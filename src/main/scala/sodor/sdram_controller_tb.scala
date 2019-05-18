package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class sdram_controller_tb extends Component {
  val io = new Bundle {
    //////////// Clock and reset ///
    val CLOCK_50 = in Bool

    //////////// Leds //////////////
    val LED = out Bits (8 bit)

    //////////// Keys //////////
    val KEY = in Bits (2 bit)

    //////////// SDRAM //////////
    val DRAM_ADDR = out Bits (13 bit)
    val DRAM_BA = out Bits (2 bit)
    val DRAM_CAS_N = out Bool
    val DRAM_CKE = out Bool
    val DRAM_CLK = out Bool
    val DRAM_CS_N = out Bool
    val DRAM_DQ = inout (Analog(Bits (16 bit))) // Bidirectional data bus
    val DRAM_DQM = out Bits (2 bit)
    val DRAM_RAS_N = out Bool
    val DRAM_WE_N = out Bool
  }

  // Instantiate a PLL for the SDRAM controller 100MHz clock
  val pll = new pll_sys() 
  pll.io.inclk0 := io.CLOCK_50
  io.DRAM_CLK := pll.io.c0

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

    // Instantiate the SDRAM blackbox
    val sdram_bb = new sdram_controller()

    // Connect all the SDRAM signals
    io.DRAM_ADDR   <> sdram_bb.io.addr
    io.DRAM_BA     <> sdram_bb.io.bank_addr
    io.DRAM_DQ     := sdram_bb.io.data
    io.DRAM_CKE    <> sdram_bb.io.clock_enable
    io.DRAM_CS_N   <> sdram_bb.io.cs_n
    io.DRAM_RAS_N  <> sdram_bb.io.ras_n
    io.DRAM_CAS_N  <> sdram_bb.io.cas_n
    io.DRAM_WE_N   <> sdram_bb.io.we_n 
    io.DRAM_DQM(0) <> sdram_bb.io.data_mask_low
    io.DRAM_DQM(1) <> sdram_bb.io.data_mask_high

    // Registers to drive sdram_controller inputs
    val wr_data = Reg(Bits(16 bit)) init 0
    val wr_enable = Reg(Bool) init False
    val rd_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] rd_addr;
    val wr_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] wr_addr;
    val rd_enable = Reg(Bool) init False

    // Registers to receive sdram_controller outputs
    val rd_data = Reg(Bits(16 bit)) init 0

    // Connect all host side SDRAM signals to the test bench
    sdram_bb.io.rd_addr := rd_addr
    sdram_bb.io.wr_addr := wr_addr
    sdram_bb.io.wr_data := wr_data
    sdram_bb.io.wr_enable := wr_enable
    sdram_bb.io.rd_enable := rd_enable
    sdram_bb.io.rst_n := !coreClockDomain.reset

    // Suppress the "io_" prefix on module connection names in Verilog output.
    noIoPrefix()

    // Test bench logic to go here.
    val sdramAddress = Reg(UInt(24 bit)) init 0
    val sdramWriteData = Reg(UInt(16 bit)) init 0
    val sdramCycles = Reg(UInt (4 bits)) init 0

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
          when (!sdram_bb.io.busy) {
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
          when(sdram_bb.io.busy) {
            goto(stateWaitWriteBusy)
          }
        }
        .onExit {
            wr_enable := False
        }
      stateWaitWriteBusy
        .whenIsActive {
          when(!sdram_bb.io.busy) {
            goto(stateRead)
          }
        }

      stateRead
        .onEntry {
          rd_addr := sdramAddress.asBits
          rd_enable := True
        }
        .whenIsActive {
          when(sdram_bb.io.busy) {
            goto(stateWaitReadReady)
          }
        }
        .onExit {
          rd_enable := False
        }
      stateWaitReadReady
        .whenIsActive {
          when(sdram_bb.io.rd_ready) {
            when (sdram_bb.io.rd_data === hash(sdramAddress).asBits(15 downto 0)) {
              rd_data := sdram_bb.io.rd_data
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
