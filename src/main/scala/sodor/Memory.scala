package sodor

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import spinal.core._

class Memory (dataWidth : Int, depth : Int, initFile: String) extends Component {
  val io = new Bundle {
    val enable = in Bool
    val mem_valid = in Bool
    val mem_instr = in Bool
    val mem_wstrb = in UInt(dataWidth / 8 bits)
    val mem_wdata = in SInt(dataWidth bits)
    val mem_addr = in UInt ((Math.log10(depth)/Math.log10(2.0)).toInt  bits)
    val mem_rdata = out SInt(dataWidth bits)
    val mem_ready = out Bool
  }

  val rdata = SInt(dataWidth bits)
  val ready = Bool

  def generateInitialContent = {
    val buffer = new ArrayBuffer[BigInt]
    for (line <- Source.fromFile(initFile).getLines) {
      val tokens: Array[String] = line.split("(//)").map(_.trim)
      if (tokens.length > 0 && tokens(0) != "") {
        val i = Integer.parseInt(tokens(0), 16)
        buffer.append(i)
      }
    }

    for(address <- 0 until depth) yield {
      S(buffer(address), dataWidth bits)
    }
  }

  val memory = Mem (SInt (dataWidth bits), depth)
  memory.init (generateInitialContent)

  memory.write(
    io.mem_addr >> U(2),
    io.mem_wdata,
    io.enable & io.mem_valid,
    io.mem_wstrb.asBits
  )

  // Wire-OR'ed bus outputs.
  when (io.enable & io.mem_valid) {
    rdata := memory.readAsync(io.mem_addr >> U(2))
    ready := True
  } otherwise {
    rdata := 0
    ready := False
  }
  io.mem_ready := ready
  io.mem_rdata := rdata
}