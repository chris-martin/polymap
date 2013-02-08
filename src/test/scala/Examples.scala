package org.codeswarm.polymap

import org.scalatest._

class Examples extends FreeSpec {

  "Simple person example" in {

    case class Person(id: Int, name: String)

    val people = new PolyMap[Person] {
      val byId = index(_.id)
      val byName = index(_.name)
    }

    people += (Person(1, "Alice"), Person(2, "Bob"), Person(3, "Alice"))

    info(people.byName("Alice").map(_.id).toSeq.sorted.mkString(", "))
    //
    // Result:
    //
    //     1, 3
    //

  }

  "Extended person example" in {

    // Person class with an identifier, name, and age.
    case class Person(id: Int, name: String, age: Int) {
      override def toString: String = "%s (age %d)".format(name, age)
    }

    // Order people by id (just to make the output more readable).
    implicit object PersonOrdering extends Ordering[Person] {
      def compare(x: Person, y: Person): Int = x.id compare y.id
    }

    // This PolyMap is a collection of people.
    val people = new PolyMap[Person] {

      val byId = index(_.id)      // We might want to look up people by id number,
      val byName = index(_.name)  // or we also might want to look up by name.

      // Print the collection as a list of id and name.
      override def toString(): String =
        toSeq.sorted.map(p => "%d-%s".format(p.id, p.name)).mkString(", ")
    }

    // Add some people to the collection.
    people += (
      Person(1, "Alice", 24),
      Person(2, "Bob", 47),
      Person(3, "Alice", 32),
      Person(4, "Eve", 12)
    )

    // Print the inital state of the collection.
    info("All people: " + people.toString())
    //
    // Result:
    //
    //     All people: 1-Alice, 2-Bob, 3-Alice, 4-Eve
    //

    // Find people named Alice.
    val alices = people.byName("Alice")
    info("People named Alice: " + alices.toSeq.sorted.map(_.id).mkString(", "))
    //
    // Result:
    //
    //     People named Alice: 1, 3
    //

    // Find the person with id 4.
    people.byId(4) foreach { person4 => info("Person 4: " + person4) }
    //
    // Result:
    //
    //     Person 4: Eve (age 12)
    //

    // Remove people named Alice from the collection.
    people.byName.remove("Alice")

    // Print the new state of the collection with Alices removed.
    info("All people: " + people.toString())
    //
    // Result:
    //
    //     All people: 2-Bob, 4-Eve
    //

  }

}