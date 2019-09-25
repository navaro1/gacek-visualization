package com.petlew.gacek.visualisation

sealed trait QueueEvent {
  val timestamp: Long
  val mailboxSize: Int
}

case class EnqueueEvent(timestamp: Long,
                        mailboxSize: Int,
                        receiver: String,
                        sender: String,
                        messageClassName: String)
    extends QueueEvent



