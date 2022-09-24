package com.twitchanalyzer.analyzer.models

case class Commenter(display_name: Option[String],
                     _id: Option[String],
                     name: Option[String],
                     `type`: Option[String],
                     bio: Option[String],
                     created_at: Option[String],
                     updated_at: Option[String],
                     logo: Option[String])
case class Fragments(text: Option[String])
case class User_badges(_id: Option[String], version: Option[String])
case class User_notice_params()
case class Message(body: Option[String],
                   fragments: Option[List[Fragments]],
                   is_action: Option[Boolean],
                   user_badges: Option[List[User_badges]],
                   user_color: Option[String],
                   user_notice_params: Option[User_notice_params])
case class Comments(_id: Option[String],
                    created_at: Option[String],
                    updated_at: Option[String],
                    channel_id: Option[String],
                    content_type: Option[String],
                    content_id: Option[String],
                    content_offset_seconds: Option[Double],
                    commenter: Option[Commenter],
                    source: Option[String],
                    state: Option[String],
                    message: Option[Message])
case class CommentsInfo(comments: List[Comments], _next: Option[String])
