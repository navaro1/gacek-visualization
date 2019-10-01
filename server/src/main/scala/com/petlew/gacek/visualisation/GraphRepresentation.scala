package com.petlew.gacek.visualisation

case class Node(id: Int, name: String, mailboxSize: Int)

case class Link(from: Int, to: Int, weight: Int)

case class GraphRepresentation(nodes: Set[Node], links: Set[Link])
