package Sodor

import spinal.core._
import spinal.lib._
import scala.util.Random

// The Sodor 1 stage RISC-V

// Allowed values of pcSel
object pcSelections extends SpinalEnum {
  val pcP4, jalr, branch, jump, exception = newElement()
}

// Branch types
object BR extends SpinalEnum {
  val N, J, JR, EQ, NE, GE, GEU, LT, LTU = newElement()
}

// Allowed values of op1Sel
object OP1 extends SpinalEnum {
  val RS1, IMU, IMZ, X = newElement()
}

// Allowed values of op2Sel
object OP2 extends SpinalEnum {
  val IMI, IMS, PC, RS2, X = newElement()
}

// Allowed values of aluFun
object ALU extends SpinalEnum {
  val ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND, COPY1,  X = newElement()
}

// Allowed values of wbSel
object WB extends SpinalEnum {
  val MEM, ALU, PC4, CSR, X = newElement()
}

object M extends SpinalEnum {
  val XRD, XWR, X = newElement()
}

object MT extends SpinalEnum {
  val W, B, BU, H, HU, X = newElement()
}

object CSR extends SpinalEnum {
  val N, W, S, C, I = newElement()
}

object opCodeSelections extends SpinalEnum {
  val lui, auipc, jal, jalr, branch, load, store, opImm, op, miscMem, system = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    lui     -> Integer.parseInt("0110111", 2),
    auipc   -> Integer.parseInt("0010111", 2),
    jal     -> Integer.parseInt("1101111", 2),
    jalr    -> Integer.parseInt("1100111", 2),
    branch  -> Integer.parseInt("1100011", 2),
    load    -> Integer.parseInt("0000011", 2),
    store   -> Integer.parseInt("0100011", 2),
    opImm   -> Integer.parseInt("0010011", 2),
    op      -> Integer.parseInt("0110011", 2),
    miscMem -> Integer.parseInt("0001111", 2),
    system  -> Integer.parseInt("1110011", 2)
  )
}

object branchFunct3 extends SpinalEnum {
  val beq, bne, blt, bge, bltu, bgeu = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    beq  -> Integer.parseInt("000", 2),
    bne  -> Integer.parseInt("001", 2),
    blt  -> Integer.parseInt("100", 2),
    bge  -> Integer.parseInt("101", 2),
    bltu -> Integer.parseInt("110", 2),
    bgeu -> Integer.parseInt("111", 2)
  )
}

object loadFunct3 extends SpinalEnum {
  val lb, lh, lw, lbu, lhu = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    lb  -> Integer.parseInt("000", 2),
    lh  -> Integer.parseInt("001", 2),
    lw  -> Integer.parseInt("010", 2),
    lbu -> Integer.parseInt("100", 2),
    lhu -> Integer.parseInt("101", 2)
  )
}

object storeFunct3 extends SpinalEnum {
  val sb, sh, sw, dummy = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    sb    -> Integer.parseInt("000", 2),
    sh    -> Integer.parseInt("001", 2),
    sw    -> Integer.parseInt("010", 2),
    dummy -> Integer.parseInt("111", 2)   // This is only here to force this enum to 3 bits width.
  )
}

object opImmFunct3x extends SpinalEnum {
  val addi, slti, sltiu, xori, ori, andi, slli, srli, srai = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    addi  -> Integer.parseInt("0000", 2),
    slti  -> Integer.parseInt("0010", 2),
    sltiu -> Integer.parseInt("0011", 2),
    xori  -> Integer.parseInt("0100", 2),
    ori   -> Integer.parseInt("0110", 2),
    andi  -> Integer.parseInt("0111", 2),
    slli  -> Integer.parseInt("0001", 2),
    srli  -> Integer.parseInt("0101", 2),
    srai  -> Integer.parseInt("1101", 2)
  )
}

object opFunct3x extends SpinalEnum {
  val add, sub, sll, slt, sltu, xor, srl, sra, or, and = newElement()
  defaultEncoding = SpinalEnumEncoding("staticEncoding")(
    add  -> Integer.parseInt("0000", 2),
    sub  -> Integer.parseInt("1000", 2),
    sll  -> Integer.parseInt("0001", 2),
    slt  -> Integer.parseInt("0010", 2),
    sltu -> Integer.parseInt("0011", 2),
    xor  -> Integer.parseInt("0100", 2),
    srl  -> Integer.parseInt("0101", 2),
    sra  -> Integer.parseInt("1101", 2),
    or   -> Integer.parseInt("0110", 2),
    and  -> Integer.parseInt("0111", 2)
  )
}

class PcMux extends Component {
  val io = new Bundle {
    val pcSel = in Bits(3 bits)
    val pc4 = in SInt(32 bits)
    val jalr = in SInt(32 bits)
    val branch = in SInt(32 bits)
    val jump = in SInt(32 bits)
    val exception = in SInt(32 bits)
    val pc = out SInt(32 bits)
  }
  switch(io.pcSel) {
    is(pcSelections.pcP4.asBits) {
      io.pc := io.pc4
    }
    is(pcSelections.jalr.asBits) {
      io.pc := io.jalr
    }
    is(pcSelections.branch.asBits) {
      io.pc := io.branch
    }
    is(pcSelections.jump.asBits) {
      io.pc := io.jump
    }
    is(pcSelections.exception.asBits) {
      io.pc := io.exception
    }
    default {
      io.pc := io.pc4
    }
  }
}

class BranchTargetGen extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val pc = in SInt(32 bits)
    val branch = out SInt(32 bits)
  }

  val branchSignExtension = Bits(20 bits)
  branchSignExtension.setAllTo(io.instruction(31))
  val bTypeImmediate =  S(branchSignExtension ## io.instruction(7) ## io.instruction(30 downto 25) ## io.instruction(11 downto 8) ## B"0")
  io.branch := io.pc + bTypeImmediate
}

class JumpTargetGen extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val pc = in SInt(32 bits)
    val jump = out SInt(32 bits)
  }
  val jSignExtension = Bits(12 bits)
  jSignExtension.setAllTo(io.instruction(31))
  val jTypeImmediate = S(jSignExtension ## io.instruction(19 downto 12) ## io.instruction(20) ## io.instruction(30 downto 21) ## B"0")
  io.jump := io.pc + jTypeImmediate
}

class ITypeSignExtend extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val iTypeImmediate = out SInt(32 bits)
  }
  val iSignExtension = Bits(21 bits)
  iSignExtension.setAllTo(io.instruction(31))
  io.iTypeImmediate := S(iSignExtension ## io.instruction(30 downto 20))
}

class JumpRegTargetGen extends Component {
  val io = new Bundle {
    val iTypeImmediate = in SInt (32 bits)
    val rs1 = in SInt (32 bits)
    val jalr = out SInt (32 bits)
  }
  io.jalr := io.iTypeImmediate + io.rs1
}

class STypeSignExtend extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val sTypeImmediate = out SInt (32 bits)
  }
  val sSignExtension = Bits(21 bits)
  sSignExtension.setAllTo(io.instruction(31))
  io.sTypeImmediate := S(sSignExtension ## io.instruction(30 downto 25) ## io.instruction(11 downto 7))
}

class UType extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val uTypeImmediate = out SInt (32 bits)
  }
  io.uTypeImmediate := S(io.instruction(31 downto 12) ## B(0, 12 bits))
}

class RegFile extends Component {
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val wd = in SInt(32 bits)
    val en = in Bool
    val rs1 = out SInt(32 bits)
    val rs2 = out SInt(32 bits)
  }
  val rs1Address = io.instruction(19 downto 15)
  val rs2Address = io.instruction(24 downto 20)
  val regFile = Mem(UInt(32.bits), 32) init Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  io.rs1 := S(regFile.readAsync(address = rs1Address))
  io.rs2 := S(regFile.readAsync(address = rs2Address))

  val wa = io.instruction(11 downto 7)
  val rfWen = True
  regFile.write(wa, U(io.wd), io.en)
}

class Op1Mux extends Component {
  val io = new Bundle {
    val op1Sel = in Bits(2 bits)
    val rs1 = in SInt(32 bits)
    val uType = in SInt(32 bits)
    val op1 = out SInt(32 bits)
  }
  switch(io.op1Sel) {
    is(OP1.RS1.asBits) {
      io.op1 := io.rs1
    }
    is(OP1.IMU.asBits) {
      io.op1 := io.uType
    }
    default {
      io.op1 := io.rs1
    }
  }
}

class Op2Mux extends Component {
  val io = new Bundle {
    val op2Sel = in Bits(3 bits)
    val rs2 = in SInt(32 bits)
    val sType = in SInt(32 bits)
    val iType = in SInt(32 bits)
    val pc = in SInt(32 bits)
    val op2 = out SInt(32 bits)
  }
  switch(io.op2Sel) {
    is(OP2.RS2.asBits) {
      io.op2 := io.rs2
    }
    is(OP2.IMS.asBits) {
      io.op2 := io.sType
    }
    is(OP2.IMI.asBits) {
      io.op2 := io.iType
    }
    is(OP2.PC.asBits) {
      io.op2 := io.pc
    }
    default {
      io.op2 := io.rs2
    }
  }
}

class Alu extends Component {
  val io = new Bundle {
    val op1 = in SInt(32 bits)
    val op2 = in SInt(32 bits)
    val aluFun = in Bits(4 bits)
    val alu = out SInt(32 bits)
  }
  switch(io.aluFun) {
    is (ALU.ADD.asBits) {
      io.alu := io.op1 + io.op2
    }
    is (ALU.SUB.asBits) {
      io.alu := io.op1 - io.op2
    }
    is (ALU.SLL.asBits) {
      io.alu := io.op1 |<< U(io.op2)
    }
    is (ALU.SLT.asBits) {
      when(io.op1 < io.op2) {
        io.alu := 1
      }.otherwise {
        io.alu := 0
      }
    }
    is (ALU.SLTU.asBits) {
      when(U(io.op1) < U(io.op2)) {
        io.alu := 1
      }.otherwise {
        io.alu := 0
      }
    }
    is (ALU.XOR.asBits) {
      io.alu := io.op1 ^ io.op2
    }
    is (ALU.SRL.asBits) {
      io.alu := io.op1 |>> U(io.op2)
    }
    is (ALU.SRA.asBits) {
      io.alu := io.op1 >> U(io.op2)
    }
    is (ALU.OR.asBits) {
      io.alu := io.op1 | io.op2
    }
    is (ALU.AND.asBits) {
      io.alu := io.op1 & io.op2
    }
    default {
      io.alu := io.op1
    }
  }
}

class WbMux extends Component {
  val io = new Bundle {
    val wbSel = in Bits(3 bits)
    val rdata = in SInt(32 bits)
    val alu = in SInt(32 bits)
    val pc4 = in SInt(32 bits)
    val csr = in SInt(32 bits)
    val wb = out SInt(32 bits)
  }
  switch(io.wbSel) {
    is( WB.MEM.asBits) {
      io.wb := io.rdata
    }
    is(WB.ALU.asBits) {
      io.wb := io.alu
    }
    is(WB.PC4.asBits) {
      io.wb := io.pc4
    }
    is(WB.CSR.asBits) {
      io.wb := io.csr
    }
    default {
      io.wb := io.alu
    }
  }
}

class Decode extends Component {
  // Decode RISC V instructions (The Hard Way!)
  val io = new Bundle {
    val instruction = in UInt(32 bits)
    val pcSel = out Bits (3 bits)
    val aluFun = out Bits (4 bits)
    val op1Sel = out Bits (2 bits)
    val op2Sel = out Bits (3 bits)
    val wbSel = out Bits (3 bits)
    val rfWen = out Bool
    val memVal = out Bool
    val memRw = out Bool
  }
  val opCode = B(io.instruction(6 downto 0))
  val funct3 = B(io.instruction(14 downto 12))
  val funct3x = B(io.instruction(31)) ## B(io.instruction(14 downto 12))

  val controlSignals = Map (
              //  val   | BR    | op1     | op2     | ALU       | wb     | rf    | mem   | mem   | mask  | csr   |
              //  inst  | type  | sel     | sel     | fcn       | sel    | wen   | en    | wr    | type  | cmd   |
    "INVALID" -> (False , BR.N  , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "LW"      -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.MEM , True  , True  , False , MT.W  , CSR.N),
    "LB"      -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.MEM , True  , True  , False , MT.B  , CSR.N),
    "LBU"     -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.MEM , True  , True  , False , MT.BU , CSR.N),
    "LH"      -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.MEM , True  , True  , False , MT.H  , CSR.N),
    "LHU"     -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.MEM , True  , True  , False , MT.HU , CSR.N),
    "SW"      -> (True  , BR.N  , OP1.RS1 , OP2.IMS , ALU.ADD   , WB.X   , False , True  , True  , MT.W  , CSR.N),
    "SB"      -> (True  , BR.N  , OP1.RS1 , OP2.IMS , ALU.ADD   , WB.X   , False , True  , True  , MT.B  , CSR.N),
    "SH"      -> (True  , BR.N  , OP1.RS1 , OP2.IMS , ALU.ADD   , WB.X   , False , True  , True  , MT.H  , CSR.N),

    "AUIPC"   -> (True  , BR.N  , OP1.IMU , OP2.PC  , ALU.ADD   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "LUI"     -> (True  , BR.N  , OP1.IMU , OP2.X   , ALU.COPY1 , WB.ALU , True  , False , False , MT.X  , CSR.N),

    "ADDI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.ADD   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "ANDI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.AND   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "ORI"     -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.OR    , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "XORI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.XOR   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SLTI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.SLT   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SLTIU"   -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.SLTU  , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SLLI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.SLL   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SRAI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.SRA   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SRLI"    -> (True  , BR.N  , OP1.RS1 , OP2.IMI , ALU.SRL   , WB.ALU , True  , False , False , MT.X  , CSR.N),

    "SLL"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SLL   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "ADD"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.ADD   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SUB"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SUB   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SLT"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SLT   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SLTU"    -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SLTU  , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "AND"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.AND   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "OR"      -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.OR    , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "XOR"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.XOR   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SRA"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SRA   , WB.ALU , True  , False , False , MT.X  , CSR.N),
    "SRL"     -> (True , BR.N   , OP1.RS1 , OP2.RS2 , ALU.SRL   , WB.ALU , True  , False , False , MT.X  , CSR.N),

    "JAL"     -> (True , BR.J   , OP1.X   , OP2.X   , ALU.X     , WB.PC4 , True  , False , False , MT.X  , CSR.N),
    "JALR"    -> (True , BR.JR  , OP1.RS1 , OP2.IMI , ALU.X     , WB.PC4 , True  , False , False , MT.X  , CSR.N),
    "BEQ"     -> (True , BR.EQ  , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "BNE"     -> (True , BR.NE  , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "BGE"     -> (True , BR.GE  , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "BGEU"    -> (True , BR.GEU , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "BLT"     -> (True , BR.LT  , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),
    "BLTU"    -> (True , BR.LTU , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False , False , MT.X  , CSR.N),

    "CSRRWI"  -> (True , BR.N   , OP1.IMZ , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.W),
    "CSRRSI"  -> (True , BR.N   , OP1.IMZ , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.S),
    "CSRRCI"  -> (True , BR.N   , OP1.IMZ , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.C),
    "CSRRW"   -> (True , BR.N   , OP1.RS1 , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.W),
    "CSRRS"   -> (True , BR.N   , OP1.RS1 , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.S),
    "CSRRC"   -> (True , BR.N   , OP1.RS1 , OP2.X   , ALU.COPY1 , WB.CSR , True  , False , False,  MT.X  , CSR.C),

    "ECALL"   -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.I),
    "MRET"    -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.I),
    "DRET"    -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.I),
    "EBREAK"  -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.I),
    "WFI"     -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.N), // implemented as a NOP

    "FENCE.I" -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , False, False , MT.X   , CSR.N),
    "FENCE"   -> (True , BR.N   , OP1.X   , OP2.X   , ALU.X     , WB.X   , False , True,  False , MT.X   , CSR.N)
    // we are already sequentially consistent, so no need to honor the fence instruction
  )

  def lookupControlSignals(s: String ): UInt = {
    val (insValid, branchType, op1Sel, op2Sel, aluFun, wbSel, rfWen, memEnable, memWr, memMask, csrCmd) = controlSignals(s)
    io.aluFun := aluFun.asBits
    io.pcSel := 0
    io.op1Sel := op1Sel.asBits
    io.op2Sel := op2Sel.asBits
    io.wbSel := wbSel.asBits
    io.rfWen := rfWen
    io.memVal := memEnable
    io.memRw := memWr
    return 0
  }

  lookupControlSignals("INVALID")

  // Decode (The Hard Way!)
  switch(opCode) {
    is(opCodeSelections.lui.asBits) {
      lookupControlSignals("LUI")
    }
    is(opCodeSelections.auipc.asBits) {
      lookupControlSignals("AUIPC")
    }
    is(opCodeSelections.jal.asBits) {
      lookupControlSignals("JAL")
    }
    is(opCodeSelections.jalr.asBits) {
      lookupControlSignals("JALR")
    }
    is(opCodeSelections.branch.asBits) {
      switch(funct3) {
        is (branchFunct3.beq.asBits) {
          lookupControlSignals("BEQ")
        }
        is (branchFunct3.bne.asBits) {
          lookupControlSignals("BNE")
        }
        is (branchFunct3.blt.asBits) {
          lookupControlSignals("BLT")
        }
        is (branchFunct3.bge.asBits) {
          lookupControlSignals("BGE")
        }
        is (branchFunct3.bltu.asBits) {
          lookupControlSignals("BLTU")
        }
        is (branchFunct3.bgeu.asBits) {
          lookupControlSignals("BGEU")
        }
      }
    }
    is(opCodeSelections.load.asBits) {
      switch(funct3) {
        is (loadFunct3.lb.asBits) {
          lookupControlSignals("LB")
        }
        is (loadFunct3.lh.asBits) {
          lookupControlSignals("LH")
        }
        is (loadFunct3.lw.asBits) {
          lookupControlSignals("LW")
        }
        is (loadFunct3.lbu.asBits) {
          lookupControlSignals("LBU")
        }
        is (loadFunct3.lhu.asBits) {
          lookupControlSignals("LHU")
        }
      }
    }
    is(opCodeSelections.store.asBits) {
      switch(funct3) {
        is (storeFunct3.sb.asBits) {
          lookupControlSignals("SB")
        }
        is (storeFunct3.sh.asBits) {
          lookupControlSignals("SH")
        }
        is (storeFunct3.sw.asBits) {
          lookupControlSignals("SW")
        }
      }
    }
    is(opCodeSelections.opImm.asBits) {
      switch(funct3x) {
        is (opImmFunct3x.addi.asBits) {
          lookupControlSignals("ADDI")
        }
        is (opImmFunct3x.slti.asBits) {
          lookupControlSignals("SLTI")
        }
        is (opImmFunct3x.sltiu.asBits) {
          lookupControlSignals("SLTIU")
        }
        is (opImmFunct3x.xori.asBits) {
          lookupControlSignals("XORI")
        }
        is (opImmFunct3x.ori.asBits) {
          lookupControlSignals("ORI")
        }
        is (opImmFunct3x.andi.asBits) {
          lookupControlSignals("ANDI")
        }
        is (opImmFunct3x.slli.asBits) {
          lookupControlSignals("SLLI")
        }
        is (opImmFunct3x.srli.asBits) {
          lookupControlSignals("SRLI")
        }
        is (opImmFunct3x.srai.asBits) {
          lookupControlSignals("SRAI")
        }
      }
    }
    is(opCodeSelections.op.asBits) {
      switch(funct3x) {
        is (opFunct3x.add.asBits) {
          lookupControlSignals("ADD")
        }
        is (opFunct3x.sub.asBits) {
          lookupControlSignals("SUB")
        }
        is (opFunct3x.sll.asBits) {
          lookupControlSignals("SLL")
        }
        is (opFunct3x.slt.asBits) {
          lookupControlSignals("SLT")
        }
        is (opFunct3x.sltu.asBits) {
          lookupControlSignals("SLTU")
        }
        is (opFunct3x.xor.asBits) {
          lookupControlSignals("XOR")
        }
        is (opFunct3x.srl.asBits) {
          lookupControlSignals("SRL")
        }
        is (opFunct3x.sra.asBits) {
          lookupControlSignals("SRA")
        }
        is (opFunct3x.or.asBits) {
          lookupControlSignals("OR")
        }
        is (opFunct3x.and.asBits) {
          lookupControlSignals("AND")
        }
      }
    }
    is(opCodeSelections.miscMem.asBits) {
    }
    is(opCodeSelections.system.asBits) {
    }
  }
}

class Sodor extends Component {
  val io = new Bundle {
    val exception = in SInt(32.bits)
    val instructionMemory = new Bundle {
      val addr = out SInt(32.bits)
      val data = in SInt(32.bits)
      val valid = out Bool
    }
    val dataMemory = new Bundle {
      val addr = out SInt(32.bits)
      val wdata = out SInt(32.bits)
      val rdata = in SInt(32.bits)
      val rw = out Bool
      val valid = out Bool
    }
    val coprocessor = new Bundle {
      val csr = in SInt(32.bits)
    }
  }
  // The Program Counter
  val pc = Reg(SInt(32.bits)) init 0

  // Program Counter plus 4
  val pc4 = SInt(32.bits)

  // Data path signals
  val branch = SInt(32.bits)
  val jump = SInt(32.bits)
  val jalr = SInt(32.bits)
  val rs1 = SInt(32.bits)
  val rs2 = SInt(32.bits)
  val op1 = SInt(32.bits)
  val op2 = SInt(32.bits)
  val aluResult = SInt(32.bits)
  val wd = SInt(32.bits)

  // Control signals.
  val pcSel = Bits (3 bits)
  val op1Sel = Bits (2 bits)
  val op2Sel = Bits (3 bits)
  val aluFun = Bits (4 bits)
  val wbSel = Bits (3 bits)
  val memRw = Bool
  val memVal = Bool
  val rfWen = Bool

  val instruction = UInt(32.bits)

  // Location of next instruction
  pc4 := pc + 4

  io.instructionMemory.addr := pc
  io.instructionMemory.valid := True

  val pcMux = new PcMux
  pcMux.io.pc4 := pc4
  pcMux.io.jalr := jalr
  pcMux.io.branch := branch
  pcMux.io.jump := jump
  pcMux.io.exception := io.exception
  pcMux.io.pcSel := pcSel.asBits
  pc := pcMux.io.pc

  instruction := U(io.instructionMemory.data)

  // BranchTargetGen
  val branchTargetGen = new BranchTargetGen
  branchTargetGen.io.instruction := instruction
  branchTargetGen.io.pc := pc
  branch := branchTargetGen.io.branch

  // J-Type, JumpTargetGen
  val jumpTargetGen = new JumpTargetGen
  jumpTargetGen.io.instruction := instruction
  jumpTargetGen.io.pc := pc
  jump := jumpTargetGen.io.jump

  val iTypeSignExtend = new ITypeSignExtend
  iTypeSignExtend.io.instruction := instruction
  val iTypeImmediate = iTypeSignExtend.io.iTypeImmediate

  val jumpRegTargetGen = new JumpRegTargetGen
  jumpRegTargetGen.io.iTypeImmediate := iTypeImmediate
  jumpRegTargetGen.io.rs1 := rs1
  jalr := jumpRegTargetGen.io.jalr

  val sTypeSignExtend = new STypeSignExtend
  sTypeSignExtend.io.instruction := instruction
  val sTypeImmediate = sTypeSignExtend.io.sTypeImmediate

  val uType = new UType
  uType.io.instruction := instruction
  val uTypeImmediate = uType.io.uTypeImmediate

  val regFile = new RegFile
  regFile.io.instruction := instruction
  regFile.io.wd := wd
  regFile.io.en := True           // FIXME Control Signal
  rs1 := regFile.io.rs1
  rs2 := regFile.io.rs2

  val op1Mux = new Op1Mux
  op1Mux.io.uType := uTypeImmediate
  op1Mux.io.rs1 := rs1
  op1Mux.io.op1Sel := op1Sel.asBits
  op1 := op1Mux.io.op1

  val op2Mux = new Op2Mux
  op2Mux.io.rs2 := rs2
  op2Mux.io.sType := sTypeImmediate
  op2Mux.io.iType := iTypeImmediate
  op2Mux.io.pc := pc
  op2Mux.io.op2Sel := op2Sel.asBits
  op2 := op2Mux.io.op2

  val alu = new Alu
  alu.io.op1 := op1
  alu.io.op2 := op2
  alu.io.aluFun := aluFun.asBits
  aluResult := alu.io.alu

  val wbMux = new WbMux
  wbMux.io.csr := io.coprocessor.csr
  wbMux.io.pc4 := pc4
  wbMux.io.alu := aluResult
  wbMux.io.rdata := io.dataMemory.rdata
  wbMux.io.wbSel := wbSel.asBits
  wd := wbMux.io.wb

  io.dataMemory.addr := aluResult
  io.dataMemory.wdata := rs2
  io.dataMemory.valid := memVal
  io.dataMemory.rw := memRw

  val decode = new Decode
  decode.io.instruction := instruction
  wbSel := decode.io.wbSel
  pcSel := decode.io.pcSel
  aluFun := decode.io.aluFun
  memRw := decode.io.memRw
  memVal := decode.io.memVal
  op1Sel := decode.io.op1Sel
  op2Sel := decode.io.op2Sel
  rfWen := decode.io.rfWen
}

// Generate the Sodor Verilog
object SodorVerilog {
  def main(args: Array[String]) {
    SpinalVerilog(new Sodor)
  }
}


