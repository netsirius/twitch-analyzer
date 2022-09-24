package com.twitchanalyzer.analyzer.api
import com.twitchanalyzer.analyzer.config
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, _}
import sttp.client3._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class Client {
  def CONNECTION_TIMEOUT_DURATION: FiniteDuration = 2.minute

  private implicit val tokenformat: RootJsonFormat[OauthToken] = jsonFormat3(
    OauthToken
  )

  private[api] def performRequest(
    request_url: String
  ): Either[String, String] = {
    performRequest(request_url, Map.empty[String, String])
  }

  private[api] def performRequest(
    request_url: String,
    headers: Map[String, String]
  ): Either[String, String] = {
    val sttpBackend = HttpURLConnectionBackend(
      options =
        SttpBackendOptions.connectionTimeout(CONNECTION_TIMEOUT_DURATION)
    )
    basicRequest
      .get(uri"$request_url")
      .headers(headers)
      .contentType("application/json")
      .response(asString)
      .send(sttpBackend)
      .body
  }

  private[api] def getOauthToken: String = {
    val url =
      s"${config.OAUTH_BASE_ENDPOINT}?client_id=${config.CLIENT_ID}&client_secret=${config.SECRET}&grant_type=client_credentials"
    val sttpBackend = HttpURLConnectionBackend(
      options =
        SttpBackendOptions.connectionTimeout(CONNECTION_TIMEOUT_DURATION)
    )
    val response = basicRequest
      .post(uri"$url")
      .contentType("application/json")
      .response(asString)
      .send(sttpBackend)
      .body
    response match {
      case Right(response: String) =>
        val res = response.parseJson.convertTo[OauthToken]
        res.access_token
    }
  }

}

case class OauthToken(access_token: String,
                      expires_in: Double,
                      token_type: String)
