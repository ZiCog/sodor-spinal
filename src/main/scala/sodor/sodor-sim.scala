package Sodor

import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

// sodor's testbench
object SodorSim {
  def main(args: Array[String]) {
    val compiled = SimConfig.withWave.compile{
      val dut = new Sodor ;
      dut.programCounter.io.pc4.simPublic()
      dut.programCounter.io.pc.simPublic()
      dut.programCounter.io.pcNext.simPublic()
      dut.pc.simPublic()
      dut.pcNext.simPublic()
      dut.pc4.simPublic()
      dut.jalr.simPublic()
      dut.branch.simPublic()
      dut.jump.simPublic()
      dut.iTypeImmediate.simPublic()
      dut.sTypeImmediate.simPublic()
      dut.uTypeImmediate.simPublic()
      dut.rs1.simPublic()
      dut.rs2.simPublic()
      dut.op1Sel.simPublic()
      dut.op2Sel.simPublic()
      dut.pcSel.simPublic()
      dut.wbSel.simPublic()
      dut.aluFun.simPublic()
      dut.aluResult.simPublic()
      dut.rfWen.simPublic()
      dut.regFile.wd.simPublic()
      dut.regFile.wa.simPublic()
      dut.rfWen.simPublic()
      dut.brEq.simPublic()
      dut.brLt.simPublic()
      dut.brLtu.simPublic()
      dut.io.dataMemory.addr.simPublic()
      dut
    }

    compiled.doSim("test_ProgramCounter") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var modelPc = 0

      var idx = 0
      while(idx < 10){

        // Drive the dut inputs
        dut.io.instructionMemory.data #= Integer.parseInt("00000000000000000000000000010011", 2)  // NOP (ADDI)

        // Wait a rising edge on the clock
        dut.clockDomain.waitRisingEdge()

        // Check that the dut values match with the reference model ones
        assert(dut.programCounter.io.pc.toInt ==  modelPc)
        assert(dut.programCounter.io.pc4.toInt == modelPc + 4)
        assert(dut.io.instructionMemory.addr.toInt == modelPc)

        modelPc = modelPc + 4
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

    compiled.doSim("test_JumpRegTargetGen") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000011111111111111111111", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("rs1 = ", dut.rs1.toInt)
      println("jalr = ", dut.jalr.toInt)
      assert(dut.rs1.toInt == 0)
      assert(dut.jalr.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111100000000000000000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("rs1 = ", dut.rs1.toInt)
      println("jalr = ", dut.jalr.toInt)
      assert(dut.jalr.toInt == -2)

      // TODO MORE BRANCH target tests required..
    }

    compiled.doSim("test_LW") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000100000000010000110000011", 2) // lw r3, r0, 8
      dut.io.dataMemory.rdata #= 42

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("rs1 = ", dut.rs1.toInt)
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("dataMemory.addr = ", dut.io.dataMemory.addr.toInt)
      println("wbSel = ", dut.wbSel.toInt)
      println("wa = ", dut.regFile.wa.toInt)
      println("wd = ", dut.regFile.wd.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("rw = ", dut.io.dataMemory.rw.toInt)
      println("val = ", dut.io.dataMemory.valid.toInt)

      assert(dut.rs1.toInt == 0)
      assert(dut.op1Sel.toInt  == 0)
      assert(dut.op2Sel.toInt  == 0)
      assert(dut.aluFun.toInt == 0)
      assert(dut.aluResult.toInt == 8)
      assert(dut.io.dataMemory.addr.toInt == 8)
      assert(dut.wbSel.toInt == 0)
      assert(dut.regFile.wa.toInt == 3)
      assert(dut.regFile.wd.toInt == 42)
      assert(dut.rfWen.toInt == 1)
      assert(dut.io.dataMemory.rw.toInt == 0)
      assert(dut.io.dataMemory.valid.toInt == 1)

      // TODO MORE BRANCH load tests required.
    }

    compiled.doSim("test_SW") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs with addi 256 to reg 1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000000000000000010010011", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Drive the dut inputs with sw reg 1 to memory[imm + reg2]
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000100000100000010001000100011", 2) // sw

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones

      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wdata = ", dut.io.dataMemory.wdata.toInt)
      println("rs1 = ", dut.rs1.toInt)
      println("rs2 = ", dut.rs2.toInt)

      assert(dut.op1Sel.toInt  == 0)
      assert(dut.op2Sel.toInt  == 1)
      assert(dut.aluFun.toInt == 0)
      assert(dut.rfWen.toInt == 0)
      assert(dut.io.dataMemory.rw.toInt == 1)
      assert(dut.io.dataMemory.valid.toInt == 1)
      assert(dut.io.dataMemory.addr.toInt == 68)
      assert(dut.aluResult.toInt == 68)
      assert(dut.rs2.toInt == 256)
      assert(dut.io.dataMemory.wdata.toInt == 256)

      // TODO More SW tests required.
    }

    compiled.doSim("test_LUI") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs with LUI R1, 4096
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000001000010110111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      println("LUI signals:")
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wd = ", dut.regFile.wd.toInt)
      println("wa = ", dut.regFile.wa.toInt)
      println("rs1 = ", dut.rs1.toInt)
      println("rs2 = ", dut.rs2.toInt)

      assert(dut.op1Sel.toInt  == 1)
      assert(dut.op2Sel.toInt  == 4)
      assert(dut.aluFun.toInt == 10)
      assert(dut.rfWen.toInt == 1)
      assert(dut.io.dataMemory.rw.toInt == 0)
      assert(dut.io.dataMemory.valid.toInt == 0)
      assert(dut.aluResult.toInt == 4096)
      assert(dut.regFile.wd.toInt == 4096)
      assert(dut.regFile.wa.toInt == 1)

      // Drive the dut inputs with sw reg 1 to memory[imm + reg2]
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000100000100000010001000100011", 2) // sw

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("SW signals:")
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wdata = ", dut.io.dataMemory.wdata.toInt)

      assert(dut.op1Sel.toInt  == 0)
      assert(dut.op2Sel.toInt  == 1)
      assert(dut.aluFun.toInt == 0)
      assert(dut.rfWen.toInt == 0)
      assert(dut.io.dataMemory.rw.toInt == 1)
      assert(dut.io.dataMemory.valid.toInt == 1)
      assert(dut.io.dataMemory.addr.toInt == 68)
      assert(dut.aluResult.toInt == 68)
      assert(dut.io.dataMemory.wdata.toInt == 4096)
    }

    compiled.doSim("test_AUIPC") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Bump the PC up a bit by driving a few NOPs in
      dut.io.instructionMemory.data #= Integer.parseInt("00000000000000000000000000010011", 2)  // NOP (ADDI)
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()

      // Drive the dut inputs with AUIPC R1, 4096
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000001000010010111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      println("AUIPC signals:")
      println("pc = ", dut.pc.toInt)
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wd = ", dut.regFile.wd.toInt)
      println("wa = ", dut.regFile.wa.toInt)
      println("rs1 = ", dut.rs1.toInt)
      println("rs2 = ", dut.rs2.toInt)

      assert(dut.pc.toInt  == 16)
      assert(dut.op1Sel.toInt  == 1)
      assert(dut.op2Sel.toInt  == 2)
      assert(dut.aluFun.toInt == 0)
      assert(dut.rfWen.toInt == 1)
      assert(dut.io.dataMemory.rw.toInt == 0)
      assert(dut.io.dataMemory.valid.toInt == 0)
      assert(dut.aluResult.toInt == 4112)
      assert(dut.regFile.wd.toInt == 4112)
      assert(dut.regFile.wa.toInt == 1)

      // Drive the dut inputs with sw reg 1 to memory[imm + reg2]
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000100000100000010001000100011", 2) // sw

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("SW signals:")
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wdata = ", dut.io.dataMemory.wdata.toInt)

      assert(dut.op1Sel.toInt  == 0)
      assert(dut.op2Sel.toInt  == 1)
      assert(dut.aluFun.toInt == 0)
      assert(dut.rfWen.toInt == 0)
      assert(dut.io.dataMemory.rw.toInt == 1)
      assert(dut.io.dataMemory.valid.toInt == 1)
      assert(dut.io.dataMemory.addr.toInt == 68)
      assert(dut.aluResult.toInt == 68)
      assert(dut.io.dataMemory.wdata.toInt == 4112)
    }

    compiled.doSim("test_JAL") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Bump the PC up a bit by driving a few NOPs in
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000000000000010011", 2)  // NOP (ADDI)
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      println("pc = ", dut.pc.toInt)
      assert(dut.pc.toInt  == 12)

      // Drive the dut inputs with JAL R1, 4096
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000001000011101111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      println("JAL signals:")
      println("pc = ", dut.pc.toInt)
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("jump = ", dut.jump.toInt)
      println("pcNext = ", dut.pcNext.toInt)
      println("wd = ", dut.regFile.wd.toInt)
      println("wa = ", dut.regFile.wa.toInt)
      println("rs1 = ", dut.rs1.toInt)
      println("rs2 = ", dut.rs2.toInt)

      assert(dut.pc.toInt  == 16)
      assert(dut.op1Sel.toInt  == 3)
      assert(dut.op2Sel.toInt  == 4)
      assert(dut.aluFun.toInt == 11)
      assert(dut.rfWen.toInt == 1)
      assert(dut.io.dataMemory.rw.toInt == 0)
      assert(dut.io.dataMemory.valid.toInt == 0)
      assert(dut.jump.toInt == 4112)
      assert(dut.pcNext.toInt == 4112)
      assert(dut.regFile.wd.toInt == 20)
      assert(dut.regFile.wa.toInt == 1)
/*
      // Drive the dut inputs with sw reg 1 to memory[imm + reg2]
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000100000100000010001000100011", 2) // sw

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
      println("SW signals:")
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("aluResult = ", dut.aluResult.toInt)
      println("wdata = ", dut.io.dataMemory.wdata.toInt)

      assert(dut.op1Sel.toInt  == 0)
      assert(dut.op2Sel.toInt  == 1)
      assert(dut.aluFun.toInt == 0)
      assert(dut.rfWen.toInt == 0)
      assert(dut.io.dataMemory.rw.toInt == 1)
      assert(dut.io.dataMemory.valid.toInt == 1)
      assert(dut.io.dataMemory.addr.toInt == 68)
      assert(dut.aluResult.toInt == 68)
      assert(dut.io.dataMemory.wdata.toInt == 4112)
*/
    }

    compiled.doSim("test_JALR") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Bump the PC up a bit by driving a few NOPs in
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000000000000010011", 2)  // NOP (ADDI)
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()
      dut.clockDomain.waitRisingEdge()

      // Drive the dut inputs with addi 256 to reg 1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000000000000000010010011", 2) // addi

      dut.clockDomain.waitRisingEdge()
      println("pc = ", dut.pc.toInt)
      assert(dut.pc.toInt  == 12)

      // Drive the dut inputs with JALR R1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00001000000100001000000011100111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      println("JALR signals:")
      println("pc = ", dut.pc.toInt)
      println("opSel1 = ", dut.op1Sel.toInt)
      println("opSel2 = ", dut.op2Sel.toInt)
      println("aluFun = ", dut.aluFun.toInt)
      println("rfWen = ", dut.rfWen.toInt)
      println("memRw = ", dut.io.dataMemory.rw.toInt)
      println("memVal = ", dut.io.dataMemory.valid.toInt)
      println("addr = ", dut.io.dataMemory.addr.toInt)
      println("jalr = ", dut.jalr.toInt)
      println("pcSel = ", dut.pcSel.toInt)
      println("pcNext = ", dut.pcNext.toInt)
      println("wd = ", dut.regFile.wd.toInt)
      println("wa = ", dut.regFile.wa.toInt)
      println("rs1 = ", dut.rs1.toInt)
      println("rs2 = ", dut.rs2.toInt)

      assert(dut.pc.toInt  == 16)
      assert(dut.op1Sel.toInt  == 3)
      assert(dut.op2Sel.toInt  == 4)
      assert(dut.aluFun.toInt == 11)
      assert(dut.rfWen.toInt == 1)
      assert(dut.io.dataMemory.rw.toInt == 0)
      assert(dut.io.dataMemory.valid.toInt == 0)
      assert(dut.jalr.toInt == 384)
      assert(dut.pcNext.toInt == 384)
      assert(dut.regFile.wd.toInt == 20)
      assert(dut.regFile.wa.toInt == 1)
      /*
            // Drive the dut inputs with sw reg 1 to memory[imm + reg2]
            dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000100000100000010001000100011", 2) // sw

            // Wait a rising edge on the clock
            dut.clockDomain.waitRisingEdge()

            // Check that the dut values match with the reference model ones
            println("SW signals:")
            println("opSel1 = ", dut.op1Sel.toInt)
            println("opSel2 = ", dut.op2Sel.toInt)
            println("aluFun = ", dut.aluFun.toInt)
            println("rfWen = ", dut.rfWen.toInt)
            println("memRw = ", dut.io.dataMemory.rw.toInt)
            println("memVal = ", dut.io.dataMemory.valid.toInt)
            println("addr = ", dut.io.dataMemory.addr.toInt)
            println("aluResult = ", dut.aluResult.toInt)
            println("wdata = ", dut.io.dataMemory.wdata.toInt)

            assert(dut.op1Sel.toInt  == 0)
            assert(dut.op2Sel.toInt  == 1)
            assert(dut.aluFun.toInt == 0)
            assert(dut.rfWen.toInt == 0)
            assert(dut.io.dataMemory.rw.toInt == 1)
            assert(dut.io.dataMemory.valid.toInt == 1)
            assert(dut.io.dataMemory.addr.toInt == 68)
            assert(dut.aluResult.toInt == 68)
            assert(dut.io.dataMemory.wdata.toInt == 4112)
      */
    }

    compiled.doSim("test_BranchCondGen") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs with addi 256 to reg 1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000000000000000010010011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      // Drive the dut inputs with addi 256 to reg 2
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00110000000000000000000100010011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      // Drive branch instructions...

      println("brEqu = ", dut.brEq.toInt)
      println("brLt = ", dut.brLt.toInt)
      println("brLtu = ", dut.brLtu.toInt)
      println("jump = ", dut.jump.toInt)
      println("jalr = ", dut.jalr.toInt)
      println("branch = ", dut.branch.toInt)
      println("pc4 = ", dut.pc4.toInt)
    }
  }
}

