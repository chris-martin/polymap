PolyMap
=======

This data structure ought to have been named
"Multi-Index Hash Multi-Map", but that was too long.

Example usage
-------------

```scala
import org.codeswarm.polymap._

case class Person(id: Int, name: String)

val people = new PolyMap[Person] {
  val byId = index(_.id)
  val byName = index(_.name)
}

// get people named Alice
people.byName("Alice")

// remove people with id 3
people.byId.remove(3)
```
