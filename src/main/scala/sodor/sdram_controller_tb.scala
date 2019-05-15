package sodor

import spinal.core._
import spinal.lib._

// Black box that wraps a Quartus pll_sys phased locked loop.
class pll_sys() extends BlackBox {
  // Define io of the pll_sys Verilog module
  val io = new Bundle {
    val inclk0 = in Bool
    val c0 = out Bool
    val c1 = out Bool
    val c2 = out Bool
    val locked = out Bool
  }

  // Suppress the "io_" prefix on module connection names in the Verilog output.
  noIoPrefix()
}

// Black box that wraps the sdram_controller.v
// taken from https://github.com/stffrdhrn/sdram-controller
class sdram_controller() extends BlackBox {

  // Define io of the sdram_controller Verilog module
  val io = new Bundle {
    val clk = in Bool

    // HOST interface.
    val wr_addr = in Bits (24 bit)       //input[HADDR_WIDTH - 1: 0] wr_addr;
    val wr_data = in Bits(16 bit)
    val wr_enable = in Bool

    val rd_addr = in Bits (24 bit)       //input[HADDR_WIDTH - 1: 0] rd_addr;
    val rd_data = out Bits (16 bit)
    val rd_enable = in Bool
    val rd_ready = out Bool

    val busy = out Bool
    val rst_n = in Bool

    // SDRAM Device Interface.
    val addr = out Bits (13 bit)                  // [SDRADDR_WIDTH-1:0];
    val bank_addr = out Bits (2 bit)                   //output [BANK_WIDTH-1:0]    bank_addr;
    val data = inout Bits (16 bit)
    val clock_enable = out Bool
    val cs_n = out Bool
    val ras_n = out Bool
    val cas_n = out Bool
    val we_n = out Bool
    val data_mask_low = out Bool
    val data_mask_high = out Bool
  }

  // Map the clock domain to the io.clk pin.
  mapClockDomain(clock=io.clk)

  // Suppress the "io_" prefix on module connection names in the Verilog output.
  noIoPrefix()
}

// Create the test bench top level and instantiate the SD RAM controller
class sdram_controller_tb extends Component{
  val io = new Bundle {

    //////////// CLOCK //////////
    val CLOCK_50 = in Bool

    //////////// LED //////////
    val LED = out Bits (8 bit)

    //////////// KEY //////////
    val KEY = in Bits (2 bit)

    //////////// SW //////////
    // val SW = in Bits (4 bit)

    //////////// SDRAM //////////
    val DRAM_ADDR = out Bits (13 bit)            // output [SDRADDR_WIDTH-1:0];
    val DRAM_BA = out Bits (2 bit)               // output [BANK_WIDTH-1:0]    bank_addr;
    val DRAM_CAS_N = out Bool
    val DRAM_CKE = out Bool
    val DRAM_CLK = out Bool
    val DRAM_CS_N = out Bool
    val DRAM_DQ = inout Bits (16 bit)
    val DRAM_DQM = out Bits (2 bits)
    val DRAM_RAS_N = out Bool
    val DRAM_WE_N = out Bool

    //////////// EPCS //////////
    // val EPCS_ASDO = out Bool
    // val EPCS_DATA0 = in Bool
    // val EPCS_DCLK = out Bool
    // val EPCS_NCSO = out Bool

    //////////// Accelerometer and EEPROM //////////
    //output                                  G_SENSOR_CS_N,
    //input                                   G_SENSOR_INT,
    //output                                  I2C_SCLK,
    //inout                                   I2C_SDAT,

    //////////// ADC //////////
    //output                                  ADC_CS_N,
    //output                                  ADC_SADDR,
    //output                                  ADC_SCLK,
    //input                                   ADC_SDAT,

    //////////// 2x13 GPIO Header //////////
    //inout               [12:0]              GPIO_2,
    //input                [2:0]              GPIO_2_IN,

    //////////// GPIO_0, GPIO_0 connect to GPIO Default //////////
    //inout               [33:0]              GPIO_0,
    //input                [1:0]              GPIO_0_IN,

    //////////// GPIO_1, GPIO_1 connect to GPIO Default //////////
    //inout               [33:0]              GPIO_1,
    //input                [1:0]              GPIO_1_IN
  }

  // Instantiate the PLL for the SDRAM controller 100MHz clock
  val pll = new pll_sys() 
  val clock_100 = Bool
  pll.io.inclk0 := io.CLOCK_50
  clock_100 := pll.io.c0

  // Create a new clock domain named 'core' to use the 100MHz clock from the PLL
  val coreClockDomain = ClockDomain.internal (
    name = "core",
    frequency = FixedFrequency(100 MHz)  // This frequency specification can be used
  )              

  // Drive clock and reset signals of the core clock domain previously created
  coreClockDomain.clock := pll.io.c0
  coreClockDomain.reset := ResetCtrl.asyncAssertSyncDeassert(
    input = !io.KEY(0) || !pll.io.locked,                 // FIXME:
    clockDomain = coreClockDomain
  )

  // Create a clocking area which will be under the effect of the core clock domain
  val core = new ClockingArea(coreClockDomain) {

    // Instantiate the SDRAM controller black box
    val ram = new sdram_controller()

    // Add all rtl dependencies to merged Verilog output
    ram.addRTLPath("./sdram_controller_tb.v")
    ram.addRTLPath("./rtl/sdram_controller.v")

    // Registers to drive sdram_controller inputs
    val wr_data = Reg(Bits(16 bit)) init 0
    val wr_enable = Reg(Bool) init False init False
    val rd_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] rd_addr;
    val wr_addr = Reg (Bits (24 bit)) init 0        //input[HADDR_WIDTH - 1: 0] wr_addr;
    val rd_enable = Reg(Bool) init False

    // Registers to receive sdram_controller outputs
    val rd_data = Reg(Bits(16 bit)) init 0
    val rd_ready = Reg(Bool) init False
    val busy = Reg(Bool) init False

    val rst_n = Reg(Bool) init False   // TODO: This should be connected to our global reset.

    val data = Bits(16 bit)

    // Connect all host side SDRAM signals to the test bench
    ram.io.rd_addr := rd_addr
    ram.io.wr_addr := wr_addr
    ram.io.wr_data := wr_data
    ram.io.wr_enable := wr_enable
    ram.io.rd_enable := rd_enable
    ram.io.rst_n := coreClockDomain.reset              // rst_n  FIXME !!!!! <<<<<<<

    rd_data := ram.io.rd_data
    rd_ready := ram.io.rd_ready
    busy := ram.io.busy

    // Connect all SDRAM device signals to outside world.
    io.DRAM_CLK <> clock_100
    io.DRAM_ADDR <> ram.io.addr
    io.DRAM_BA <> ram.io.bank_addr

    // WARNING! 
    io.DRAM_DQ := ram.io.data

    io.DRAM_CKE <> ram.io.clock_enable
    io.DRAM_CS_N <> ram.io.cs_n
    io.DRAM_RAS_N <> ram.io.ras_n
    io.DRAM_CAS_N <> ram.io.cas_n
    io.DRAM_WE_N <> ram.io.we_n
    io.DRAM_DQM(0) <> ram.io.data_mask_low
    io.DRAM_DQM(1) <> ram.io.data_mask_high

    // Suppress the "io_" prefix on module connection names in Verilog output.
    noIoPrefix()

    // Test bench logic to go here.

    val count = Reg(UInt(64 bit)) init 85
    val led = Reg(Bits(8 bit)) init 0
    led := count.asBits(31 downto 24)
    io.LED := led
    count := count + 1


    rd_addr := 0
    wr_addr := 0
    wr_data := 0
    wr_enable := False
    rd_enable := False
    rst_n := False
  }
}

object SDRAMVerilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram_controller_tb)
    report.mergeRTLSource("quartus/sdram_controller_tb/sdram_controller_tb") // Merge all rtl sources into sdram_controller_tb.v file
    report.printPruned()

    println("")
    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    println("!!                                                         !!")
    println("!! WARNING!                                                !!")
    println("!!                                                         !!")
    println("!! Spinal cannot hadle bidirectional signals               !!")
    println("!! Be sure to edit sdram_controller_tb.v to make this work !!")
    println("!! Hint: Change:                                           !!")
    println("!!          .data(data),                                   !!")
    println("!!       To:                                               !!")
    println("!!          .data(DRAM_DQ),                                !!")
    println("!!                                                         !!")
    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    println("")
  }
}
