import Chisel._
import math.pow

class SimpleA(val dataWidth: Int) extends Module {
  val dataType = Bits(INPUT, width = dataWidth)
  val arrayLength = 4
  val simpleB = Module(new SimpleB(dataWidth = dataWidth,
				   arrayLength = arrayLength))
  val arrayType = Vec(gen = dataType, n = arrayLength)
  val io = new Bundle {
    val i_valid = Bool(INPUT)
    val i_data = dataType
    val i_array = arrayType
    val o_valid = Bool(OUTPUT)
    val o_data = dataType.flip
    val o_array = arrayType.flip
  }
  // val o_valid = RegNext(simpleB.io.o_valid)
  // val o_data = RegNext(simpleB.io.o_data)
  // val o_array = RegNext(simpleB.io.o_array)
  // simpleB.io.i_valid := io.i_valid
  // simpleB.io.i_data := io.i_data
  // simpleB.io.i_array := io.i_array
  // io.o_valid := simpleB.io.o_valid
  // io.o_data := simpleB.io.o_data
  // io.o_array := simpleB.io.o_array  
}

