package sodor

import spinal.core._

// Define a Ram as a BlackBox
class sdram_controller(wordWidth: Int, wordCount: Int) extends BlackBox {

  // SpinalHDL will look at Generic classes to get attributes which
  // should be used ad VHDL gererics / Verilog parameter
  // You can use String Int Double Boolean and all SpinalHDL base types
  // as generic value
  val generic = new Generic {
    val wordCount = sdram_controller.this.wordCount
    val wordWidth = sdram_controller.this.wordWidth
  }

  // Define io of the VHDL entiry / Verilog module
  val io = new Bundle {
    val clk = in Bool

    // HOST interface
    val wr_addr = in Bits (24 bit)       //input[HADDR_WIDTH - 1: 0] wr_addr;
    val wr_data = in Bits(16 bit)
    val wr_enable = in Bool

    val rd_addr = in Bits (24 bit)       //input[HADDR_WIDTH - 1: 0] rd_addr;
    val rd_data = out Bits (16 bit)
    val rd_enable = in Bool
    val rd_ready = out Bool

    val busy = out Bool
    val rst_n = in Bool

    // SDRAM Device Interface
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

  // Map the current clock domain to the io.clk pin
  mapClockDomain(clock=io.clk)

  // Add all rtl dependencies
  addRTLPath("./rtl/sdram_controller.v")
  addRTLPath("./sdram_controller_tb.v")

  noIoPrefix()
}

// Create the top level and instantiate the SD RAM controller
class sdram_controller_tb extends Component {
  val io = new Bundle {
    val DRAM_ADDR = out Bits (13 bit)            // output [SDRADDR_WIDTH-1:0];
    val DRAM_BA = out Bits (2 bit)               //output [BANK_WIDTH-1:0]    bank_addr;
    val DRAM_DQ = inout Bits (16 bit)
    val DRAM_CKE = out Bool
    val DRAM_CS_N = out Bool
    val DRAM_RAS_N = out Bool
    val DRAM_CAS_N = out Bool
    val DRAM_WE_N = out Bool
    val DRAM_DQM = out Bits (2 bits)
  }

  // Instantiate the SDRAM controller black box
  val ram = new sdram_controller(8,16)

  val wr_data = Reg(Bits(16 bit)) init 0
  val wr_enable = Reg(Bool) init False

  val rd_addr = Reg (Bits (24 bit))         //input[HADDR_WIDTH - 1: 0] rd_addr;
  val wr_addr = Reg (Bits (24 bit))         //input[HADDR_WIDTH - 1: 0] wr_addr;
  val rd_data = Reg(Bits (16 bit))
  val rd_enable = Reg(Bool) init False
  val rd_ready = Reg(Bool)

  val busy = Reg(Bool)
  val rst_n = Reg(Bool) init False   // TODO: This should be connected to our global reset.

  rd_addr := 0
  wr_addr := 0
  wr_data := 0
  wr_enable := False
  rd_enable := False
  rst_n := False

  // Connect all host side SDRAM signals to something
  ram.io.rd_addr := rd_addr
  ram.io.wr_addr := wr_addr
  ram.io.wr_data := wr_data
  ram.io.wr_enable := wr_enable
  ram.io.rd_enable := rd_enable
  ram.io.rst_n := rst_n

  // Connect all SDRAM device signals to outside world
  io.DRAM_ADDR <> ram.io.addr
  io.DRAM_BA <> ram.io.bank_addr
  io.DRAM_DQ := ram.io.data
  io.DRAM_CKE <> ram.io.clock_enable
  io.DRAM_CS_N <> ram.io.cs_n
  io.DRAM_RAS_N <> ram.io.ras_n
  io.DRAM_CAS_N <> ram.io.cas_n
  io.DRAM_WE_N <> ram.io.we_n
  io.DRAM_DQM(0) <> ram.io.data_mask_low     // TODO: These bits the right way around?
  io.DRAM_DQM(1) <> ram.io.data_mask_high

  noIoPrefix()
}

object Main {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram_controller_tb)
    report.mergeRTLSource("mergeRTL") // merge all rtl sources into mergeRTL.vhd and mergeRTL.v file
    report.printPruned()
  }
}
