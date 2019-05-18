package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class sdram32 (width : Int, depth : Int)   extends Component {
  val io = new Bundle {
    // picorv32 host bus interface
    val host = new Bundle {
      val enable = in Bool
      val mem_valid = in Bool
      val mem_instr = in Bool
      val mem_wstrb = in UInt(width / 8 bits)
      val mem_wdata = in SInt(width bits)
      val mem_addr = in UInt ((Math.log10(depth * 4)/Math.log10(2.0)).toInt  bits)

      val mem_rdata = out SInt(width bits)
      val mem_ready = out Bool
    }

    // SDRAM controller host bus
    val sdram = new Bundle {
      val rd_addr = out Bits (24 bit)
      val wr_addr = out Bits (24 bit)
      val wr_data = out Bits (16 bit)
      val wr_enable = out Bool
      val rd_enable = out Bool

      val rd_data = in Bits (16 bit)
      val rd_ready = in Bool
      val busy = in Bool
    }
  }

  val mem_rdata = Reg (SInt(width bits)) init 0
  val mem_ready = Reg (Bool) init False

  val rd_addr = Reg (Bits (24 bit)) init 0
  val wr_addr = Reg (Bits (24 bit)) init 0
  val wr_data = Reg (Bits (16 bit)) init 0
  val wr_enable = Reg (Bool) init False
  val rd_enable = Reg (Bool) init False

  io.host.mem_rdata := 0 
  io.host.mem_ready := False

  io.sdram.rd_addr := 0
  io.sdram.wr_addr := 0
  io.sdram.wr_data := 0
  io.sdram.wr_enable := False
  io.sdram.rd_enable := False


  val fsm = new StateMachine {
    val stateA = new State with EntryPoint
    val stateB = new State
    val stateC = new State
    val stateD = new State
    val stateE = new State
    val stateF = new State

    stateA
      .onEntry {
      }
      .whenIsActive {
      }

    stateB
      .onEntry {
      }
      .whenIsActive {
      }
      .onExit {
      }
    stateC
      .onEntry {
      }
      .whenIsActive {
      }
      .onExit {
      }
    stateD
      .onEntry {
      }
      .whenIsActive {
      }
      .onExit {
      }
    stateE
      .whenIsActive {
      }
    stateF
      .onEntry {
    }
  }
}

object sdram32Verilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new sdram32(32, 16 * 1024 * 1024))
    report.mergeRTLSource("quartus/sdram_controller_tb/sdram32") // Merge all rtl sources into sdram32.v file
    report.printPruned()
  }
}
