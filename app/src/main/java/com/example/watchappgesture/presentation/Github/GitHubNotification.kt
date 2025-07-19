package com.example.watchappgesture.presentation.Github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubNotification(
    @Json(name = "id") val id: String,
    @Json(name = "unread") val unread: Boolean,
    @Json(name = "reason") val reason: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "last_read_at") val lastReadAt: String,
    val subject: Subject,
    @Json(name = "repository") val repository: Repository,
    @Json(name = "url") val url: String,
    @Json(name = "subscription_url") val subscriptionUrl: String,
)

@JsonClass(generateAdapter = true)
data class Subject(
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
)

@JsonClass(generateAdapter = true)
data class Repository(
    @Json(name = "full_name") val fullName: String
)