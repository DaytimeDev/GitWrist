package com.example.watchappgesture.presentation.Github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

suspend fun getUserInfo(token: String): GitHubUser {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.github.com/user")
        .header("Authorization", "Bearer $token")
        .build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val body = response.body?.string()
                ?: throw Exception("Empty body")

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val adapter = moshi.adapter(GitHubUser::class.java)
            adapter.fromJson(body) ?: throw Exception("Failed to parse JSON")
        }
    }
}


suspend fun getUserNotifications(token: String): List<GitHubNotification> {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.github.com/notifications")
        .header("Authorization", "Bearer $token")
        .build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val body = response.body?.string()
                ?: throw Exception("Empty body")

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val adapter = moshi.adapter<List<GitHubNotification>>(Types.newParameterizedType(List::class.java, GitHubNotification::class.java))
            adapter.fromJson(body) ?: throw Exception("Failed to parse JSON")
        }
    }
}


suspend fun markNotificationAsRead(token: String, notificationId: String) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.github.com/notifications/threads/$notificationId")
        .header("Authorization", "Bearer $token")
        .patch(RequestBody.create(null, ""))
        .build()

    withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
        }
    }
}