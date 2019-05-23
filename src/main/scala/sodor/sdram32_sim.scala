package sodor

import Array._
import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random




object Sdram32_sim {

  def printDut (dut : Sdram32) = {
    print(dut.io.host.mem_valid.toBoolean)
    print("\t\t")
    print(dut.io.sdram.wr_addr.toInt)
    print("\t")
    print(dut.io.sdram.wr_data.toInt.toHexString.toUpperCase)
    print("\t")
    print(dut.io.sdram.wr_enable.toBoolean)
    print("\t\t")
    print(dut.io.sdram.rd_addr.toInt)
    print("\t")
    print(dut.io.sdram.rd_data.toInt.toHexString.toUpperCase)
    print("\t")
    print(dut.io.sdram.rd_enable.toBoolean)
    print("\t\t")
    print(dut.io.sdram.rd_ready.toBoolean)
    print("\t\t")
    print(dut.io.sdram.busy.toBoolean)
    print("\t\t")
    print(dut.io.host.mem_ready.toBoolean)
    print("\t\t")
    print(dut.io.host.mem_rdata.toInt.toHexString.toUpperCase)
    println()
  } 

  def write32 (dut : Sdram32, address: Int, data: Int) = {
    assert((address & 0x03) == 0)

    println("mem_valid\twr_addr\twr_data\twr_enable\trd_addr\trd_data\trd_enable\trd_ready\tbusy\t\tmem_ready")
    printDut(dut)

    // Drive inputs for write operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0xF
    dut.io.host.mem_wdata #= data
    dut.io.host.mem_addr #= address

    // Drive SDRAM inputs
    dut.io.sdram.rd_data #= 32000 
    dut.io.sdram.rd_ready #= false
    dut.io.sdram.busy #= false

    dut.clockDomain.waitRisingEdge()
    printDut(dut)

    println ("Set busy")
    dut.io.sdram.busy #= true
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
/*
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
*/
    println ("Clear busy")
    dut.io.sdram.busy #= false
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(dut.io.sdram.wr_enable.toBoolean == false)

    dut.clockDomain.waitRisingEdge()
    printDut(dut)

    println ("Set busy")
    dut.io.sdram.busy #= true
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
/*
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
*/
    println ("Clear busy")
    dut.io.sdram.busy #= false
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(dut.io.sdram. wr_enable.toBoolean == false)
    assert(dut.io.host.mem_ready.toBoolean)

    // Drive inputs to idle again
    println ("Clear write request")
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(!dut.io.host.mem_ready.toBoolean)
    assert(dut.io.host.mem_rdata.toInt == 0)
  }

  def read32 (dut : Sdram32, address: Int, expect: Int) = {
    assert((address & 0x03) == 0)

    println("mem_valid\twr_addr\twr_data\twr_enable\trd_addr\trd_data\trd_enable\trd_ready\tbusy\t\tmem_ready\tmem_rdata")
    printDut(dut)

    // Drive inputs for read operation.
    dut.io.host.enable #= true
    dut.io.host.mem_valid #= true
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= address

    // Drive SDRAM inputs
    dut.io.sdram.rd_data #= 0
    dut.io.sdram.rd_ready #= false
    dut.io.sdram.busy #= false

    dut.clockDomain.waitRisingEdge()
    printDut(dut)

    println ("Set busy")
    dut.io.sdram.busy #= true
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    /*
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
    */
    println ("Set the read low data and clear busy")
    dut.io.sdram.rd_data #= expect & 0xffff
    dut.io.sdram.busy #= false
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(dut.io.sdram.wr_enable.toBoolean == false)

    dut.clockDomain.waitRisingEdge()
    printDut(dut)

    println ("Set busy")
    dut.io.sdram.busy #= true
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    /*
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
        dut.clockDomain.waitRisingEdge()
        printDut(dut)
    */
    println ("Set the high read data clear busy")
    dut.io.sdram.rd_data #= (expect >> 16) & 0xffff
    dut.io.sdram.busy #= false
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(dut.io.sdram. wr_enable.toBoolean == false)
    assert(dut.io.host.mem_ready.toBoolean)

    // Capture the word read
    val result = dut.io.host.mem_rdata.toInt

    // Drive inputs to idle again
    println ("Clear write request")
    dut.io.host.enable #= false
    dut.io.host.mem_valid #= false
    dut.io.host.mem_instr #= false
    dut.io.host.mem_wstrb #= 0
    dut.io.host.mem_wdata #= 0
    dut.io.host.mem_addr #= 0
    dut.clockDomain.waitRisingEdge()
    printDut(dut)
    assert(dut.io.host.mem_ready.toBoolean == false)
    assert(dut.io.host.mem_rdata.toInt == 0)

    result
  }

  def main(args: Array[String]) {

    val width = 32
    val depth = 16 * 1024 * 1024
    val maxAddress = 1
    
    val compiled = SimConfig.withWave.compile {
      val dut = new Sdram32(width, depth)
      dut
    }

    // Verify 32 bit write cycles
    compiled.doSim("Test 1") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period   = 10)

      // Drive inputs to idle
      dut.io.host.enable #= false
      dut.io.host.mem_valid #= false
      dut.io.host.mem_instr #= false
      dut.io.host.mem_wstrb #= 0
      dut.io.host.mem_wdata #= 0
      dut.io.host.mem_addr #= 0
      dut.clockDomain.waitRisingEdge()

      var address = 0
      var data = 0x22221111
      write32(dut, address, data)

      address = address + 4
      data = 0x44443333
      write32(dut, address, data)

      address = address + 4
      data = 0x66665555
      write32(dut, address, data)

      println("PASS")
    }

    // Verify 32 bit read cycles
    compiled.doSim("Test 2") { dut =>

      // Fork a process to generate the reset and the clock on the DUT
      dut.clockDomain.forkStimulus(period   = 10)

      // Drive inputs to idle
      dut.io.host.enable #= false
      dut.io.host.mem_valid #= false
      dut.io.host.mem_instr #= false
      dut.io.host.mem_wstrb #= 0
      dut.io.host.mem_wdata #= 0
      dut.io.host.mem_addr #= 0
      dut.clockDomain.waitRisingEdge()

      var address = 0
      var expect = 0x66665555
      var res = read32(dut, address, expect)
      assert(res == expect)

      address = 4
      expect = 0x88887777
      res = read32(dut, address, expect)
      assert(res == expect)

      println("PASS")
    }
  }
}

