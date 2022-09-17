package com.twitchanalyzer.analyzer.api

import com.twitchanalyzer.analyzer.config
import com.twitchanalyzer.analyzer.models._
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, _}

object V5Api extends TwitchClient {

  private val HEADERS: Map[String, String] = Map(
    "client-id" -> config.V5_CLIENT_ID,
    "accept" -> "application/vnd.twitchtv.v5+json; charset=UTF-8",
  )

  def getChats(vod: String): Seq[Comments] = {
    val url: String = s"${config.V5_BASE_ENDPOINT}/videos/$vod/comments"
    performRequest(url, HEADERS) match {
      case Right(response: String) =>
        response.parseJson.convertTo[CommentsInfo].comments
    }
  }

  // Response formats
  private implicit val badgesFormat: JsonFormat[User_badges] = jsonFormat2(
    User_badges
  )
  private implicit val fragmentsFormat: JsonFormat[Fragments] = jsonFormat1(
    Fragments
  )
  private implicit val noticeFormat: JsonFormat[User_notice_params] =
    jsonFormat0(User_notice_params)
  private implicit val messageFormat: JsonFormat[Message] = jsonFormat6(Message)
  private implicit val commenterFormat: JsonFormat[Commenter] = jsonFormat8(
    Commenter
  )
  private implicit val commentFormat: JsonFormat[Comments] = jsonFormat11(
    Comments
  )
  private implicit val commentsFormat: RootJsonFormat[CommentsInfo] =
    jsonFormat2(CommentsInfo)
}
