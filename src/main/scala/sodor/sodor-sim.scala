package Sodor

import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

// sodor's testbench
object SodorSim {
  def main(args: Array[String]) {
    SimConfig.withWave.doSim(new ProgramCounter){dut =>
      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      while(idx < 100){

        // Drive the dut inputs
        dut.io.pcNext #= 0

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()

        // Check that the dut values match with the reference model ones
        assert(dut.io.pc.toInt == 0)
        assert(dut.io.pc4.toInt == 4)

        println(dut.io.pc4.toInt, dut.io.pc4.toInt)

        idx += 1
      }
    }
  }
}
