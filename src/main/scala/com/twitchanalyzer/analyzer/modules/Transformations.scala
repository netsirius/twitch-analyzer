package com.twitchanalyzer.analyzer.modules

import com.vader.sentiment.analyzer.SentimentAnalyzer
import com.vader.sentiment.processor.TextProperties
import com.vader.sentiment.util.SentimentModifyingTokens
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, collect_set, explode, first, udf}
import org.apache.spark.storage.StorageLevel

import java.{lang, util}
//
object Transformations {
  def getFlattenedMessages(
    chats: DataFrame
  )(implicit spark: SparkSession): DataFrame = {
    chats
      .select(
        col("streamer_id"),
        col("streamer_name"),
        col("vod_id"),
        col("vod_title"),
        col("created_at"),
        col("commenter.name").as("user_name"),
        col("commenter._id").as("user_id"),
        col("message.body").as("message"),
        explode(col("message.user_badges")).as("badge")
      )
      .groupBy(
        "streamer_id",
        "streamer_name",
        "vod_id",
        "vod_title",
        "created_at",
        "user_name",
        "user_id",
        "message"
      )
      .pivot("badge._id")
      .agg(first("badge.version"))
      .repartition(col("streamer_id"), col("vod_id"))
  }

  def analyzeSentiment(
    chats: DataFrame
  )(implicit spark: SparkSession): DataFrame = {
    val sentiment = udf((message: String) => {
      val sentimentAnalyzer = new SentimentAnalyzer(message)
      sentimentAnalyzer.analyze()
      var sentiment: Map[String, String] = Map.empty
      sentimentAnalyzer.getPolarity.forEach(
        (k, v) => sentiment += (k -> v.toString)
      )
      sentiment
    })

    spark.udf
      .register("sentiment", sentiment)

    val cols: Seq[Column] = chats.columns.map(cn => col(cn)).toSeq
    chats
      .withColumn("sentiment", sentiment(col("message")))
  }
}
