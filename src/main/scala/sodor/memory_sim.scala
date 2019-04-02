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
    dut.io.mem_wstrb #= 0x3 << ((address & 1) * 2)   // TODO: Check this
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
    dut.io.mem_wstrb #= 1 << (address & 0x3)    // TODO: Check this
    dut.io.mem_wdata #= data << ((address & 0x3) * 8) // TODO: Check this
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

  // TODO: Check this
  def read16 (dut : Memory, address: Int) = {
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

  // TODO: Check this
  def read8 (dut : Memory, address: Int) = {
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

    (dut.io.mem_rdata.toInt >> (address & 0x03) * 8) & 0xff
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

    // Write and read random words and check
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

    // Write and read random words and check
    compiled.doSim("test 4") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      val address = 0

      write32(dut, address, 0)
      val res = read32(dut, address)
      println(address.toHexString, res.toInt.toHexString)

      write8(dut, address + 0, 0xf0)
      val res0 = read8(dut, address + 0)
      println(address.toHexString, res0.toInt.toHexString)

      write8(dut, address + 1, 0xf1)
      val res1 = read8(dut, address + 1)
      println(address.toHexString, res1.toInt.toHexString)

      write8(dut, address + 2, 0xf2)
      val res2 = read8(dut, address + 2)
      println(address.toHexString, res2.toInt.toHexString)

      write8(dut, address + 3, 0xf3)
      val res3 = read8(dut, address + 3)
      println(address.toHexString, res3.toInt.toHexString)

      val ress = read32(dut, address + 3)
      println(address.toHexString, ress.toInt.toHexString)

      println("PASS")
    }

    // Write and read random bytes and check
    compiled.doSim("test 5") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var loops = 1000
      while (loops != 0) {
        random.setSeed(0xdeadbeef)
        var address = 0
        while (address < 1024) {
          val r = random.nextInt & 0xff
          write8(dut, address, r)
          address = address + 1
        }
        random.setSeed(0xdeadbeef)
        address = 0
        while (address < 1024) {
          val expected = random.nextInt & 0xff
          val res = read8(dut, address)
          assert(expected == res.toInt)
          address = address + 1
        }
        loops = loops - 1
      }
      println("PASS")
    }
  }
}

