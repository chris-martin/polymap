package org.codeswarm.polymap

import org.scalatest._

class MultiMapTest extends FunSpec {

  describe ("""A MultiMap[String, String]""") {

    class X extends MultiMap[String, String]

    it ("""should be initially empty""") {
      val x = new X()
      assert(x.isEmpty)
    }

    it ("""should be empty after it is cleared""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "c", "b" -> "d")
      x.clear()
      assert(x.isEmpty)
    }

    it ("""should have 1 element after ("a" -> "b") is added""") {
      val x = new X()
      x += "a" -> "b"
      assert(x.size == 1)
    }

    it ("""should equal Set(("a", "b")) after ("a" -> "b") is added""") {
      val x = new X()
      x += "a" -> "b"
      assert(x == Set(("a", "b")))
    }

    it ("""should have 1 element after ("a" -> "b") is added twice""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "b")
      assert(x.size == 1)
    }

    it ("""should equal Set(("a", "b")) after ("a" -> "b") is added twice""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "b")
      assert(x == Set(("a", "b")))
    }

    it ("""should have 2 elements after ("a"->"b") and ("c"->"d") are added""") {
      val x = new X()
      x += ("a" -> "b", "c" -> "d")
      assert(x.size == 2)
    }

    it ("""should equal Set(("a"->"b"),("c"->"d")) after ("a"->"b") and ("c"->"d") are added""") {
      val x = new X()
      x += ("a" -> "b", "c" -> "d")
      assert(x == Set(("a"->"b"),("c"->"d")))
    }

    it ("""should have 2 elements after ("a"->"b") and ("a"->"c") are added""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "c")
      assert(x.size == 2)
    }

    it ("""should equal Set(("a"->"b"), ("a"->"c")) after ("a"->"b") and ("a"->"c") are added""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "c")
      assert(x == Set(("a"->"b"), ("a"->"c")))
    }

    it ("""should map "a" to ["b", "c"] after ("a"->"b") and ("a"->"c") are added""") {
      val x = new X()
      x += ("a" -> "b", "a" -> "c")
      assert((x get "a") == Set("b", "c"))
    }

  }

}