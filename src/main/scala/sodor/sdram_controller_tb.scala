package sodor

import spinal.core._

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
class sdram_controller_tb extends Component {
  val io = new Bundle {

    //////////// CLOCK //////////
    val CLOCK_50 = in Bool

    //////////// LED //////////
    // val LED = out Bits (8 bit)

    //////////// KEY //////////
    // val KEY = in Bits (2 bit)


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

  // Connect all host side SDRAM signals to the test bench
  ram.io.rd_addr := rd_addr
  ram.io.wr_addr := wr_addr
  ram.io.wr_data := wr_data
  ram.io.wr_enable := wr_enable
  ram.io.rd_enable := rd_enable
  ram.io.rst_n := rst_n

  rd_data := ram.io.rd_data
  rd_ready := ram.io.rd_ready
  busy := ram.io.busy

  // Connect all SDRAM device signals to outside world.
  io.DRAM_CLK <> io.CLOCK_50
  io.DRAM_ADDR <> ram.io.addr
  io.DRAM_BA <> ram.io.bank_addr
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
  rd_addr := 0
  wr_addr := 0
  wr_data := 0
  wr_enable := False
  rd_enable := False
  rst_n := False
}

object SDRAMVerilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram_controller_tb)
    report.mergeRTLSource("quartus/sdram_controller_tb/sdram_controller_tb") // Merge all rtl sources into sdram_controller_tb.v file
    report.printPruned()
  }
}
