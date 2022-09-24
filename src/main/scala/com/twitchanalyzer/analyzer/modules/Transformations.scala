package com.twitchanalyzer.analyzer.modules

import com.vader.sentiment.analyzer.SentimentAnalyzer
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

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
      .groupBy(groupingCols.head, groupingCols.tail: _*)
      .pivot("sentiment._1")
      .agg(first("sentiment._2").cast("float"))
      .repartition(col("streamer_id"), col("vod_id"))
  }

  def getUserMetrics(
    chats: DataFrame
  )(implicit spark: SparkSession): DataFrame = {
    chats
      .groupBy("streamer_id", "vod_id", "vod_title", "user_id", "user_name")
      .agg(
        sum("bits").as("total_bits_spend"),
        first("subscriber").as("subscriber_level"),
        first("bits-leader").as("bits_leader_position"),
        first("sub-gift-leader").as("sub_gift_leader"),
        first("sub-gifter").as("is_sub_gifter"),
        mean("compound").as("mean_compound_sentiment"),
        mean("negative").as("mean_negative_sentiment"),
        mean("neutral").as("mean_neutral_sentiment"),
        mean("positive").as("mean_positive_sentiment")
      )
      .na
      .fill(0)
      .na
      .fill("0")
      .repartition(col("streamer_id"), col("vod_id"))
  }
}
