package com.ignition.workflow

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.ignition.SparkTestHelper
import com.ignition.workflow.rdd.core.{ RDDFilter, RDDFlatMap, RDDJoin, RDDMap, RDDReduceByKey, RDDSequence, RDDUnion }

@RunWith(classOf[JUnitRunner])
class RDDStepSpec extends Specification with SparkTestHelper {

  "RDD Sequence" should {
    "yield the result" in {
      val step = new RDDSequence(List(1, 2, 3, 4))
      step.output.collect.toSet === Set(1, 2, 3, 4)
    }
  }

  "RDD Sequence + Map + Filter" should {
    "yield the result" in {
      val stepA = new RDDSequence(List(1, 2, 3, 4))
      val stepB = new RDDMap[Int, Int](_ * 2)
      val stepC = new RDDFilter[Int](_ % 3 == 0)
      stepA.connectTo(stepB).connectTo(stepC)
      stepC.output.collect === Array(6)
    }
  }

  "RDD Sequence + FlatMap + ReduceByKey" should {
    "yield the result" in {
      val stepA = new RDDSequence(List(1, 2, 3, 4))
      val stepB = new RDDFlatMap[Int, (String, Int)](x => List((x.toString, x), (x.toString, x * 2)))
      val stepC = new RDDReduceByKey[String, Int](_ + _)
      stepA.connectTo(stepB).connectTo(stepC)
      stepC.output.collect.toSet === Set(("1", 3), ("2", 6), ("3", 9), ("4", 12))
    }
  }

  "RDD Sequence + Map + Join" should {
    "yield the result" in {
      val stepA = new RDDSequence(List(1, 2, 3, 4))
      val stepB = new RDDSequence(List(3, 4, 5))
      val stepC = new RDDMap[Int, (Int, Int)](x => (x, x))
      val stepD = new RDDMap[Int, (Int, String)](x => (x, x.toString))
      val stepE = new RDDJoin[Int, Int, String]()
      stepA.connectTo(stepC).connectTo1(stepE)
      stepB.connectTo(stepD).connectTo2(stepE)
      stepE.output.collect.toSet === Set((3, (3, "3")), (4, (4, "4")))
    }
  }

  "RDD Sequence + Union" should {
    "yield the result" in {
      val stepA = new RDDSequence(List(1, 3, 4))
      val stepB = new RDDSequence(List(3, 2, 7, 8))
      val stepC = new RDDSequence(List(1, 5, 4))
      val stepD = new RDDSequence(List(6))
      val stepE = new RDDSequence(List(0, 9))
      val stepF = new RDDUnion[Int]()
      stepA.connectTo(stepF)
      stepB.connectTo(stepF)
      stepC.connectTo(stepF)
      stepD.connectTo(stepF)
      stepE.connectTo(stepF)
      stepF.output.collect.toList.sorted === List(0, 1, 1, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9)
    }
  }
}