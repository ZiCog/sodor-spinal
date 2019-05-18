//
// SpinalHDL black box that wraps the SRAM contoller for the DE0-Nano board by stffrdhrn.
// Taken from https://github.com/stffrdhrn/sdram-controller
//
// WARNING! This does not work! 
//  It the SDRAM contoller requires direct access to the FPGA bidirection SRRAM data bus
//  pins, SpinalHDL does not support actual tristate signals.
//
package sodor

import spinal.core._
import spinal.lib._

// Black box that wraps the sdram_controller.v
class sdram_controller() extends BlackBox {

  // Define io of the sdram_controller Verilog module
  val io = new Bundle {
    val clk = in Bool

    // HOST interface.
    val wr_addr = in Bits (24 bits)       //input[HADDR_WIDTH - 1: 0] wr_addr;
    val wr_data = in Bits(16 bits)
    val wr_enable = in Bool

    val rd_addr = in Bits (24 bits)       //input[HADDR_WIDTH - 1: 0] rd_addr;
    val rd_data = out Bits (16 bits)
    val rd_enable = in Bool
    val rd_ready = out Bool

    val busy = out Bool
    val rst_n = in Bool

    // SDRAM Device Interface.
    val addr = out Bits (13 bits)                  // [SDRADDR_WIDTH-1:0];
    val bank_addr = out Bits (2 bits)                   //output [BANK_WIDTH-1:0]    bank_addr;
    val data = inout (Analog(Bits (16 bits)))
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
