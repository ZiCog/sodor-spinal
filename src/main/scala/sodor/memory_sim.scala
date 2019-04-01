package sodor

import Array._
import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

object MemorySim {
  def main(args: Array[String]) {

    val compiled2 = SimConfig.withWave.compile {
      val dut = new Memory(32, 1024, "firmware.hex")
      dut
    }

    compiled2.doSim("test 1") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var address = 0
      while (address < 128) {
        // Drive the DUT inputs
        dut.io.enable #= true
        dut.io.mem_valid #= true
        dut.io.mem_instr #= false
        dut.io.mem_wstrb #= 0
        dut.io.mem_wdata #= 0
        dut.io.mem_addr #= address

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()

        // Check the DUT output
        println(address.toHexString, dut.io.mem_rdata.toInt.toHexString)

        address = address + 1
      }
    }
  }
}

