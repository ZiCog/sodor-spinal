package sodor

import Array._
import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

object MemorySim {

  def write32 (dut : Memory, address: Int, data: Int) = {
    // Drive inputs for write operation
    dut.io.enable #= true
    dut.io.mem_valid #= true
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0xF
    dut.io.mem_wdata #= data
    dut.io.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.enable #= false
    dut.io.mem_valid #= false
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0
    dut.io.mem_wdata #= 0
    dut.io.mem_addr #= 0
  }

  def write16 (dut : Memory, address: Int, data: Int) = {
    // Drive inputs for write operation
    dut.io.enable #= true
    dut.io.mem_valid #= true
    dut.io.mem_instr #= false
    if((address & 1) == 0) {
      dut.io.mem_wstrb #= 0x03
    } else {
      dut.io.mem_wstrb #= 0x0c
    }
    dut.io.mem_wdata #= data
    dut.io.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.enable #= false
    dut.io.mem_valid #= false
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0
    dut.io.mem_wdata #= 0
    dut.io.mem_addr #= 0
  }

  def write8 (dut : Memory, address: Int, data: Int) = {
    // Drive inputs for write operation
    dut.io.enable #= true
    dut.io.mem_valid #= true
    dut.io.mem_instr #= false
    if((address & 0x3) == 0) {
      dut.io.mem_wstrb #= 0x01
    } else if ((address & 0x3) == 1) {
      dut.io.mem_wstrb #= 0x02
    } else if ((address & 0x3) == 2) {
      dut.io.mem_wstrb #= 0x04
    } else if ((address & 0x3) == 3) {
      dut.io.mem_wstrb #= 0x08
    }
    dut.io.mem_wdata #= data
    dut.io.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.enable #= false
    dut.io.mem_valid #= false
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0
    dut.io.mem_wdata #= 0
    dut.io.mem_addr #= 0
  }



  def read32 (dut : Memory, address: Int) = {
    // Drive inputs for read operation
    dut.io.enable #= true
    dut.io.mem_valid #= true
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0
    dut.io.mem_wdata #= 0
    dut.io.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.enable #= false
    dut.io.mem_valid #= false
    dut.io.mem_instr #= false
    dut.io.mem_wstrb #= 0
    dut.io.mem_wdata #= 0
    dut.io.mem_addr #= 0

    dut.io.mem_rdata
  }

  def main(args: Array[String]) {

    val compiled = SimConfig.withWave.compile {
      val dut = new Memory(32, 1024, "firmware.hex")
      dut
    }

    // Check initial RAM content.
    compiled.doSim("test 1") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var address = 0
      while (address < 1024) {
        val res = read32(dut, address)
        println(address.toHexString, res.toInt.toHexString)
        address = address + 4
      }
      println("PASS")
    }

    // Clear RAM and check
    compiled.doSim("test 2") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var address = 0
      while (address < 1024) {
        write32(dut, address, 0)
        address = address + 4
      }
      address = 0
      while (address < 1024) {
        val res = read32(dut, address)
        assert(res.toInt == 0)
        address = address + 4
      }
      println("PASS")
    }

    // Write and read random data and check
    compiled.doSim("test 3") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var loops = 1000
      while (loops != 0) {
        random.setSeed(0xdeadbeef)
        var address = 0
        while (address < 1024) {
          val r = random.nextInt
          write32(dut, address, r)
          address = address + 4
        }
        random.setSeed(0xdeadbeef)
        address = 0
        while (address < 1024) {
          val expected = random.nextInt
          val res = read32(dut, address)
          assert(expected == res.toInt)
          address = address + 4
        }
        loops = loops - 1
      }
      println("PASS")
    }
  }
}

