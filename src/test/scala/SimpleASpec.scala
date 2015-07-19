import Chisel._
import org.scalatest._
import scala.math.pow


class SimpleATester(c: SimpleA) extends Tester(c) {
  val maxData = pow(2, c.dataWidth).toInt
  for (r <- 0 until 16) {
    val i_valid = rnd.nextInt(2)
    val i_data = rnd.nextInt(maxData)
    val i_array = List.fill(c.arrayLength)(rnd.nextInt(maxData))
    poke(c.io.i_valid, i_valid)
    poke(c.io.i_data, i_data)
    (c.io.i_array, i_array).zipped foreach {
      (element,value) => poke(element, value)
    }
    step(1)
    expect(c.io.o_valid, i_valid)
    expect(c.io.o_data, i_data)
    (c.io.o_array, i_array).zipped foreach {
      (element,value) => expect(element, value)
    }
  }
}


class SimpleASpec extends FlatSpec with Matchers {
  val dataWidth = 3
  "SimpleA" should "compile" in {
    val testArgs = Array("--genHarness", "--compile", "--backend", "c")
    chiselMainTest(testArgs, () => Module(new SimpleA(dataWidth))
  		 )(m => new Tester(m))
  }
  // "The output from SimpleA" should "be the same as it's input but delayed one clock cycle" in {
  //   val testArgs = Array("--test")
  //   chiselMainTest(testArgs, () => Module(new SimpleA(dataWidth))
  // 		 )(m => new SimpleATester(m))
  // }
}

