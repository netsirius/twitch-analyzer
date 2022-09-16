package com.twitchanalyzer.analyzer.models

case class VideoInfo(id: Option[String],
                     stream_id: Option[String],
                     user_id: Option[String],
                     user_login: Option[String],
                     user_name: Option[String],
                     title: Option[String],
                     description: Option[String],
                     created_at: Option[String],
                     published_at: Option[String],
                     url: Option[String],
                     thumbnail_url: Option[String],
                     viewable: Option[String],
                     view_count: Option[Double],
                     language: Option[String],
                     `type`: Option[String],
                     duration: Option[String],
                     muted_segments: Option[String])
case class Pagination(cursor: Option[String])
case class VideosInfo(data: List[VideoInfo], pagination: Option[Pagination])
