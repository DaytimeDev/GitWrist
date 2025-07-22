package com.example.watchappgesture.presentation.Github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepoEvent(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "actor") val actor: Actor,
    @Json(name = "payload") val payload: Payload?,
    @Json(name = "public") val public: Boolean,
    @Json(name = "created_at") val createdAt: String,
)


data class Payload(
    val repository_id: Long?,
    val push_id: Long?,
    val size: Int?, // Make nullable
    val distinct_size: Int?,
    val ref: String?,
    val head: String?,
    val before: String?,
    val commits: List<Commit>?,
    val pull_request: PullRequest?,
)


@JsonClass(generateAdapter = true)
data class Actor(
    @Json(name = "id") val id: Long,
    @Json(name = "login") val login: String,
    @Json(name = "display_login") val displayLogin: String,
    @Json(name = "gravatar_id") val gravatarId: String,
    @Json(name = "url") val url: String,
    @Json(name = "avatar_url") val avatarUrl: String
)

data class PullRequest(
    val id: Long,
    val number: Int,
    val state: String,
    val title: String,
    val user: Owner,
    val created_at: String,
    val updated_at: String,
    val closed_at: String?,
    val merged_at: String?,
    val merge_commit_sha: String?,
    val url: String,
    val head: Head?,
    val base: Head?,
)
data class Head(
    val ref: String
)

@JsonClass(generateAdapter = true)
data class Commit(
    @Json(name = "sha") val sha: String,
    @Json(name = "author") val author: Author,
    @Json(name = "message") val message: String,
    @Json(name = "distinct") val distinct: Boolean,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class Author(
    @Json(name = "name") val name: String
)