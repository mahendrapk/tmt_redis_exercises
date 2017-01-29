package com.thoughtworks.pubdubdemo

import java.net.InetSocketAddress

import akka.actor.Props
import akka.util.ByteString
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}

object PatternSubscriberApp extends App {
  implicit val akkaSystem = akka.actor.ActorSystem()
  akkaSystem.actorOf(Props(classOf[PatternSubscriberActor], Seq("Lifecycle.Exit"), Seq("Service.*")).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
}

class PatternSubscriberActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(
    new InetSocketAddress("localhost", 6379),
    channels,
    patterns,
    onConnectStatus = connected => { println(s"connected: $connected")}
  ) {

  def onMessage(message: Message) {
    message match {
      case Message("Lifecycle.Exit", s) => println(s"Received Exit message from Publisher. So exiting."); context.stop(self)
      case Message(c, s) => println(s"Unknown channel : $c")
    }
  }

  def onPMessage(pmessage: PMessage) {
    pmessage match {
      case PMessage("Service.*", "Service.Word", s) => println(s" Word message received: ${s.decodeString(ByteString.UTF_8)}")
      case PMessage("Service.*", "Service.Time", s) => println(s" Time message received: ${s.decodeString(ByteString.UTF_8)}")
      case PMessage("Service.*", c, s) => println(s"Unknown channel: $c")
    }
  }
}