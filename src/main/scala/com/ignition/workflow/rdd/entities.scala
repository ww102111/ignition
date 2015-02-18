package com.ignition.workflow.rdd

import scala.reflect.ClassTag

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import com.ignition.workflow.{ Step0, Step1, Step2, StepN }

/**
 * Creates a new RDD[T]
 */
abstract class RDDStep0[T: ClassTag](func: SparkContext => RDD[T])
  extends Step0[RDD[T], SparkContext] {
  protected def compute(sc: SparkContext) = func(sc)
}

/**
 * Transforms RDD[S] => RDD[T].
 *
 * @author Vlad Orzhekhovskiy
 */
abstract class RDDStep1[S: ClassTag, T: ClassTag](func: RDD[S] => RDD[T])
  extends Step1[RDD[S], RDD[T], SparkContext] {
  protected def compute(sc: SparkContext)(arg: RDD[S]) = func(arg)
}

/**
 * Transforms (RDD[S1], RDD[S2]) => RDD[T].
 *
 * @author Vlad Orzhekhovskiy
 */
abstract class RDDStep2[S1: ClassTag, S2: ClassTag, T: ClassTag](func: (RDD[S1], RDD[S2]) => RDD[T])
  extends Step2[RDD[S1], RDD[S2], RDD[T], SparkContext] {
  protected def compute(sc: SparkContext)(arg1: RDD[S1], arg2: RDD[S2]) = func(arg1, arg2)
}

/**
 * Transforms SparkContext => Iterable[RDD[S]] => RDD[T].
 *
 * @author Vlad Orzhekhovskiy
 */
abstract class RDDStepN[S: ClassTag, T: ClassTag](func: SparkContext => Iterable[RDD[S]] => RDD[T])
  extends StepN[RDD[S], RDD[T], SparkContext] {
  protected def compute(sc: SparkContext)(args: Iterable[RDD[S]]) = func(sc)(args)
}