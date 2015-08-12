import Chisel._

object Translator {

  def isBitSet(value: BigInt, bitIndex: Int): Boolean = {
    (value >> bitIndex) % 2 == BigInt(1)
  }

  def unsignedBigIntToBooleans(value: BigInt, width: Int): Vector[Boolean] = {
    assert(width > 0)
    assert(value < (1 << width))
    (0 to width).map(isBitSet(value, _)).to[Vector]
  }

  def signedBigIntToBooleans(value: BigInt, width: Int): Vector[Boolean] = {
    assert(width > 1)
    assert(value < (1 << (width-1)))
    assert(value >= -(1 << (width-1)))
    val sgn: Boolean = value < 0
    (0 to width).map(i => if (i == width-1) sgn else isBitSet(value, i)).to[Vector]
  }

  def toBooleans(signal: Data, value: Any): Vector[Boolean] = {
    (signal, value) match {
      case (s: Bool, v: Boolean) =>
        if (s.dir == INPUT) Vector(v) else Vector()
      case (s: UInt, v: BigInt) =>
        if (s.dir == INPUT) unsignedBigIntToBooleans(value=v, width=s.getWidth()) else Vector()
      case (s: SInt, v: BigInt) =>
        if (s.dir == INPUT) signedBigIntToBooleans(value=v, width=s.getWidth()) else Vector()
      case (s: Bits, v: BigInt) =>
        if (s.dir == INPUT) unsignedBigIntToBooleans(value=v, width=s.getWidth()) else Vector ()
      case (s: Vec[Data], v: Iterable[Any]) => iterableToBooleans(signals=s, values=v)
      case (s: Bundle, v: Map[String, Any]) => mapToBooleans(signal=s, value=v)
    }
  }

  def mapToBooleans(signal: Bundle, value: Map[String, Any]): Vector[Boolean] = {
    def wrappedToBooleans(nameAndSignal: (String, Data)): Vector[Boolean] = {
      toBooleans(signal=nameAndSignal._2, value=value(nameAndSignal._1))
    }
    val nested = signal.elements.map(wrappedToBooleans)
    nested.flatten.to[Vector]
  }

  def iterableToBooleans(signals: Vec[Data], values: Iterable[Any]): Vector[Boolean] = {
    def wrappedToBooleans(signalAndValue: (Data, Any)): Vector[Boolean] = {
      val (signal: Data, value: Any) = signalAndValue
      toBooleans(signal=signal, value=value)
    }
    val nested = signals.zip(values).map(wrappedToBooleans)
    nested.flatten.to[Vector]
  }

}

