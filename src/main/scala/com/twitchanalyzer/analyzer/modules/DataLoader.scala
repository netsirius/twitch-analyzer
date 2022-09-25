package com.twitchanalyzer.analyzer.modules

import com.twitchanalyzer.analyzer.api.{Helix, V5Api}
import com.twitchanalyzer.analyzer.chekpoint.Checkpoint
import com.twitchanalyzer.analyzer.config.{TWITCH_CHATS_DIR, TWITCH_USERS_FILE}
import com.twitchanalyzer.analyzer.models.VideoInfo
import org.apache.spark.sql.{
  DataFrame,
  Dataset,
  Encoder,
  Encoders,
  SparkSession
}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.functions.{col, explode}

import scala.io.Source

object DataLoader {
  def readVodFromFile[T <: Product]()(
    implicit spark: SparkSession,
    tag: scala.reflect.runtime.universe.TypeTag[T]
  ): Dataset[T] = {
    implicit val encoder: Encoder[T] = Encoders.product[T]
    spark.read.schema(encoder.schema).json(TWITCH_CHATS_DIR).as[T]
  }

  private def getStreamersToAnalyze: Seq[String] = {
    val usersSource = Source.fromFile(TWITCH_USERS_FILE)
    val users: Seq[String] = usersSource.getLines().mkString.split(',').toSeq
    usersSource.close()
    Helix.getUsers(users)
  }

  def loadChats(
    limit: Option[Int] = None
  )(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    val streamers = getStreamersToAnalyze
    val videos: Seq[VideoInfo] = Helix.getVideos(streamers, limit)

    // Only get not processed videos
    val filteredVideos: Seq[VideoInfo] = videos
      .groupBy(v => v.user_login.get)
      .flatMap((group: (String, Seq[VideoInfo])) => {
        group._2
          .filter(_.id.get.toInt > Checkpoint.getLastVideo(group._1).toInt)
      })
      .toSeq

    // Update checkpoint for each streamer
    filteredVideos
      .groupBy(v => v.user_login.get)
      .foreach((group: (String, Seq[VideoInfo])) => {
        val lastVideo: String = group._2.maxBy(_.id).id.get
        Checkpoint.updateCheckpoint(group._1, lastVideo)
      })

    val chats = spark
      .createDataset(filteredVideos)
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
    flattenedChats
  }

}
