package com.twitchanalyzer.analyzer.api

import com.twitchanalyzer.analyzer.config
import com.twitchanalyzer.analyzer.models.{
  Pagination,
  User,
  UsersInfo,
  VideoInfo,
  VideosInfo
}
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, _}

object Helix extends Client {

  private val OAUTH_TOKEN: String = getOauthToken
  private val HEADERS: Map[String, String] = Map(
    "client-id" -> config.CLIENT_ID,
    "authorization" -> s"Bearer $OAUTH_TOKEN",
    "accept" -> "application/vnd.twitchtv.v5+json; charset=UTF-8",
  )

  def getUsers(users: Seq[String]): Seq[String] = {
    val params = users.map(user => s"login=$user").mkString("&")
    val url = s"${config.HELIX_BASE_ENDPOINT}/users?$params"
    performRequest(url, HEADERS) match {
      case Right(response: String) =>
        response.parseJson.convertTo[UsersInfo].data.map(_.id)
    }
  }

  def getVideos(users: Seq[String], limit: Option[Int]): Seq[VideoInfo] = {
    val params = users.map(user => s"user_id=$user").mkString("&")
    val url = limit match {
      case Some(first) =>
        s"${config.HELIX_BASE_ENDPOINT}/videos?$params&first=$first&sort=time"
      case None =>
        s"${config.HELIX_BASE_ENDPOINT}/videos?$params&sort=time"
    }
    performRequest(url, HEADERS) match {
      case Right(response: String) =>
        response.parseJson.convertTo[VideosInfo].data
    }
  }

  // Response formats
  private implicit val userFormat: JsonFormat[User] = jsonFormat10(User)
  private implicit val usersFormat: RootJsonFormat[UsersInfo] = jsonFormat1(
    UsersInfo
  )

  private implicit val paginationFormat: JsonFormat[Pagination] = jsonFormat1(
    Pagination
  )
  private implicit val videoFormat: JsonFormat[VideoInfo] = jsonFormat17(
    VideoInfo
  )
  private implicit val videosFormat: RootJsonFormat[VideosInfo] = jsonFormat2(
    VideosInfo
  )
}
