package com.example.watchappgesture.presentation.Github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String?,
    @Json(name = "avatar_url") val avatarUrl: String?,
    val bio: String?,
    @Json(name = "public_repos") val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val html_url: String? = null,
    val created_at: String? = null, // The format is in "2020-05-06T15:51:29Z"
)
