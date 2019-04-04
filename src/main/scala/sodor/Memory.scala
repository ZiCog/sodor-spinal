package sodor

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import spinal.core._

class Memory (width : Int, depth : Int, initFile: String) extends Component {
  val io = new Bundle {
    val enable = in Bool
    val mem_valid = in Bool
    val mem_instr = in Bool
    val mem_wstrb = in UInt(width / 8 bits)
    val mem_wdata = in SInt(width bits)
    val mem_addr = in UInt ((Math.log10(depth * 4)/Math.log10(2.0)).toInt  bits)
    val mem_rdata = out SInt(width bits)
    val mem_ready = out Bool
  }

  val rdata = SInt(width bits)
  val ready = Bool

  def generateInitialContent = {
    val buffer = new ArrayBuffer[BigInt]
    for (line <- Source.fromFile(initFile).getLines) {
      val tokens: Array[String] = line.split("(//)").map(_.trim)
      if (tokens.length > 0 && tokens(0) != "") {
        val i = BigInt(tokens(0), 16).toInt
        buffer.append(i)
      }
    }

    for(address <- 0 until depth) yield {
      S(buffer(address), width bits)
    }
  }

  val memory = Mem (SInt (width bits), depth)
  memory.init (generateInitialContent)

  val addr = UInt(14 bits)
  addr := io.mem_addr >> 2

  memory.write(
    addr,
    io.mem_wdata,
    io.enable & io.mem_valid,
    io.mem_wstrb.asBits
  )

  // Wire-OR'ed bus outputs.
  when (io.enable & io.mem_valid) {
    rdata := memory.readAsync(addr)
    ready := True
  } otherwise {
    rdata := 0
    ready := False
  }
  io.mem_ready := ready
  io.mem_rdata := rdata
}