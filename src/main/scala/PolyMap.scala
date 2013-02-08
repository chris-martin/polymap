package org.codeswarm.polymap

import scala.collection.{immutable, mutable}

/** A set backed by `n` (0 or more) hash-based multimaps keyed on properties of the set's
  * elements.
  *
  * == Example usage ==
  * {{{
  * case class Person(id: Int, name: String)
  *
  * val people = new PolyMap[Person] {
  *   val byId = index(_.id)
  *   val byName = index(_.name)
  * }
  *
  * // get people named Alice
  * people.byName("Alice")
  *
  * // remove people with id 3
  * people.byId.remove(3)
  * }}}
  * @tparam Element the type of the elements of the set
  */
class PolyMap[Element] extends mutable.Set[Element] {

  override def size = anIndex.size

  override def iterator: Iterator[Element] = anIndex.iterator.map(_._2)

  override def add(elem: Element): Boolean = state add elem
  def +=(elem: Element): this.type = { add(elem); this }

  override def remove(elem: Element): Boolean = state remove elem
  def -=(elem: Element): this.type = { remove(elem); this }

  def contains(elem: Element): Boolean = anIndex containsElement elem

  override def clear() { state foreach (_.clear())  }

  /** An arbitrarily-chosen index. State is required to have at least one index, so this
    * always succeeds.
    */
  private[polymap] def anIndex: IndexPrivate[_] = state.head

  private[polymap] var state: State = new State.NoIndexes()

  private[polymap] sealed trait State extends Iterable[IndexPrivate[_]] {

    def add(elem: Element): Boolean = {
      var changed = false
      for (index <- state) if (index addElement elem) changed = true
      changed
    }
    def +=(elem: Element): this.type = { add(elem); this }

    def remove(elem: Element): Boolean = {
      var changed = false
      for (index <- state) if (index removeElement elem) changed = true
      changed
    }
    def -=(elem: Element): this.type = { remove(elem); this }

  }

  private[polymap] object State {

    /** The default state when there are no indexes. This has one index that simply maps every
      * element to itself to preserve the set of elements when there are no other indexes to
      * hold them.
      */
    class NoIndexes extends State {

      val index = new IndexPrivate[Element](identity)

      def iterator: Iterator[IndexPrivate[_]] = Iterator(index)
      override def size: Int = 1

      override def toString(): String = "NoIndexes"

    }

    class SomeIndexes extends State {

      val indexes = new mutable.HashSet[IndexPrivate[_]]()

      def iterator: Iterator[IndexPrivate[_]] = indexes.iterator
      override def size: Int = indexes.size

      override def toString(): String = "SomeIndexes(%d)".format(indexes.size)

    }

  }

  /** A single multimap.
    *
    * == Mutation ==
    * Changes to the `PolyMap` affect each `Index`, and vice versa (therefore a change
    * to an `Index` also affects other `Index`es).
    *
    * == Lifecycle ==
    * Typically an `Index` should be created in the constructor of a `PolyMap` subtype,
    * and lives and dies with the `PolyMap`. But `Index`es may also be created and at
    * any time, and may be removed by invoking `destroy()`.
    */
  trait Index[Key] extends Iterable[(Key, Element)] with Function[Key, immutable.Set[Element]] {

    /** Removes this `Index` from the `PolyMap`. The `Index` becomes forever unusable.
      * No methods should be invoked upon it after this, except for `destroy`, which
      * may be invoked repeatedly (although subsequent invocations have no effect).
      */
    def destroy()

    /** @return Elements of the `PolyMap` that are associated with `key` in this `Index`.
      */
    def get(key: Key): immutable.Set[Element]

    /** Equivalent to `get(Key)`.
      */
    def apply(key: Key): immutable.Set[Element] = get(key)

    /** Finds elements that are associated with `key` in this `Index`, and removes them
      * from the `PolyMap`.
      * @return the elements that were removed
      */
    def remove(key: Key): immutable.Set[Element] = {
      val elems = get(key)
      PolyMap.this --= elems
      elems
    }

  }

  private[polymap] class IndexPrivate[Key](keyFor: Element => Key,
      var underlying: MultiMap[Key, Element] = new MultiMap[Key, Element]) extends Index[Key] {

    def keyValuePair(elem: Element): (Key, Element) = (keyFor(elem), elem)

    def get(key: Key): immutable.Set[Element] = underlying.get(key)
    def iterator: Iterator[(Key, Element)] = underlying.iterator
    def elements: Iterable[Element] = map(_._2)

    override def size: Int = underlying.size
    override def toSet[B >: (Key, Element)]: Set[B] = underlying.toSet

    def containsElement(elem: Element): Boolean = underlying contains keyValuePair(elem)

    def addElement(elem: Element): Boolean = underlying add keyValuePair(elem)
    def +=(elem: Element): this.type = { addElement(elem); this }

    def removeElement(elem: Element): Boolean = underlying remove keyValuePair(elem)
    def -= (elem: Element): this.type = { removeElement(elem); this }

    def clear() { underlying.clear() }

    def destroy() {

      // Multiple calls to destroy have no effect
      if (underlying == null) return

      PolyMap.this.state match {

        case someIndexes: State.SomeIndexes =>

          // Remove this index from the index set
          val removed = someIndexes.indexes remove IndexPrivate.this
          assert(removed)

          // Switch to no-indexes mode if necessary
          if (someIndexes.indexes.isEmpty) {
            val noIndexes = new State.NoIndexes
            PolyMap.this.state = noIndexes
            PolyMap.this ++= IndexPrivate.this.elements
          }

        case _ => assert(false)
      }

      underlying = null
    }

  }

  /** Creates a new index for this `PolyMap`.
    * @param f Defines the `Key` for each `Element`. Should never throw an exception.
    * @return The index that was created.
    */
  def createIndex[Key](f: Element => Key): Index[Key] = {

    // Create the new index
    val index = new IndexPrivate[Key](f)

    // Fill in the new index with the existing elements
    for (elem <- this) index += elem

    // Switch to some-indexes mode if not already there
    val someIndexes = state match {
      case x: State.SomeIndexes => x
      case _: State.NoIndexes =>
        val x = new State.SomeIndexes
        state = x
        x
    }
    state = someIndexes

    // Add the new index to the set of indexes
    someIndexes.indexes += index

    index
  }

  /** Alias for `createIndex` designed to be used in a `PolyMap` subtype constructor.
    */
  protected def index[Key](f: Element => Key): Index[Key] = createIndex(f)

}