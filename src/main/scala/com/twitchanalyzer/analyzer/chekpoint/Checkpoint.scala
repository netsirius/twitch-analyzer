package com.twitchanalyzer.analyzer.chekpoint

import com.twitchanalyzer.analyzer.config
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, _}

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

case class CheckpointState(states: Seq[StreamerState])
case class StreamerState(streamer_id: String, last_vod_id: String)

object Checkpoint {

  private def getCheckpoint: Option[CheckpointState] = {
    if (scala.reflect.io.File(config.CHECKPOINT_FILE_PATH).exists) {
      val checkpointSource = Source.fromFile(config.CHECKPOINT_FILE_PATH)
      val checkpointContent = checkpointSource.getLines.mkString.parseJson
        .convertTo[CheckpointState]
      checkpointSource.close()
      Some(checkpointContent)
    } else None
  }

  private def existsState(checkpointState: CheckpointState,
                          stremaerId: String): Boolean = {
    checkpointState.states.exists(
      _.streamer_id.toLowerCase == stremaerId.toLowerCase
    )
  }

  def updateCheckpoint(streamerId: String, lastVodId: String) = {
    try {
      // Update checkpoint state
      val updatedCheckpoint: CheckpointState = getCheckpoint match {
        case Some(checkpoint: CheckpointState) =>
          if (existsState(checkpoint, streamerId)) {
            checkpoint.states.map(state => {
              if (state.streamer_id.toLowerCase == streamerId.toLowerCase)
                StreamerState(streamer_id = streamerId, last_vod_id = lastVodId)
              else state
            })
            checkpoint
          } else {
            val newStates
              : Seq[StreamerState] = checkpoint.states :+ StreamerState(
              streamer_id = streamerId,
              last_vod_id = lastVodId
            )
            CheckpointState(newStates)
          }
        case None =>
          val newStreamerState: StreamerState =
            StreamerState(streamer_id = streamerId, last_vod_id = lastVodId)
          CheckpointState(states = Seq(newStreamerState))
      }

      // Overwrite the file with new checkpoint changes
      val file = new File(config.CHECKPOINT_FILE_PATH)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(updatedCheckpoint.toJson.toString)
      bw.close()

    } catch {
      case e: Exception =>
        println(s"Error updating the checkpoint state for streamer $streamerId")
    }
  }

  def getLastVideo(streamerId: String): String = {
    getCheckpoint match {
      case Some(checkpoint: CheckpointState) =>
        checkpoint.states
          .filter(_.streamer_id.toLowerCase == streamerId.toLowerCase)
          .last
          .last_vod_id
      case None => "0"
    }
  }

  private implicit val stateFormat: JsonFormat[StreamerState] = jsonFormat2(
    StreamerState
  )
  private implicit val videosFormat: RootJsonFormat[CheckpointState] =
    jsonFormat1(CheckpointState)
}
