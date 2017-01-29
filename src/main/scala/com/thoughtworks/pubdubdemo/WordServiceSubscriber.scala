package com.thoughtworks.pubdubdemo

import java.net.InetSocketAddress

import akka.actor.Props
import akka.util.ByteString
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}

object WordSubscriberApp extends App {
  implicit val akkaSystem = akka.actor.ActorSystem()
  akkaSystem.actorOf(Props(classOf[WordServiceSubscriberActor], Seq("Service.Word", "Lifecycle.Exit"), Seq("")).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
}

class WordServiceSubscriberActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(
    new InetSocketAddress("localhost", 6379),
    channels,
    patterns,
    onConnectStatus = connected => { println(s"connected: $connected")}
  ) {

  def onMessage(message: Message) {
    message match {
      case Message("Service.Word", s) => println(s" Word message received: ${s.decodeString(ByteString.UTF_8)}")
      case Message("Lifecycle.Exit", s) => println(s"Received Exit message from Publisher. So exiting."); context.stop(self)
      case Message(c, m) => println(s"Unknown message : $c")
    }
  }

  def onPMessage(pmessage: PMessage) {}
}