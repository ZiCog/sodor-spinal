package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class Sdram32 (width : Int, depth : Int)   extends Component {
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

  io.host.mem_rdata := 0 
  io.host.mem_ready := False

  io.sdram.rd_addr := 0
  io.sdram.wr_addr := 0
  io.sdram.wr_data := 0
  io.sdram.wr_enable := False
  io.sdram.rd_enable := False

  val readDataLow = Reg (Bits (16 bit)) init 0

  val fsm = new StateMachine {
    val stateWaitHostCommand = new State with EntryPoint
    val stateWaitReadLowReady = new State
    val stateWaitReadHighReady = new State
    val stateWaitWriteLowReady = new State
    always {
      when (!io.host.enable) {
        io.host.mem_rdata := 0
        io.host.mem_ready := False
        goto(stateWaitHostCommand)
      }
    }

    stateWaitHostCommand
      .onEntry {
        io.host.mem_rdata := 0
        io.host.mem_ready := False
        io.sdram.wr_enable := False
        io.sdram.rd_enable := False
      }
      .whenIsActive {
        when (io.host.enable & io.host.mem_valid & !io.sdram.busy) {
          when (io.host.mem_wstrb =/= 0) {
            // Initiate write low word.
            io.sdram.wr_addr := io.host.mem_addr.asBits(23 downto 0)   // FIXME: word32 or word16 address?
            io.sdram.wr_data := io.host.mem_wdata.asBits(15 downto 0)
            io.sdram.wr_enable := True
            goto(stateWaitWriteLowReady)
          } otherwise {
            // Initiate read low word.
            io.sdram.rd_addr := io.host.mem_addr.asBits(23 downto 0)   // FIXME: word32 or word16 address?
            io.sdram.rd_enable := True
            goto(stateWaitReadLowReady)
          }
        }
      }

    stateWaitReadLowReady
      .whenIsActive {
        when (!io.sdram.busy & io.sdram.rd_ready) {
          // Capture the low word
          readDataLow := io.sdram.rd_data
          goto(stateWaitReadHighReady)
        }
      }
      .onExit {
        // Initiate read high word
        io.sdram.rd_addr := (io.host.mem_addr + 1).asBits(23 downto 0)   // FIXME: word32 or word16 address?
        io.sdram.rd_enable := True
      }

    stateWaitReadHighReady
      .whenIsActive {
        when(io.sdram.rd_ready) {
          // Capture high word
          io.host.mem_rdata := (readDataLow ## io.sdram.rd_data).asSInt 
          goto (stateWaitHostCommand)
        }
      }
      .onExit {
        io.sdram.rd_enable := False
      }

    stateWaitWriteLowReady
      .whenIsActive {
          when (!io.sdram.busy) {
            // Write cycle: High word
            io.sdram.wr_addr := (io.host.mem_addr + 1).asBits(23 downto 0)   // FIXME: word32 or word16 address?
            io.sdram.wr_data := io.host.mem_wdata.asBits(31 downto 16)
            io.sdram.wr_enable := True
            goto(stateWaitHostCommand)
          }
      }
      .onExit {
        io.sdram.wr_enable := False
        io.host.mem_ready := True
      }
  }
}

object sdram32Verilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new Sdram32(32, 16 * 1024 * 1024))
    report.mergeRTLSource("quartus/sdram_controller_tb/sdram32") // Merge all rtl sources into sdram32.v file
    report.printPruned()
  }
}
