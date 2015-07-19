import Chisel._
import math.pow

class SimpleB(val dataWidth: Int, val arrayLength: Int) extends Module {
  val dataType = Bits(INPUT, width = dataWidth)
  val arrayType = Vec(gen = dataType, n = arrayLength)
  val io = new Bundle {
    val i_valid = Bool(INPUT)
    val i_data = dataType
    val i_array = arrayType
    val o_valid = Bool(OUTPUT)
    val o_data = dataType.flip
    val o_array = arrayType.flip
  }
  io.o_valid := io.i_valid
  io.o_data := io.i_data
  io.o_array := io.i_array
}

