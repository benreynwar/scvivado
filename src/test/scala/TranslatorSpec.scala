import org.scalatest._
import Chisel._


class TranslatorSpec extends FlatSpec with Matchers {

  "An individual Bool" should "translate only when INPUT" in {
    val outputSignal = Bool(OUTPUT)
    assert(Translator.toBooleans(outputSignal, true) == Vector[Boolean]())
    assert(Translator.toBooleans(outputSignal, false) == Vector[Boolean]())
    val inputSignal = Bool(INPUT)
    assert(Translator.toBooleans(inputSignal, true) == Vector[Boolean](true))
    assert(Translator.toBooleans(inputSignal, false) == Vector[Boolean](false))
  }
  "A Vec[Bool]" should "translate only when INPUT" in {
    val outputSignal = Vec.fill(3)(Bool(OUTPUT))
    assert(Translator.toBooleans(outputSignal, Vector(true, false, true)) == Vector[Boolean]())
    val inputSignal = Vec.fill(3)(Bool(INPUT))
    assert(Translator.toBooleans(inputSignal, Vector(false, false, true)) == Vector[Boolean](false, false, true))
  }
  "A Bundle[Bool]" should "only translate the INPUT components" in {
    class CustomBundle extends Bundle {
      val i_bool = Bool(INPUT)
      val o_bool = Bool(OUTPUT)
      val i_vec = Vec.fill(4)(Bool(INPUT))
      val o_vec = Vec.fill(5)(Bool(OUTPUT))
    }
    val data1 = Map[String, Any](
      "i_bool" -> true,
      "o_bool" -> false,
      "i_vec" -> Vector(true, true, false, false),
      "o_vec" -> Vector(true, false, true, true, true)
      )
    val normalSignal = new CustomBundle()
    val switchedSignal = new CustomBundle().flip()

    assert(Translator.toBooleans(normalSignal, data1) == Vector[Boolean](true, true, true, false, false))
    assert(Translator.toBooleans(switchedSignal, data1) == Vector[Boolean](false, true, false ,true, true, true))
  }
}