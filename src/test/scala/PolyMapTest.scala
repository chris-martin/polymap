package org.codeswarm.polymap

import org.scalatest._

class PolyMapTest extends FunSpec {

  describe ("""A PolyMap[String] with no indexes""") {

    class X extends PolyMap[String]

    it ("""should initially be empty""") {
      val x = new X()
      assert(x.isEmpty)
    }

    it ("""should iniitally equal the empty set""") {
      val x = new X()
      assert(x == Set())
    }

    it ("""should have NoIndexes state""") {
      val x = new X()
      assert(x.state match {
        case s: x.State.NoIndexes => true
        case _ => false
      })
    }

    it ("""should have 1 element after "abc" is added""") {
      val x = new X()
      x += "abc"
      assert(x.size == 1)
    }

    it ("""should have 1 element after "abc" is added twice""") {
      val x = new X()
      x += "abc"
      x += "abc"
      assert(x.size == 1)
    }

    it ("""should equal Set("abc") after "abc" is added""") {
      val x = new X()
      x += "abc"
      assert(x == Set("abc"))
    }

    it ("""should be empty after "abc" is added then removed""") {
      val x = new X()
      x += "abc"
      x -= "abc"
      assert(x.isEmpty)
    }

    it ("""should have 1 index when 1 index is added""") {
      val x = new X()
      x.createIndex(_(0))
      assert(x.state match {
        case s: x.State.SomeIndexes if s.indexes.size == 1 => true
        case _ => false
      })
    }

    it ("""should retain its elements when an index is added""") {
      val x = new X()
      x += ("abc", "def")
      x.createIndex(_(0))
      assert(x == Set("abc", "def"))
    }

  }

  describe ("""A PolyMap[String] indexed by first letter""") {

    class X extends PolyMap[String] {
      val byFirstLetter = index(_(0))
    }

    it ("""should initially be empty""") {
      val x = new X()
      assert(x.isEmpty)
    }

    it ("""should have 1 element after "abc" is added""") {
      val x = new X()
      x += "abc"
      assert(x.size == 1)
    }

    it ("""should have 1 element after "abc" is added twice""") {
      val x = new X()
      x += ("abc", "abc")
      assert(x.size == 1)
    }

    it ("""should equal Set("abc") after "abc" is added""") {
      val x = new X()
      x += "abc"
      assert(x == Set("abc"))
    }

    it ("""should map 'a' to "abc"""") {
      val x = new X()
      x += "abc"
      assert(x.byFirstLetter('a') == Set("abc"))
    }

    it ("""should map 'a' to "abc" and 'd' to "def"""") {
      val x = new X()
      x += ("abc", "def")
      assert(x.byFirstLetter('a') == Set("abc"))
      assert(x.byFirstLetter('d') == Set("def"))
    }

    it ("""should map 'a' to "abc" and "aaa"""") {
      val x = new X()
      x += ("abc", "aaa")
      assert(x.byFirstLetter('a') == Set("abc", "aaa"))
    }

    it ("""should retain its elements when the index is destroyed""") {
      val x = new X()
      x += ("abc", "def")
      x.byFirstLetter.destroy()
      assert(x == Set("abc", "def"))
    }

    it ("""should have an index with keys (a, b) after (abc, aaa, bcd) are added""") {
      val x = new X()
      x += ("abc", "aaa", "bcd")
      assert(x.byFirstLetter.keys == Set('a', 'b'))
    }

    it ("""should have an index with keys (a, b) after (aaa, bbb) are added""") {
      val x = new X()
      x += ("aaa", "bbb")
      x -= "bbb"
      assert(x.byFirstLetter.keys == Set('a'))
    }

    it ("""should have an index with key (a) after (aaa, bbb) are added and (bbb) is removed""") {
      val x = new X()
      x += ("aaa", "bbb")
      x -= "bbb"
      assert(x.byFirstLetter.keys == Set('a'))
    }

  }

  describe ("""A PolyMap[String] indexed by the first and second letters""") {

    class X extends PolyMap[String] {
      val byFirstLetter = index(_(0))
      val bySecondLetter = index(_(1))
    }

    it ("""should first-letter map 'a' to "abc" and "aaa"""") {
      val x = new X()
      x += ("abc", "aaa")
      assert(x.byFirstLetter.toSet == Set('a' -> "abc", 'a' -> "aaa"))
    }

    it ("""should second-letter map 'b' to "abc" and 'a' to "aaa"""") {
      val x = new X()
      x += ("abc", "aaa")
      assert(x.bySecondLetter.toSet == Set('b' -> "abc", 'a' -> "aaa"))
    }

    it ("""should have 2 indexes""") {
      val x = new X()
      assert(x.state match {
        case s: x.State.SomeIndexes if s.indexes.size == 2 => true
        case _ => false
      })
    }

    it ("""should have 1 index after 1 index is destroyed""") {
      val x = new X()
      x.bySecondLetter.destroy()
      assert(x.state match {
        case s: x.State.SomeIndexes if s.indexes.size == 1 => true
        case _ => false
      })
    }

    it ("""should have NoIndexes state after both indexes are destroyed""") {
      val x = new X()
      x.byFirstLetter.destroy()
      x.bySecondLetter.destroy()
      assert(x.state match {
        case s: x.State.NoIndexes => true
        case _ => false
      })
    }

    it ("""should retain its elements after one index is destroyed""") {
      val x = new X()
      x += ("abc", "def", "aaa")
      x.byFirstLetter.destroy()
      assert(x == Set("abc", "def", "aaa"))
    }

    it ("""should retain its elements after both indexes are destroyed""") {
      val x = new X()
      x += ("abc", "def", "aaa")
      x.byFirstLetter.destroy()
      x.bySecondLetter.destroy()
      assert(x == Set("abc", "def", "aaa"))
    }

    it ("""should retain its elements after both indexes are destroyed and re-created""") {
      val x = new X()
      x += ("abc", "def", "aaa")
      x.byFirstLetter.destroy()
      x.bySecondLetter.destroy()
      x.createIndex(_(0))
      x.createIndex(_(1))
      assert(x == Set("abc", "def", "aaa"))
    }

    it ("""should allow removal by an index's key""") {
      val x = new X()
      x += ("abc", "bac", "def", "aaa")
      x.bySecondLetter.remove('a')
      assert(x == Set("abc", "def"))
      assert(x.byFirstLetter('a') == Set("abc"))
    }

  }

}