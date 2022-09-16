package com.twitchanalyzer.analyzer

import com.twitchanalyzer.analyzer.api.Helix

import scala.util.{Failure, Success}
import org.apache.spark.sql.SparkSession

import scala.concurrent.ExecutionContext.Implicits.global
import com.twitchanalyzer.analyzer.modules.{DataLoader, Transformations}
import org.apache.spark.storage.StorageLevel

import scala.io.Source

object TwitchAnalyzer {
  def main(args: Array[String]): Unit = {
    // Setting up spark
    implicit val spark: SparkSession = getSparkSession
    spark.sparkContext.setLogLevel("WARN")

    val chats = DataLoader.loadChats(Some(2))
    val flattened = Transformations.getFlattenedMessages(chats)
    val chatsWithSentiment = Transformations.analyzeSentiment(flattened)
    chatsWithSentiment.persist(StorageLevel.MEMORY_ONLY_SER)
    chatsWithSentiment.unpersist()
    chatsWithSentiment.show(10)
    chats.unpersist()
  }

  private def getSparkSession = {
    val conf = config.SPARK_CONF.clone()
    var builder = SparkSession
      .builder()
      .config(conf)
    sys.env.get("MASTER") match {
      case Some(v) => builder = builder.master(v)
      case _       =>
    }
    builder.getOrCreate()
  }
}
