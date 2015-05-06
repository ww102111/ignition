package com.ignition.flow

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import com.ignition.types._

@RunWith(classOf[JUnitRunner])
class UnionSpec extends FlowSpecification {

  val schema = string("a") ~ int("b") ~ boolean("c")

  val grid1 = DataGrid(schema)
    .addRow("xyz", 1, true)
    .addRow("ccc", 0, true)

  val grid2 = DataGrid(schema)
    .addRow("kmk", 5, false)
    .addRow("ppp", 1, true)
    .addRow("abc", 0, true)

  val grid3 = DataGrid(schema)
    .addRow("zzz", 5, false)
    .addRow("xyz", 1, true)

  "Union" should {
    "merge identical rows" in {
      val union = Union()
      (grid1, grid2, grid3) --> union

      assertSchema(schema, union, 0)
      assertOutput(union, 0,
        Seq("xyz", 1, true), Seq("ccc", 0, true), Seq("kmk", 5, false), Seq("ppp", 1, true),
        Seq("abc", 0, true), Seq("zzz", 5, false), Seq("xyz", 1, true))
    }
    "fail for unmatching metadata" in {
      val metaX = string("a") ~ int("b")
      val gridX = DataGrid(metaX).addRow("aaa", 666)

      val union = Union()
      (grid1, grid2, gridX) --> union

      union.outSchema must throwA[FlowExecutionException]
      union.output.collect must throwA[FlowExecutionException]
    }
    "fail for unconnected inputs" in {
      val union = Union()
      union.outSchema must throwA[FlowExecutionException]
      union.output.collect must throwA[FlowExecutionException]
    }
    "be unserializable" in assertUnserializable(Union())
  }
}