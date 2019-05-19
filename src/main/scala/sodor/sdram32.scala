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

  val dataHigh = Reg (Bits (16 bit)) init 0

  val fsm = new StateMachine {
    val stateIdle = new State with EntryPoint
    val stateReadHigh = new State
    val stateRead1 = new State
    val stateWriteHigh = new State
    always {
      when (!io.host.enable) {
        goto(stateIdle)
      }
    }

    stateIdle
      .onEntry {
        io.host.mem_rdata := 0
        io.host.mem_ready := False
        io.sdram.wr_enable := False
        io.sdram.rd_enable := False
      }
      .whenIsActive {
        when (io.host.enable & io.host.mem_valid) {
          when (io.host.mem_wstrb =/= 0) {
            when (!io.sdram.busy) {
              // Write cycle: Save high word and write low word to SDRAM.
              io.sdram.wr_data := io.host.mem_wdata.asBits(15 downto 0)
              io.sdram.wr_addr := io.host.mem_addr.asBits(15 downto 0)   // FIXME: word32 or word16 address?
              io.sdram.wr_enable := True
              goto(stateWriteHigh)
            }
          } otherwise {
            // A Read cycle
            dataHigh := io.host.mem_wdata.asBits(31 downto 16)
            goto(stateReadHigh)
          }
        }
      }

    stateReadHigh
      .onEntry {
      }
      .whenIsActive {
      }
      .onExit {
      }
    stateWriteHigh
      .onEntry {
      }
      .whenIsActive {
          when (!io.sdram.busy) {
            // Write cycle: High word
            io.sdram.wr_data := io.host.mem_wdata.asBits(31 downto 16)
            io.sdram.wr_addr := (io.host.mem_addr + 1).asBits(15 downto 0)   // FIXME: word32 or word16 address?
            goto(stateIdle)
          }
      }
      .onExit {
        io.host.mem_ready := True
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
