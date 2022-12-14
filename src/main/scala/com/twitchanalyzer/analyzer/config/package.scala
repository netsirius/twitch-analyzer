package com.twitchanalyzer.analyzer

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf

package object config {
  val SETTINGS: Config = ConfigFactory.load()

  // Twitch Config
  val TWITCH_CHATS_DIR: String = SETTINGS.getString("app.twitch.chatsDir")
  val TWITCH_USERS_FILE: String = SETTINGS.getString("app.twitch.usersFile")
  val CHECKPOINT_FILE_PATH: String =
    SETTINGS.getString("app.twitch.checkpointFile")
  val DATA_OUTPUT_PATH: String = SETTINGS.getString("app.output.outputPath")
  val DATA_OUTPUT_FORMAT: String = SETTINGS.getString("app.output.outputFormat")
  val HELIX_BASE_ENDPOINT: String = SETTINGS.getString("twitch.api.helixUrl")
  val V5_BASE_ENDPOINT: String = SETTINGS.getString("twitch.api.v5Url")
  val OAUTH_BASE_ENDPOINT: String = SETTINGS.getString("twitch.api.oauthUrl")
  val CLIENT_ID: String = SETTINGS.getString("twitch.client.id")
  val V5_CLIENT_ID: String = SETTINGS.getString("twitch.client.v5id")
  val SECRET: String = SETTINGS.getString("twitch.client.secret")

  // Spark config
  val SPARK_EXECUTORS_NUM: Int =
    SETTINGS.getInt("app.spark.numExecutors")
  val SPARK_EXECUTORS_CORES: Int =
    SETTINGS.getInt("app.spark.executorCores")
  val SPARK_PARTITIONS: Int = SPARK_EXECUTORS_CORES * SPARK_EXECUTORS_NUM * 3

  val SPARK_CONF: SparkConf = {
    val conf = new SparkConf()
    val options = Map(
      "spark.app.nam" -> "Twitch Analyzer",
      "spark.worker.cleanup.enabled" -> "true",
      "spark.shuffle.consolidateFiles" -> "true",
      "spark.default.parallelism" -> s"$SPARK_PARTITIONS",
      "spark.sql.shuffle.partitions" -> s"$SPARK_PARTITIONS",
      "spark.shuffle.service.enabled" -> "true",
      "spark.network.timeout" -> "120s",
      "spark.ui.retainedJobs" -> "300",
      "spark.ui.retainedStages" -> "400",
      "spark.ui.retainedTasks" -> "1000",
      "spark.ui.retainedDeadExecutors" -> "10",
      "spark.sql.warehouse.dir" -> "file:/tmp/spark-warehouse",
    )
    options.foreach {
      case (k, v) => conf.set(k, v)
    }
    conf
  }

}
