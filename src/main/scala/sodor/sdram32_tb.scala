package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class sdram32_tb extends Component {
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

  // Create a new clock domain named 'core' to use the 100MHz clock from the PLL
  val coreClockDomain = ClockDomain.internal (
    name = "core",
    frequency = FixedFrequency(100 MHz)  // This frequency specification can be used
  )

  // Create a clocking area which will be under the effect of the core clock domain
  val core = new ClockingArea(coreClockDomain) {

    // Instantiate a PLL for the SDRAM controller 100MHz clock
    val pll = new pll_sys()
    pll.io.inclk0 := io.CLOCK_50
    io.DRAM_CLK := pll.io.c0

    // Drive clock and reset signals of the core clock domain previously created
    coreClockDomain.clock := pll.io.c0
    coreClockDomain.reset := ResetCtrl.asyncAssertSyncDeassert(
      input = !io.KEY(0) || !pll.io.locked,
      clockDomain = coreClockDomain
    )

    // Instantiate the SDRAM blackbox
    val sdram = new sdram_controller()

    // Instantiate the SDRAM 32 interface
    val sdram32 = new Sdram32(32, 8 * 1024 * 1024)

    // Connect all the SDRAM signals to the outside world
    io.DRAM_ADDR   <> sdram.io.addr
    io.DRAM_BA     <> sdram.io.bank_addr
    io.DRAM_DQ     := sdram.io.data
    io.DRAM_CKE    <> sdram.io.clock_enable
    io.DRAM_CS_N   <> sdram.io.cs_n
    io.DRAM_RAS_N  <> sdram.io.ras_n
    io.DRAM_CAS_N  <> sdram.io.cas_n
    io.DRAM_WE_N   <> sdram.io.we_n
    io.DRAM_DQM(0) <> sdram.io.data_mask_low
    io.DRAM_DQM(1) <> sdram.io.data_mask_high

    sdram.io.rd_addr <> sdram32.io.sdram.rd_addr
    sdram.io.wr_addr <> sdram32.io.sdram.wr_addr
    sdram.io.wr_data <> sdram32.io.sdram.wr_data
    sdram.io.wr_enable <> sdram32.io.sdram.wr_enable
    sdram.io.rd_enable <> sdram32.io.sdram.rd_enable
    sdram.io.rst_n <> coreClockDomain.reset

    sdram32.io.sdram.busy := sdram.io.busy
    sdram32.io.sdram.rd_data := sdram.io.rd_data
    sdram32.io.sdram.rd_ready := sdram.io.rd_ready

    // A really bad random number
    def hash(n: UInt): UInt = {
      n * 27146105
    }

    // Drive idle on bus
    def cmdIdle () {
      sdram32.io.host.enable := False
      sdram32.io.host.mem_valid := False
      sdram32.io.host.mem_instr := False
      sdram32.io.host.mem_wstrb := 0
      sdram32.io.host.mem_wdata := 0
      sdram32.io.host.mem_addr := 0
    }

    // Drive write command onto bus
    def cmdWrite32 (address: UInt, data:SInt) {
      sdram32.io.host.enable := True
      sdram32.io.host.mem_valid := True
      sdram32.io.host.mem_instr := False
      sdram32.io.host.mem_wstrb := 0xF
      sdram32.io.host.mem_wdata := data
      sdram32.io.host.mem_addr := address
    }

    // Drive read command onto bus
    def cmdRead32 (address: UInt)= {
      sdram32.io.host.enable := True
      sdram32.io.host.mem_valid := True
      sdram32.io.host.mem_instr := False
      sdram32.io.host.mem_wstrb := 0
      sdram32.io.host.mem_wdata := 0
      sdram32.io.host.mem_addr := address
    }

    // Read the data
    def read32 () : SInt = {
      sdram32.io.host.mem_rdata
    }
    // Test for ready signal
    def isReady () : Bool = {
      sdram32.io.host.mem_ready
    }

    cmdIdle()
    val address = UInt(32 bits)
    address := 0xad
    val data = Reg (SInt (32 bits)) init 0x55555555
    io.LED := data.asBits(7 downto 0)

    val SDRAM32ReadWriteFSM = new StateMachine {
      val stateIdle = new State with EntryPoint
      val stateWrite = new State
      val stateRead = new State
      val stateAbend = new State

      always {
  //        cmdIdle()
      }

      stateIdle
        .whenIsActive {
          cmdIdle()
          when (!isReady()) {
            goto(stateWrite)
          }
        }

      stateWrite
        .whenIsActive {
          cmdWrite32(address, data)
          when (isReady()) {
            goto(stateRead)
          }
        }

      stateRead
        .whenIsActive {
          data := read32()
          when (isReady()) {
            goto(stateIdle)
          }
        }

      stateAbend
        .whenIsActive {
          cmdIdle()
          goto (stateIdle)
        }
    }
  }
}

object sdram32_tb_Verilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram32_tb)
    report.mergeRTLSource("quartus/sdram32_tb/sdram32_tb") // Merge all rtl sources into sdram32_tb.v file
    report.printPruned()
  }
}
