package com.petlew.gacek.visualisation

case class Node(id: Int, name: String, mailboxSize: Int)

case class Link(source: Int, target: Int, weight: Int)

case class GraphRepresentation(nodes: Set[Node], links: Set[Link])

object GraphBuilder {

  def buildGraph(events: Set[EnqueueEvent]): GraphRepresentation = {
    val names = events.map(_.receiver) ++ events.map(_.sender)
    val nodes = names.zipWithIndex.map { case (name, idx) =>
      Node(
        idx, name,
        events.filter(_.receiver == name)
          .toList
          .sortBy(_.timestamp)
          .lastOption
          .map(_.mailboxSize)
          .getOrElse(0)
      )
    }
    val links = events.groupBy(event => (event.sender, event.receiver))
      .mapValues(_.size)
      .map { case ((from, to), size) => {
        Link(
          nodes.filter(_.name == from).head.id,
          nodes.filter(_.name == to).head.id,
          size
        )
      }
      }.toSet
    GraphRepresentation(nodes, links)
  }

}