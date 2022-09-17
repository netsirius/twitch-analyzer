package com.twitchanalyzer.analyzer.modules

import com.twitchanalyzer.analyzer.api.{Helix, V5Api}
import com.twitchanalyzer.analyzer.config.{TWITCH_CHATS_DIR, TWITCH_USERS_FILE}
import org.apache.spark.sql.{
  DataFrame,
  Dataset,
  Encoders,
  SparkSession,
}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.functions.{col, explode}

import scala.io.Source

object DataLoader {
  def readVodFromFile[T <: Product]()(
    implicit spark: SparkSession,
    tag: scala.reflect.runtime.universe.TypeTag[T]
  ): Dataset[T] = {
    implicit val encoder = Encoders.product[T]
    spark.read.schema(encoder.schema).json(TWITCH_CHATS_DIR).as[T]
  }

  private def getStreamersToAnalyze(): Seq[String] = {
    val usersSource = Source.fromFile(TWITCH_USERS_FILE)
    val users: Seq[String] = usersSource.getLines().mkString.split(',').toSeq
    usersSource.close()
    Helix.getUsers(users)
  }

  def loadChats(limit: Option[Int])(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    val streamers = getStreamersToAnalyze()
    val videos = Helix.getVideos(streamers, limit)
    val chats = spark
      .createDataset(videos)
      .repartition()
      .map(vod => {
        val data = V5Api.getChats(vod.id.get)
        (vod.user_id, vod.user_login, vod.id, vod.title, data)
      })
    val flattenedChats = chats
      .select(
        col("_1").as("streamer_id"),
        col("_2").as("streamer_name"),
        col("_3").as("vod_id"),
        col("_4").as("vod_title"),
        explode(col("_5")).as("chats"),
      )
      .select("streamer_id", "streamer_name", "vod_id", "vod_title", "chats.*")
    flattenedChats.persist(StorageLevel.MEMORY_ONLY)
  }

}
