//
// Defines a SpinalHDL black box that wraps a Quartus MegaWizard generated PLL.
//

package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._


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

  // Merge RTL sources into output
  addRTLPath("./sdram_controller_tb.v")

  // Suppress the "io_" prefix on module connection names in the Verilog output.
  noIoPrefix()
}
