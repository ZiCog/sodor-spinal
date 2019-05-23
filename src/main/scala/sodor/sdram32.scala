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

//  val rd_addr = Reg(Bits (24 bit)) init 0
  val rd_enable = Reg(Bool) init False

  io.sdram.wr_addr := 0
  io.sdram.rd_addr := 0
  rd_enable := False    // FIXME: We need this?

  io.sdram.rd_enable := rd_enable

  val rdataLow = Reg (Bits(16 bits)) init 0
  val ready = Bool
  ready := False
  val request = Bool
  val done = Reg(Bool) init False
  request := io.host.enable & io.host.mem_valid

  io.sdram.wr_enable := False
  io.sdram.wr_data := 0

  val SDRAMReadWriteFSM = new StateMachine {
    val stateIdle = new State with EntryPoint
    val stateWriteLow = new State
    val stateWaitWriteLowBusy = new State
    val stateWriteHigh= new State
    val stateWaitWriteHighBusy = new State
    val stateReadLow = new State
    val stateWaitReadLowBusy = new State
    val stateReadHigh= new State
    val stateWaitReadHighBusy = new State

    always {
      when (!(io.host.enable & io.host.mem_valid)) {
        io.sdram.wr_addr := 0
        rd_enable := False
        ready := False
        goto(stateIdle)
      }
    }
    stateIdle
      .whenIsActive {
        when (!io.sdram.busy & (io.host.enable & io.host.mem_valid)) {
          when (io.host.mem_wstrb =/= 0) {
            io.sdram.wr_addr := io.host.mem_addr.asBits(23 downto 0)
            io.sdram.wr_data :=  io.host.mem_wdata.asBits.asBits(15 downto 0)
            io.sdram.wr_enable := True
            goto(stateWriteLow)
          } otherwise {
            io.sdram.rd_addr := io.host.mem_addr.asBits(23 downto 0)
            io.sdram.rd_enable := True
            goto(stateReadLow)
          }
        }
      }
    stateWriteLow
      .whenIsActive {
        io.sdram.wr_addr := io.host.mem_addr.asBits(23 downto 0)
        io.sdram.wr_data :=  io.host.mem_wdata.asBits.asBits(15 downto 0)
        io.sdram.wr_enable := True
        when(io.sdram.busy) {
          goto (stateWaitWriteLowBusy)
        }
      }
    stateWaitWriteLowBusy
      .whenIsActive {
        io.sdram.wr_addr := io.host.mem_addr.asBits(23 downto 0)
        io.sdram.wr_data :=  io.host.mem_wdata.asBits.asBits(15 downto 0)
        io.sdram.wr_enable := False           // FIXME: Do we need to go low here? Can't we do back to back writes?
        when(!io.sdram.busy) {
          goto (stateWriteHigh)
        }
      }
    stateWriteHigh
      .whenIsActive {
        io.sdram.wr_addr := (io.host.mem_addr + 1)asBits(23 downto 0)
        io.sdram.wr_data :=  io.host.mem_wdata.asBits.asBits(31 downto 16)
        io.sdram.wr_enable := True
        when(io.sdram.busy) {
          goto (stateWaitWriteHighBusy)
        }
      }
    stateWaitWriteHighBusy
      .whenIsActive {
        io.sdram.wr_addr := (io.host.mem_addr + 1)asBits(23 downto 0)
        io.sdram.wr_data :=  io.host.mem_wdata.asBits.asBits(31 downto 16)
        io.sdram.wr_enable := False
        when(!io.sdram.busy) {
          goto (stateIdle)
        }
      }
      .onExit {
        ready := True
      }

    stateReadLow
      .whenIsActive {
        io.sdram.rd_addr := io.host.mem_addr.asBits(23 downto 0)
        io.sdram.rd_enable := True
        when(io.sdram.busy) {
          goto (stateWaitReadLowBusy)
        }
      }
    stateWaitReadLowBusy
      .whenIsActive {
        io.sdram.rd_addr := io.host.mem_addr.asBits(23 downto 0)
        io.sdram.rd_enable := False          // FIXME: Do we need to go low here? Can't we do back to back reads?
        when(!io.sdram.busy) {
          rdataLow := io.sdram.rd_data
          goto (stateReadHigh)
        }
      }
    stateReadHigh
      .whenIsActive {
        io.sdram.rd_addr := (io.host.mem_addr + 1)asBits(23 downto 0)
        io.sdram.rd_enable := True
        when(io.sdram.busy) {
          goto (stateWaitReadHighBusy)
        }
      }
    stateWaitReadHighBusy
      .whenIsActive {
        io.sdram.rd_addr := (io.host.mem_addr + 1)asBits(23 downto 0)
        io.sdram.rd_enable := False
        when(!io.sdram.busy) {
          goto (stateIdle)
        }
      }
      .onExit {
        ready := True
      }

    // Shared bus must be driven low when not in use.
    when (request) {
      io.host.mem_ready := ready
      io.host.mem_rdata := (io.sdram.rd_data ##  rdataLow).asSInt
    } otherwise {
      io.host.mem_ready := False
      io.host.mem_rdata := 0
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
