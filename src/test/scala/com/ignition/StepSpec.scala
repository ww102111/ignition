package com.ignition

import org.junit.runner.RunWith
import org.specs2.ScalaCheck
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StepSpec extends FlowSpecification with ScalaCheck {
  sequential

  trait StringStep {
    def toXml: scala.xml.Elem = ???
    def toJson: org.json4s.JValue = ???
  }

  class Mock extends FlowRuntime { val previewMode = false }

  implicit val rt = new Mock

  case class StringProducer(x: String) extends Producer[String, Mock] with StringStep {
    protected def compute(implicit runtime: Mock) = x
  }

  case class StringTransformer() extends Transformer[String, Mock] with StringStep {
    protected def compute(arg: String)(implicit runtime: Mock) = arg.toUpperCase
  }

  case class StringSplitter() extends Splitter[String, Mock] with StringStep {
    def outputCount = 2
    protected def compute(arg: String, index: Int)(implicit runtime: Mock) =
      if (index == 0) arg.take(arg.length / 2) else arg.takeRight(arg.length / 2)
  }

  case class StringMerger(inputCount: Int, override val allInputsRequired: Boolean)
      extends Merger[String, Mock] with StringStep {
    protected def compute(args: IndexedSeq[String])(implicit runtime: Mock) =
      args.filter(_ != null).mkString
  }

  case class StringModule(size: Int) extends Module[String, Mock] with StringStep {
    def inputCount = size
    def outputCount = size
    var computeCount: Int = 0
    protected def compute(args: IndexedSeq[String], index: Int)(implicit runtime: Mock) = {
      computeCount += 1
      args(index)
    }
  }

  def throwRT() = throw new RuntimeException("runtime")
  def throwWF() = throw ExecutionException("workflow")

  "Producer" should {
    "yield output" in prop { (s: String) =>
      val step = StringProducer(s)
      step.output === s
    }
    "fail for output(!=0)" in {
      val step = StringProducer("abc")
      step.output(1) must throwA[ExecutionException]
    }
    "wrap runtime error into workflow exception" in {
      val step = new StringProducer("abc") {
        override def compute(implicit runtime: Mock) = throwRT
      }
      step.output must throwA[ExecutionException](message = "Step computation failed")
    }
    "propagate workflow exception" in {
      val step = new StringProducer("abc") {
        override def compute(implicit runtime: Mock) = throwWF
      }
      step.output must throwA[ExecutionException](message = "workflow")
    }
  }

  "Transformer" should {
    "yield output" in prop { (s: String) =>
      val step0 = StringProducer(s)
      val step1 = StringTransformer()
      step0 --> step1
      step1.output === s.toUpperCase
    }
    "fail for output(!=0)" in {
      val step0 = StringProducer("")
      val step1 = StringTransformer()
      step0 --> step1
      step1.output(1) must throwA[ExecutionException]
    }
    "throw exception when not connected" in {
      val step = StringTransformer()
      step.output must throwA[ExecutionException](message = "Input is not connected")
    }
  }

  "Splitter" should {
    "yield output" in prop { (s: String) =>
      val step0 = StringProducer(s)
      val step1 = StringSplitter()
      step0 --> step1
      step1.output(0) === s.take(s.length / 2)
      step1.output(1) === s.takeRight(s.length / 2)
    }
    "fail for output(<0 or >= outputCount)" in {
      val step0 = StringProducer("")
      val step1 = StringSplitter()
      step0 --> step1
      step1.output(-1) must throwA[ExecutionException]
      step1.output(2) must throwA[ExecutionException]
    }
    "throw exception when not connected" in {
      val step = StringSplitter()
      step.output must throwA[ExecutionException](message = "Input is not connected")
    }
  }

  "Merger" should {
    "yield output with mandatory inputs" in prop { (s1: String, s2: String, s3: String) =>
      val step1 = StringProducer(s1)
      val step2 = StringProducer(s2)
      val step3 = StringProducer(s3)
      val step4 = StringMerger(3, true)
      (step1, step2, step3) --> step4
      step4.output === s1 + s2 + s3
    }
    "yield output with optional inputs" in prop { (s1: String, s2: String) =>
      val step1 = StringProducer(s1)
      val step2 = StringProducer(s2)
      val step3 = StringMerger(3, false)
      step1 --> step3.in(0)
      step2 --> step3.in(2)
      step3.output === s1 + s2
    }
    "fail for output(!=0)" in {
      val step0 = StringProducer("")
      val step1 = StringMerger(1, false)
      step0 --> step1
      step1.output(1) must throwA[ExecutionException]
    }
    "fail when a mandatory input is not connected" in {
      val step1 = StringProducer("")
      val step2 = StringProducer("")
      val step3 = StringMerger(3, true)
      step1 --> step3.in(0)
      step2 --> step3.in(2)
      step3.output must throwA[ExecutionException](message = "Input1 is not connected")
    }
  }

  "Module" should {
    "yield output" in prop { (s1: String, s2: String) =>
      val step1 = StringProducer(s1)
      val step2 = StringProducer(s2)
      val step3 = StringModule(2)
      (step1, step2) --> step3
      step3.output(0) === s1
      step3.output(1) === s2
    }
    "fail for output(<0 or >= outputCount)" in {
      val step0 = StringProducer("")
      val step1 = StringModule(1)
      step0 --> step1
      step1.output(-1) must throwA[ExecutionException]
      step1.output(1) must throwA[ExecutionException]
    }
  }

  "Step cache" should {
    "reuse computed values" in {
      val step0 = StringProducer("0")
      val step1 = StringProducer("1")
      val step2 = StringProducer("2")
      val step = StringModule(3)
      (step0, step1, step2) --> step
      step.computeCount === 0
      step.output(0)
      step.computeCount === 1
      step.output(0)
      step.computeCount === 1
      step.output(2)
      step.computeCount === 2
      step.output(1)
      step.computeCount === 3
      step.output(2)
      step.computeCount === 3
    }
  }

  "Step connection operators" should {
    val p1 = StringProducer("a")
    val t1 = StringTransformer()
    val t2 = StringTransformer()
    val t3 = StringTransformer()
    val s1 = StringSplitter()
    val g1 = StringMerger(2, true)
    val m1 = StringModule(3)

    "connect producers and transformers with `to`" in {
      (p1 to t1 to t2 to t3) === t3
      t1.inbound === p1
      t2.inbound === t1
      t3.inbound === t2
    }
    "connect producers and transformers with `-->`" in {
      (p1 --> t1 --> t2) === t2
      t1.inbound === p1
      t2.inbound === t1
      t3.inbound === t2
    }
    "connect multi-port steps with `out()` and `in()`" in {
      s1.out(0) to g1.in(1)
      g1.in(0).inbound == s1.out(0)
      (s1.out(1) to t1 to t2) === t2
      t1.inbound === s1.out(1)
      t2.inbound === t1
      p1 to t1 to m1.in(0)
      t1.inbound === p1
      m1.in(0).inbound === t1
      s1.out(1) --> (m1.in(1), t1)
      m1.in(1).inbound === s1.out(1)
      t1.inbound === s1.out(1)
    }
    "connect products with multi-input steps" in {
      (t1, t2) to m1
      m1.in(0).inbound === t1
      m1.in(1).inbound === t2
      (s1.out(1), t1) --> m1
      m1.in(0).inbound === s1.out(1)
      m1.in(1).inbound === t1
    }
    "connect muti-output steps with products" in {
      s1 to (t1, t2)
      t1.inbound === s1.out(0)
      t2.inbound === s1.out(1)
      s1 --> (t1, t2)
      t1.inbound === s1.out(0)
      t2.inbound === s1.out(1)
      m1 --> (t1, g1.in(1))
      t1.inbound === m1.out(0)
      g1.in(1).inbound === m1.out(1)
    }
  }
}