import Chisel._
import org.scalatest._
import scala.math.pow


class SimpleBOutputEqualsInputTester(c: SimpleB) extends Tester(c) {
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
    expect(c.io.o_valid, i_valid)
    expect(c.io.o_data, i_data)
    (c.io.o_array, i_array).zipped foreach {
      (element,value) => expect(element, value)
    }
    step(1)
  }
}


class SimpleBSpec extends FlatSpec with Matchers {
  val dataWidth = 3
  val arrayLength = 4
  //val m = new SimpleB(dataWidth, arrayLength)
  "SimpleB" should "compile" in {
    val testArgs = Array("--genHarness", "--compile", "--backend", "c")
    chiselMainTest(testArgs, () => Module(new SimpleB(dataWidth, arrayLength))
		 )(m => new Tester(m))
  }
  "The output from SimpleB" should "be the same as it's input" in {
    val testArgs = Array("--test")
    chiselMainTest(testArgs, () => Module(new SimpleB(dataWidth, arrayLength))
		 )(m => new SimpleBOutputEqualsInputTester(m))
  }
}

