import Chisel._
import grizzled.slf4j.Logging


class SimpleA(val dataWidth: Int) extends Module {
  def makeDataType(dir: IODirection): Bits = {
    Bits(dir=dir, width=dataWidth)
  }
  val arrayLength = 4
  val simpleB = Module(new SimpleB(dataWidth = dataWidth,
				   arrayLength = arrayLength))
  def makeArrayType(dir: IODirection): Vec[Bits] = {
    Vec(gen=makeDataType(dir), n=arrayLength)
  }

  class SimpleAIO extends Bundle {
    val i_valid = Bool(INPUT)
    val i_data = makeDataType(INPUT)
    val i_array = makeArrayType(INPUT)
    val o_valid = Bool(OUTPUT)
    val o_data = makeDataType(OUTPUT)
    val o_array = makeArrayType(OUTPUT)
  }

  val io = new SimpleAIO

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

