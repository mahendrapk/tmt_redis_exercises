package com.thoughtworks.pubdubdemo

import redis.RedisClient

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

object Publisher extends App {

  implicit val akkaSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  private val dictionary: List[String] = loadDictionary
  private def loadDictionary: List[String] = {
    val dict = Source.fromFile("/usr/share/dict/words")
    dict.getLines().toList
  }

  println(s"Dictionary initialized with ${dictionary.size} words. Starting to publish..")
  akkaSystem.scheduler.schedule(2 seconds, 3 seconds)(redis.publish("Service.Word", dictionary(Random.nextInt(dictionary.size))))
  akkaSystem.scheduler.schedule(2 seconds, 1 seconds)(redis.publish("Service.Time", System.currentTimeMillis()))

  akkaSystem.scheduler.scheduleOnce(30 seconds)(redis.publish("Lifecycle.Exit", System.currentTimeMillis()))
  akkaSystem.scheduler.scheduleOnce(35 seconds)(akkaSystem.terminate())
}
