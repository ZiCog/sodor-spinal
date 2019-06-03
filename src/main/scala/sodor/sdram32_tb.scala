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

  // Suppress the "io_" prefix on module connection names in Verilog output.
  noIoPrefix()

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

    // Connect all the SDRAM controler signals to the outside world
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

    // Connect SDRAM32 driver to SDRM controller
    sdram.io.rd_addr <> sdram32.io.sdram.rd_addr
    sdram.io.wr_addr <> sdram32.io.sdram.wr_addr
    sdram.io.wr_data <> sdram32.io.sdram.wr_data
    sdram.io.wr_enable <> sdram32.io.sdram.wr_enable
    sdram.io.rd_enable <> sdram32.io.sdram.rd_enable
    sdram.io.rst_n <> !coreClockDomain.reset

    sdram32.io.sdram.busy := sdram.io.busy
    sdram32.io.sdram.rd_data := sdram.io.rd_data
    sdram32.io.sdram.rd_ready := sdram.io.rd_ready

    // Registers to drive/receive SRRAM32 host interface
    val enable = Reg(Bool) init False
    val mem_valid = Reg (Bool) init False
    val mem_instr = Reg (Bool) init False
    val mem_wstrb = Reg (UInt(4 bits)) init 0
    val mem_wdata = Reg (SInt(32 bits)) init 0
    val mem_addr  = Reg (UInt(32 bits)) init 0
    val mem_rdata = Reg (SInt(32 bits)) init 0
    val mem_ready = Reg (Bool) init False

    // Connect up SDRAM32 host interface bus
    sdram32.io.host.enable := enable
    sdram32.io.host.mem_valid := mem_valid
    sdram32.io.host.mem_instr := mem_instr
    sdram32.io.host.mem_wstrb := mem_wstrb
    sdram32.io.host.mem_wdata := mem_wdata
    sdram32.io.host.mem_addr := mem_addr
    mem_rdata := sdram32.io.host.mem_rdata
    mem_ready := sdram32.io.host.mem_ready

    // Drive SDRAM32 host interface bus to idle state
    enable := False
    mem_valid := False
    mem_instr := False
    mem_wstrb := 0
    mem_wdata := 0
    mem_addr := 0

    // A really bad random number
    def hash(n: UInt): UInt = {
      n * 27146105
    }

    val address = Reg (UInt (32 bits)) init 0
    address := 4 + 16 + 64

    val data = Reg (SInt (32 bits)) init 0

    val timer = Reg (SInt (32 bits)) init 0
    timer := timer + 1
    when (timer === 100000000) {
      timer := 0
      data := data + 1
      address := address + 4
    }

    io.LED := data.asBits(7 downto 0)

    val SDRAM32ReadWriteFSM = new StateMachine {
      val state0 = new State with EntryPoint
      val state1 = new State
      val state2 = new State
      val state3 = new State
      val state4 = new State
      val state5 = new State
      val state6 = new State

      state0
        // Idle the bus
        .whenIsActive {
          enable := False
          mem_valid := False
          mem_wstrb := 0
          goto(state1)
      }
      state1
        // Start write cycle
        .whenIsActive {
          enable := True
          mem_valid := True
          mem_wstrb := U"1111"
          mem_wdata := data
          mem_addr := address
          goto(state2)
        }
      state2
        // Wait for memory ready
        .whenIsActive {
          when(mem_ready) {
            goto(state3)
          }
        }
      state3
        // Idle the bus
        .whenIsActive {
          enable := False
          mem_valid := False
          mem_wstrb := 0
          goto (state4)
        }
      state4
        // Start read cycle
        .whenIsActive {
          enable := True
          mem_valid := True
          mem_addr := address
          goto (state5)
        }
      state5
        // Wait for memory ready
        .whenIsActive {
          when(mem_ready) {
            goto(state6)
          }
        }
      state6
        // Read the data
        .whenIsActive {
          when(mem_ready) {
            data := mem_rdata
            goto(state0)
          }
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
