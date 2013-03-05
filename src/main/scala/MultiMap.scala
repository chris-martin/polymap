package org.codeswarm.polymap

import collection.{immutable, mutable}

private[polymap] class MultiMap[A, B] extends mutable.Set[(A, B)] {

  private val underlying = new mutable.HashMap[A, mutable.Set[B]]

  private var _size = 0

  /** The total number of mappings in the set.
    * For example, [ (1 -> [2]), (2 -> [1, 2]) ] has size 3.
    */
  override def size: Int = _size

  def iterator: Iterator[(A, B)] = underlying.iterator flatMap {
    case (a, bs) => bs map { b => (a, b) }
  }

  def get(key: A): immutable.Set[B] = underlying.get(key).toSet.flatten

  def keys: collection.Set[A] = underlying.keySet

  def contains(key: A, value: B): Boolean =
    underlying.get(key) match {
      case None =>
        false
      case Some(set) =>
        set contains value
    }

  /** Tupled variant of `contains(A, B)`.
    */
  def contains(kv: (A, B)): Boolean = contains(kv._1, kv._2)

  /** @return true if the collection was modified, false if key->value was already mapped
    */
  def add(key: A, value: B): Boolean =
    underlying.get(key) match {
      case None =>
        val set = makeSet
        set += value
        underlying(key) = set
        _size += 1
        true
      case Some(set) =>
        val added = set add value
        if (added) _size += 1
        added
    }

  /** Tupled variant of `add(A, B)`.
    */
  override def add(kv: (A, B)): Boolean = add(kv._1, kv._2)

  /** Variant of `add((A, B))` returning `this`.
    */
  def +=(kv: (A, B)): this.type = { add(kv._1, kv._2); this }

  /** @return true if the collection was modified, false if key->value was not mapped
    */
  def remove(key: A, value: B): Boolean =
    underlying.get(key) match {
      case None =>
        false
      case Some(set) =>
        val r = set remove value
        if (r) {
          _size -= 1
          if (set.isEmpty) {
            underlying remove key
          }
        }
        r
    }

  /** Tupled variant of `remove(A, B)`.
    */
  override def remove(kv: (A, B)): Boolean = remove(kv._1, kv._2)

  /** Variant of `remove((A, B))` returning `this`.
    */
  def -=(kv: (A, B)): this.type = { remove(kv._1, kv._2); this }

  override def clear() {
    underlying.clear()
    _size = 0
  }

  def makeSet: mutable.Set[B] = new mutable.HashSet[B]

}