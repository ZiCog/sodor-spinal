package sodor

import Array._
import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

object Sdram32_sim {

  def write32 (dut : Sdram32, address: Int, data: Int) = {
    assert((address & 0x03) == 0)

    // Drive inputs for write operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0xF
    dut.io.host.mem_wdata #= data
    dut.io.host.mem_addr #= address


    // Drive SDRAM inputs
    dut.io.sdram.rd_data #= 42 
    dut.io.sdram.rd_ready #= false
    dut.io.sdram.busy #= false


    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    dut.clockDomain.waitRisingEdge()
    println("2. sdram.wr_addr = ", dut.io.sdram.rd_addr.toInt)
    print(dut.io.sdram.rd_addr.toInt)
    print(" ")
    print(dut.io.sdram.wr_addr.toInt)
    print(" ")
    print(dut.io.sdram.wr_data.toInt)
    print(" ")
    print(dut.io.sdram.wr_enable.toBoolean)
    print(" ")
    print(dut.io.sdram.rd_enable.toBoolean)
    print(" ")
    println()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0
  }

  def write16 (dut : Sdram32, address: Int, data: Int) = {
    assert((address & 0x01) == 0)

    // Drive inputs for write operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    if ((address & 0x3) == 0) {
      dut.io.host.mem_wstrb #= 0x03
      dut.io.host.mem_wdata #= data
    } else {
      dut.io.host.mem_wstrb #= 0x0C
      dut.io.host.mem_wdata #= data << 16
    }
    dut.io.host.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0
  }

  def write8 (dut : Sdram32, address: Int, data: Int) = {
    // Drive inputs for write operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 1 << (address & 0x3)
    dut.io.host.mem_wdata #= data << ((address & 0x3) * 8)
    dut.io.host.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0
  }

  def read32 (dut : Sdram32, address: Int) = {
    assert((address & 0x03) == 0)

    // Drive inputs for read operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0

    dut.io.host.mem_rdata
  }

  def read16 (dut : Sdram32, address: Int) = {
    assert((address & 0x01) == 0)

    // Drive inputs for read operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0

    if ((address & 0x3) == 0) {
      dut.io.host.mem_rdata.toInt & 0xffff
    } else {
      (dut.io.host.mem_rdata.toInt >> 16) & 0xffff
    }
  }

  def read8 (dut : Sdram32, address: Int) = {
    // Drive inputs for read operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= address

    // Wait for a rising edge on the clock
    dut.clockDomain.waitRisingEdge()

    // Drive inputs to idle again
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0

    (dut.io.host.mem_rdata.toInt >> (address & 0x03) * 8) & 0xff
  }


  def main(args: Array[String]) {

    val width = 32
    val depth = 16 * 1024 * 1024
    val maxAddress = 64
    
    val compiled = SimConfig.withWave.compile {
      val dut = new Sdram32(width, depth)
      //dut.io.sdram.rd_data.simPublic()
      dut
    }

    // Clear RAM and check
    compiled.doSim("Test 1") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period   = 10)

      var address = 0
      while (address < maxAddress) {
        write32(dut, address, 0)
        address = address + 4
      }
      address = 0
      while (address < maxAddress) {
        val res = read32(dut, address)
        assert(res.toInt == 0)
        address = address + 4
      }
      println("PASS")
    }
/*
    // Write and read random words and check
    compiled.doSim("Test 2") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var loops = 100
      while (loops != 0) {
        random.setSeed(0xdeadbeef)
        var address = 0
        while (address < maxAddress) {
          val r = random.nextInt
          write32(dut, address, r)
          address = address + 4
        }
        random.setSeed(0xdeadbeef)
        address = 0
        while (address < maxAddress) {
          val expected = random.nextInt
          val res = read32(dut, address)
   //       assert(expected == res.toInt)
          address = address + 4
        }
        loops = loops - 1
      }
      println("PASS")
    }

    // Write and read half words and check
    compiled.doSim("Test 3") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var loops = 100
      while (loops != 0) {
        random.setSeed(0xdeadbeef)
        var address = 0
        while (address < maxAddress) {
          val r = random.nextInt & 0xffff
          write16(dut, address, r)
          address = address + 2
        }
        random.setSeed(0xdeadbeef)
        address = 0
        while (address < maxAddress) {
          val expected = random.nextInt & 0xffff
          val res = read16(dut, address)
//          assert(expected == res.toInt)
          address = address + 2
        }
        loops = loops - 1
      }
      println("PASS")
    }

    // Write and read random bytes and check
    compiled.doSim("Test 4") { dut =>
      val random = scala.util.Random

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      var loops = 100
      while (loops != 0) {
        random.setSeed(0xdeadbeef)
        var address = 0
        while (address < maxAddress) {
          val r = random.nextInt & 0xff
          write8(dut, address, r)
          address = address + 1
        }
        random.setSeed(0xdeadbeef)
        address = 0
        while (address < maxAddress) {
          val expected = random.nextInt & 0xff
          val res = read8(dut, address)
//          assert(expected == res.toInt)
          address = address + 1
        }
        loops = loops - 1
      }
      println("PASS")
    }

    // Exercise enable and valid signals
    compiled.doSim("Test 5") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period = 10)

      write32(dut, 0, 0xffffffff)
      val res = read32(dut, 0)
      assert(res.toInt == 0xffffffff)

      dut.io.host.enable #= false
      dut.io.host.mem_valid #= false
      dut.clockDomain.waitRisingEdge()
      assert(!dut.io.host.mem_ready.toBoolean)
      assert(dut.io.host.mem_rdata.toInt == 0)

      dut.io.host.enable #= false
      dut.io.host.mem_valid #= true
      dut.clockDomain.waitRisingEdge()
      assert(!dut.io.host.mem_ready.toBoolean)
      assert(dut.io.host.mem_rdata.toInt == 0)

      dut.io.host.enable #= true
      dut.io.host.mem_valid #= false
      dut.clockDomain.waitRisingEdge()
      assert(!dut.io.host.mem_ready.toBoolean)
      assert(dut.io.host.mem_rdata.toInt == 0)

      dut.io.host.enable #= true
      dut.io.host.mem_valid #= true
      dut.clockDomain.waitRisingEdge()
      assert(dut.io.host.mem_ready.toBoolean)
      assert(dut.io.host.mem_rdata.toInt == 0xffffffff)

      println("PASS")
    }
*/
  }
}

