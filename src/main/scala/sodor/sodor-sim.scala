package Sodor

import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

// sodor's testbench
object SodorSim {
/*
  def main(args: Array[String]) {
    SimConfig.withWave.doSim(new ProgramCounter){dut =>
      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var modelPcNext = 0;

      var idx = 0
      while(idx < 100){

        // Drive the dut inputs
        dut.io.pcNext #= modelPcNext

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()
        dut.clockDomain.waitFallingEdge()

        // Check that the dut values match with the reference model ones
        println(modelPcNext, dut.io.pcNext.toInt, dut.io.pc.toInt, dut.io.pc4.toInt)
    //    assert(dut.io.pc.toInt == modelPcNext)
     //   assert(dut.io.pc4.toInt == modelPcNext + 4)

        modelPcNext = modelPcNext + 4

        idx += 1
      }
    }
  }
*/
  def main(args: Array[String]) {
    val compiled = SimConfig.withWave.compile{
      val dut = new Sodor ;
      dut.counter1.count.simPublic()
      dut.programCounter.io.pc4.simPublic()
      dut.programCounter.io.pc.simPublic()
      dut.programCounter.io.pcNext.simPublic()
      dut.pc.simPublic()
      dut.jalr.simPublic()
      dut.branch.simPublic()
      dut.jump.simPublic()
      dut.iTypeImmediate.simPublic()
      dut.sTypeImmediate.simPublic()
      dut.uTypeImmediate.simPublic()
      dut.rs1.simPublic()
      dut.rs2.simPublic()
      dut
    }

    compiled.doSim("testA") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      while (idx < 10) {

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()
        dut.clockDomain.waitFallingEdge()

        // Check that the dut values match with the reference model ones
        println(dut.counter1.count.toInt)
        //    assert(dut.io.pc.toInt == modelPcNext)
        //   assert(dut.io.pc4.toInt == modelPcNext + 4)

        idx += 1
      }
    }

    compiled.doSim("test_ProgramCounter") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var idx = 0
      while(idx < 10){

        // Drive the dut inputs
        dut.io.instructionMemory.data #= Integer.parseInt("00000000000000000000000000010011", 2)  // NOP (ADDI)

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()

        // Check that the dut values match with the reference model ones
        println("pc = ", dut.programCounter.io.pc.toInt)
        println("pc4 = ", dut.programCounter.io.pc4.toInt)
        println("iTypeImmediate = ", dut.iTypeImmediate.toInt)
        //    assert(dut.io.pc.toInt == modelPcNext)
        //   assert(dut.io.pc4.toInt == modelPcNext + 4)

        idx += 1
      }
    }

    compiled.doSim("test_UType") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000000111111111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("uType = ", dut.uTypeImmediate.toInt)
      assert(dut.uTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111111111111000000000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("uType = ", dut.uTypeImmediate.toInt)
      assert(dut.uTypeImmediate.toInt == -4096)
    }

    compiled.doSim("test_ITypeSignExtend") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000011111111111111111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("iType = ", dut.iTypeImmediate.toInt)
      assert(dut.iTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111100000000000000000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("iType = ", dut.iTypeImmediate.toInt)
      assert(dut.iTypeImmediate.toInt == -1)
    }

    compiled.doSim("test_STypeSignExtend") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000001111111111111000001111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("iType = ", dut.sTypeImmediate.toInt)
      assert(dut.sTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111110000000000000111110000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("sType = ", dut.sTypeImmediate.toInt)
      assert(dut.sTypeImmediate.toInt == -1)
    }

    compiled.doSim("test_JumpTargetGen")
    { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000000111111111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("pc = ", dut.programCounter.io.pc.toInt)
      println("jump = ", dut.jump.toInt)
      assert(dut.pc.toInt == 0)
      assert(dut.jump.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111111111111000000000000", 2)

      // Advance PC to 32 by waiting 8 clocks.
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("pc = ", dut.programCounter.io.pc.toInt)
      println("jump = ", dut.jump.toInt)
      assert(dut.pc.toInt == 32)
      assert(dut.jump.toInt == 30)

      // TODO MORE JAL target tests required..
    }

    compiled.doSim("test_BranchTargetGen")
    { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000001111111111111000001111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("pc = ", dut.programCounter.io.pc.toInt)
      println("branch = ", dut.branch.toInt)
      assert(dut.pc.toInt == 0)
      assert(dut.branch.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111110000000000000111110000000", 2)

      // Advance PC to 32 by waiting 8 clocks.
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("pc = ", dut.programCounter.io.pc.toInt)
      println("branch = ", dut.branch.toInt)
      assert(dut.pc.toInt == 32)
      assert(dut.branch.toInt == 30)

      // TODO MORE BRANCH target tests required..
    }
  }
}

