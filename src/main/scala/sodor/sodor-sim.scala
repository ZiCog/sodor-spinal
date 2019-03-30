package sodor

import Array._
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
      dut.alu.io.op1.simPublic()
      dut.alu.io.op2.simPublic()
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
      assert(dut.uTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111111111111000000000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
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
      assert(dut.iTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111111111100000000000000000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
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
      assert(dut.sTypeImmediate.toInt == 0)

      // Drive the dut inputs
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("11111110000000000000111110000000", 2)

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

      // Check that the dut values match with the reference model ones
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
      assert(dut.pc.toInt  == 12)

      // Drive the dut inputs with JAL R1, 4096
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000000000000001000011101111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

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
      assert(dut.pc.toInt  == 12)

      // Drive the dut inputs with JALR R1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00001000000100001000000011100111", 2) // addi

      // Wait a rising edge on the clock
      dut.clockDomain.waitRisingEdge()

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

    compiled.doSim("test_RegFile") { dut =>
      val instructionsLoad = Array (
        ("06400013", "addi    x0,x0,100"),
        ("06500093", "addi    x1,x0,101"),
        ("06600113", "addi    x2,x0,102"),
        ("06700193", "addi    x3,x0,103"),
        ("06800213", "addi    x4,x0,104"),
        ("06900293", "addi    x5,x0,105"),
        ("06a00313", "addi    x6,x0,106"),
        ("06b00393", "addi    x7,x0,107"),
        ("06c00413", "addi    x8,x0,108"),
        ("06d00493", "addi    x9,x0,109"),
        ("06e00513", "addi    x10,x0,110"),
        ("06f00593", "addi    x11,x0,111"),
        ("07000613", "addi    x12,x0,112"),
        ("07100693", "addi    x13,x0,113"),
        ("07200713", "addi    x14,x0,114"),
        ("07300793", "addi    x15,x0,115"),
        ("07400813", "addi    x16,x0,116"),
        ("07500893", "addi    x17,x0,117"),
        ("07600913", "addi    x18,x0,118"),
        ("07700993", "addi    x19,x0,119"),
        ("07800a13", "addi    x20,x0,120"),
        ("07900a93", "addi    x21,x0,121"),
        ("07a00b13", "addi    x22,x0,122"),
        ("07b00b93", "addi    x23,x0,123"),
        ("07c00c13", "addi    x24,x0,124"),
        ("07d00c93", "addi    x25,x0,125"),
        ("07e00d13", "addi    x26,x0,126"),
        ("07f00d93", "addi    x27,x0,127"),
        ("08000e13", "addi    x28,x0,128"),
        ("08100e93", "addi    x29,x0,129"),
        ("08200f13", "addi    x30,x0,130"),
        ("08300f93", "addi    x31,x0,131")
      )
      val instructionsStore = Array (
        ("00002023", "sw      x0,0(x0)"),
        ("00102023", "sw      x1,0(x0)"),
        ("00202023", "sw      x2,0(x0)"),
        ("00302023", "sw      x3,0(x0)"),
        ("00402023", "sw      x4,0(x0)"),
        ("00502023", "sw      x5,0(x0)"),
        ("00602023", "sw      x6,0(x0)"),
        ("00702023", "sw      x7,0(x0)"),
        ("00802023", "sw      x8,0(x0)"),
        ("00902023", "sw      x9,0(x0)"),
        ("00a02023", "sw      x10,0(x0)"),
        ("00b02023", "sw      x11,0(x0)"),
        ("00c02023", "sw      x12,0(x0)"),
        ("00d02023", "sw      x13,0(x0)"),
        ("00e02023", "sw      x14,0(x0)"),
        ("00f02023", "sw      x15,0(x0)"),
        ("01002023", "sw      x16,0(x0)"),
        ("01102023", "sw      x17,0(x0)"),
        ("01202023", "sw      x18,0(x0)"),
        ("01302023", "sw      x19,0(x0)"),
        ("01402023", "sw      x20,0(x0)"),
        ("01502023", "sw      x21,0(x0)"),
        ("01602023", "sw      x22,0(x0)"),
        ("01702023", "sw      x23,0(x0)"),
        ("01802023", "sw      x24,0(x0)"),
        ("01902023", "sw      x25,0(x0)"),
        ("01a02023", "sw      x26,0(x0)"),
        ("01b02023", "sw      x27,0(x0)"),
        ("01c02023", "sw      x28,0(x0)"),
        ("01d02023", "sw      x29,0(x0)"),
        ("01e02023", "sw      x30,0(x0)"),
        ("01f02023", "sw      x31,0(x0)")
      )

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var i = 0
      while (i < instructionsLoad.length) {
        // Drive the dut inputs
        val (hex, asm) = instructionsLoad(i)
        dut.io.instructionMemory.data #= Integer.parseUnsignedInt(hex, 16)
        dut.clockDomain.waitRisingEdge()
        i += 1
      }
      i = 0
      while (i < instructionsStore.length) {
        // Drive the dut inputs
        val (hex, asm) = instructionsStore(i)
        dut.io.instructionMemory.data #= Integer.parseUnsignedInt(hex, 16)
        dut.clockDomain.waitRisingEdge()

        if (i > 0) {
          assert(dut.io.dataMemory.wdata.toInt == i + 100)
        } else {
          assert(dut.io.dataMemory.wdata.toInt == 0)
        }
        i += 1
      }
    }

    compiled.doSim("test_ALU") { dut =>
      val instructions = Array (
        ("02a00093", "addi    x1,x0,42"),
        ("03500113", "addi    x2,x0,53"),
        ("002081b3", "add     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("03500093", "addi    x1,x0,53"),
        ("02a00113", "addi    x2,x0,42"),
        ("402081b3", "sub     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("03500093", "addi    x1,x0,53"),
        ("00200113", "addi    x2,x0,2 "),
        ("002091b3", "sll     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("02a00093", "addi    x1,x0,42"),
        ("03500113", "addi    x2,x0,53"),
        ("0020a1b3", "slt     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("03500093", "addi    x1,x0,53"),
        ("02a00113", "addi    x2,x0,42"),
        ("0020a1b3", "slt     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("fd600093", "addi    x1,x0,-42"),
        ("fcb00113", "addi    x2,x0,-53"),
        ("0020b1b3", "sltu    x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("fcb00093", "addi    x1,x0,-53"),
        ("fd600113", "addi    x2,x0,-42"),
        ("0020b1b3", "sltu    x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("7bc00093", "addi    x1,x0,1980"),
        ("62b00113", "addi    x2,x0,1579"),
        ("0020c1b3", "xor     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("03500093", "addi    x1,x0,53"),
        ("00200113", "addi    x2,x0,2"),
        ("0020d1b3", "srl     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("fcb00093", "addi    x1,x0,-53"),
        ("00200113", "addi    x2,x0,2"),
        ("4020d1b3", "sra     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("05500093", "addi    x1,x0,85"),
        ("0aa00113", "addi    x2,x0,170"),
        ("0020e1b3", "or      x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)"),
        ("7ff00093", "addi    x1,x0,2047"),
        ("05500113", "addi    x2,x0,85"),
        ("0020f1b3", "and     x3,x1,x2"),
        ("00302023", "sw      x3,0(x0)")
      )

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var i = 0
      while (i < instructions.length) {
        // Drive the dut inputs
        val (hex, asm) = instructions(i)
        dut.io.instructionMemory.data #= Integer.parseUnsignedInt(hex, 16)
        dut.clockDomain.waitRisingEdge()

        println(hex, asm)
        println(dut.io.dataMemory.wdata.toInt)
        i += 1
      }
    }

/*
    compiled.doSim("test_BranchCondGen") { dut =>

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      // Drive the dut inputs with addi 256 to reg 1
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000000000000000010010011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      // Drive the dut inputs with addi 256 to reg 2
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000000000000000100010011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      // Drive branch instructions...BEQ
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000001000001000000001100011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      assert(dut.rs1.toInt == 256)
      assert(dut.rs2.toInt == 256)
      assert(dut.brEq.toInt == 1)

      // Drive the dut inputs with addi 257 to reg 2
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00010000000100000000000100010011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      // Drive branch instructions...BEQ
      dut.io.instructionMemory.data #= Integer.parseUnsignedInt("00000000001000001000000001100011", 2) // addi
      dut.clockDomain.waitRisingEdge()

      assert(dut.rs1.toInt == 256)
      assert(dut.rs2.toInt == 256)
      assert(dut.brEq.toInt == 1)

    }
*/
  }
}

