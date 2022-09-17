package com.twitchanalyzer.analyzer.modules

import com.vader.sentiment.analyzer.SentimentAnalyzer
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, explode, first, udf}

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
      var sentiment: Seq[(String, String)] = Seq()
      sentimentAnalyzer.getPolarity.forEach((k, v) => {
        sentiment = sentiment :+ (k, v.toString)
      })
      sentiment
    })

    spark.udf
      .register("sentiment", sentiment)

    val groupingCols: Seq[String] = chats.columns.toSeq
    chats
      .withColumn("sentiment", explode(sentiment(col("message"))))
      .groupBy(groupingCols.head, groupingCols: _*)
      .pivot("sentiment._1")
      .agg(first("sentiment._2"))
      .repartition(col("streamer_id"), col("vod_id"))
  }
}
