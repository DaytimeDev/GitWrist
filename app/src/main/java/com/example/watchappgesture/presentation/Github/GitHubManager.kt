package com.example.watchappgesture.presentation.Github

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.wear.compose.material3.MaterialTheme
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
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


suspend fun getRepositories(token: String): List<GitHubRepository> {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.github.com/user/repos")
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

            val adapter = moshi.adapter<List<GitHubRepository>>(Types.newParameterizedType(List::class.java, GitHubRepository::class.java))
            adapter.fromJson(body) ?: throw Exception("Failed to parse JSON")
        }
    }
}


fun generateQrCode(content: String?, size: Int = 512, colorHex: String = "#FFFFFF"): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bmp = createBitmap(size, size, Bitmap.Config.RGB_565)
    val colorInt = Color.parseColor(colorHex)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp[x, y] = if (bitMatrix[x, y]) colorInt else Color.BLACK
        }
    }
    return bmp
}


@Composable
fun QRPreview(url: String?, modifier: Modifier) {
    val primaryColorToHexString = MaterialTheme.colorScheme.primary.toArgb().let { String.format("#%06X", 0xFFFFFF and it) }
    val qrCodeBitmap by produceState<Bitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.Default) { generateQrCode(url, 512, primaryColorToHexString) }
    }
    Box(
        modifier = modifier
    )
    {
        AsyncImage(
            model = qrCodeBitmap,
            contentDescription = "QR Code",
            modifier = Modifier.size(400.dp)
        )
    }
}