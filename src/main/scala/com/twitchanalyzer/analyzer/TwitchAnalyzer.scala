package com.twitchanalyzer.analyzer

import com.twitchanalyzer.analyzer
import org.apache.spark.sql.SparkSession
import com.twitchanalyzer.analyzer.modules.{DataLoader, Transformations}

object TwitchAnalyzer {
  def main(args: Array[String]): Unit = {
    // Setting up spark
    implicit val spark: SparkSession = getSparkSession
    spark.sparkContext.setLogLevel("WARN")

    val chats = DataLoader.loadChats(Some(10))
    val flattened = Transformations.getFlattenedMessages(chats)
    val chatsWithSentiment = Transformations.analyzeSentiment(flattened)
    val metrics = Transformations.getUserMetrics(chatsWithSentiment)
    metrics
      .coalesce(analyzer.config.SPARK_PARTITIONS)
      .write
      .format(analyzer.config.DATA_OUTPUT_FORMAT)
      .option("header", "true")
      .save(analyzer.config.DATA_OUTPUT_PATH)
    flattened.unpersist()
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
