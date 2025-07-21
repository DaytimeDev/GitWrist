package com.example.watchappgesture.presentation.Github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepository(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "private") val isPrivate: Boolean,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "description") val description: String?,
    @Json(name = "owner") val owner: Owner,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "language") val language: String?,
    @Json(name = "visibility") val visibility: String,
    @Json(name = "watchers_count") val watchersCount: Int,
    @Json(name = "open_issues") val openIssues: Int,
    @Json(name = "stargazers_count") val stargazersCount: Int,
    @Json(name = "forks_count") val forksCount: Int,
)

@JsonClass(generateAdapter = true)
data class Owner(
    @Json(name = "login") val login: String,
    @Json(name = "id") val id: Long,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val htmlUrl: String
)