package com.thoughtworks

import akka.util.ByteString
import redis.RedisClient

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object SimpleReadWriteApp extends App{

  implicit val akkaSystem = akka.actor.ActorSystem()

  val redis = RedisClient()
  val quoteKey = "Redis"
  val numberKey = "Number"

  def setRedisKeys(redis: RedisClient) = {
    //cleanup
    redis.del(numberKey)

    //add new
    redis.set(quoteKey, "means a 'RE'mote 'DI'ctionary 'S'ervice.")
    redis.incr(numberKey)
  }

  setRedisKeys(redis)

  val finalNumber: Future[Option[String]] = {
    for {
      iInitial <- redis.get(numberKey)
      incrByOne <- redis.incr(numberKey)
      iIntermediate <- redis.get(numberKey)
      incrByTen <- redis.incrby(numberKey, 10)
      iFinal <- redis.get(numberKey)
    } yield {
      val i1: Option[String] = iInitial.map(_.utf8String)
      val i2: Option[String] = iIntermediate.map(_.utf8String)
      val i12: Option[String] = iFinal.map(_.utf8String)
      println(s"Initially i was $i1, then I was made $i2 and finally i am $i12")
      i12
    }
  }

  val quoteResult: Future[Option[String]] = {
    for {
      quoteOption <- redis.get(quoteKey)
    } yield {
      quoteOption.map(_.decodeString(ByteString.UTF_8))
    }
  }
  quoteResult.onComplete{
    case Success(quote) => quote match {
      case Some(s) => println(s"key: $quoteKey, val: $s")
      case None => s"Key $quoteKey not found"
    }
    case Failure(e) => "error occurred"
  }

  Await.result(quoteResult, 5 seconds)

  akkaSystem.terminate()
}