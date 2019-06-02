package sodor

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class Sdram32 (width : Int, depth : Int)   extends Component {
  val io = new Bundle {
    // picorv32 host bus interface
    val host = new Bundle {
      val enable = in Bool
      val mem_valid = in(Bool)
      val mem_instr = in Bool
      val mem_wstrb = in UInt (width / 8 bits)
      val mem_wdata = in SInt (width bits)
      val mem_addr = in UInt (32 bits)

      val mem_rdata = out SInt (width bits)
      val mem_ready = out Bool
    }

    // SDRAM controller host bus
    val sdram = new Bundle {
      val rd_addr = out Bits (24 bits)
      val wr_addr = out Bits (24 bits)
      val wr_data = out Bits (16 bits)
      val wr_enable = out Bool
      val rd_enable = out Bool

      val rd_data = in Bits (16 bits)
      val rd_ready = in Bool
      val busy = in Bool
    }
  }

  // Registers to drive SDRAMs inputs
  val rd_addr = Reg(Bits(24 bits)) init 0
  val rd_enable = Reg(Bool) init False
  val wr_addr = Reg(Bits(24 bits)) init 0
  val wr_data = Reg(Bits(16 bits)) init 0
  val wr_enable = Reg(Bool) init False

  // Drive SDRAMs inputs from registers.
  io.sdram.rd_addr := rd_addr
  io.sdram.rd_enable := rd_enable
  io.sdram.wr_addr := wr_addr
  io.sdram.wr_data := wr_data
  io.sdram.wr_enable := wr_enable

  // Registers to drive host interface outputs
  val mem_ready = Reg(Bool) init False
  val mem_rd_data_low = Reg(Bits(16 bits)) init 0
  val mem_rd_data_high = Reg(Bits(16 bits)) init 0

  val addressLow = Bits(24 bits)
  addressLow := (io.host.mem_addr >> 1).asBits(23 downto 0)
  val addressHigh = Bits(24 bits)
  addressHigh := ((io.host.mem_addr >> 1) + 1).asBits(23 downto 0)

  val SDRAM32ReadWriteFSM = new StateMachine {
    val state0 = new State with EntryPoint
    val state1 = new State
    val state2 = new State
    val state3 = new State
    val state4 = new State
    val state5 = new State
    val state6 = new State
    val state7 = new State
    val state8 = new State
    val state9 = new State
    val state10 = new State

    state0 // Idle
      .whenIsActive {
        mem_ready := False
        when(io.host.enable & io.host.mem_valid & !io.sdram.busy) {
          when(io.host.mem_wstrb =/= 0) {
            wr_addr := addressLow
            wr_data := io.host.mem_wdata.asBits(15 downto 0)
            wr_enable := True
            goto(state7)
          }
          .otherwise {
            rd_addr := addressLow
            rd_enable := True
            goto(state1)
          }
        }
      }
    state1
      .whenIsActive {
        when(io.sdram.busy) {
          rd_enable := False
          goto(state2)
        }
      }
    state2
      .whenIsActive {
        when(io.sdram.rd_ready) {
          mem_rd_data_low := io.sdram.rd_data
          goto(state3)
        }

      }
    state3
      .whenIsActive {
        when(!io.sdram.busy) {
          rd_addr := addressHigh
          rd_enable := True
          goto(state4)
        }
      }
    state4
      .whenIsActive {
        when(io.sdram.busy) {
          rd_enable := False
          goto(state5)
        }
      }
    state5
      .whenIsActive {
        when(io.sdram.rd_ready) {
          mem_rd_data_high := io.sdram.rd_data
          mem_ready := True
          goto(state6)
        }
      }
    state6
      .whenIsActive {
        when(!io.sdram.busy) {
          goto(state0)
        }
      }
    state7
      .whenIsActive {
        when(io.sdram.busy) {
          wr_enable := False
          goto(state8)
        }
      }
    state8
      .whenIsActive {
        when(!io.sdram.busy) {
          wr_addr := addressHigh
          wr_enable := True
          goto(state9)
        }
      }
    state9
      .whenIsActive {
        when(io.sdram.busy) {
          wr_enable := False
          goto(state10)
        }
      }
    state10
      .whenIsActive {
        when(!io.sdram.busy) {
          mem_ready := True
          goto(state0)
        }
      }
  }

  // Shared bus must be driven low when not in use.
  when(io.host.enable & io.host.mem_valid) {
    io.host.mem_rdata := (mem_rd_data_high ## mem_rd_data_low).asSInt
    io.host.mem_ready := mem_ready
  } otherwise {
    io.host.mem_rdata := 0
    io.host.mem_ready := False
  }
}

object sdram32Verilog {
  def main(args: Array[String]): Unit = {
    val report = SpinalVerilog(new Sdram32(32, 16 * 1024 * 1024))
//    report.mergeRTLSource("quartus/sdram_controller_tb/sdram32") // Merge all rtl sources into sdram32.v file
//    report.mergeRTLSource("quartus/sdram32_tb/sdram32") // Merge all rtl sources into sdram32.v file
    report.printPruned()
  }
}
