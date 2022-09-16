package com.twitchanalyzer.analyzer.models

case class User(id: String,
                login: String,
                display_name: String,
                `type`: String,
                broadcaster_type: String,
                description: String,
                profile_image_url: String,
                offline_image_url: String,
                view_count: Double,
                created_at: String)
case class UsersInfo(data: List[User])
