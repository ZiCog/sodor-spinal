//
// Define a TriState bus bundle
// See here for explanation: https://spinalhdl.github.io/SpinalDoc-RTD/SpinalHDL/Libraries/IO/tristate.html?highlight=tristate
//
package sodor

import spinal.core._
import spinal.lib._

case class TriState[T <: Data](dataType : HardType[T]) extends Bundle with IMasterSlave {
  val read,write : T = dataType()
  val writeEnable = Bool

  override def asMaster(): Unit = {
    out (write, writeEnable)
    in (read)
  }
}
