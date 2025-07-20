package com.example.watchappgesture.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.watchappgesture.presentation.Github.GitHubUser
import com.example.watchappgesture.presentation.Github.QRPreview
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    userInfo: GitHubUser?,
    shareAccountQR: Boolean,
    onToggleQR: () -> Unit,
    token: String,
) {
    val listState = rememberScalingLazyListState()
    var showNotifications by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            userScrollEnabled = !showNotifications && !shareAccountQR, // Stop scrolling when notifications or QR are visible
        ) {
            item {
                AsyncImage(
                    model = userInfo?.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
            }
            item {
                Spacer(modifier = Modifier.size(4.dp))
            }
            item {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Hey, ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(userInfo?.name ?: "User")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "@${userInfo?.login}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }
            item {
                CompactButton(
                    enabled = !shareAccountQR,
                    onClick = onToggleQR,
                    label = { Text(text = "Share Account QR") },
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                )
            }
            item {
                CompactButton(
                    onClick = { showNotifications = true },
                    label = { Text("Notifications") },
                )
            }
            item {
                Spacer(modifier = Modifier.size(8.dp))
            }
            item {
                Text(
                    text = "Stats",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            item {
                val totalRepos = (userInfo?.publicRepos ?: 0)
                Text(
                    text = "Public Repos: $totalRepos",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
            item {
                Text(
                    text = "Followers: ${userInfo?.followers ?: "?"}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
            item {
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = userInfo?.created_at?.let { dateStr ->
                        val sinceText = "Joined: "
                        try {
                            val inputFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                Locale.US
                            )
                            val outputFormat =
                                SimpleDateFormat("MMMM yyyy", Locale.US)
                            sinceText + outputFormat.format(inputFormat.parse(dateStr)!!)
                        } catch (e: Exception) {
                            sinceText + "?"
                        }
                    } ?: "Joined: ?",
                    fontSize = 12.sp,
                )
            }
        }

        ScrollIndicator(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(horizontal = 4.dp)
        )

        if (shareAccountQR) {
            val state = androidx.wear.compose.foundation.rememberSwipeToDismissBoxState()
            SwipeToDismissBox(
                state = state,
                onDismissed = { onToggleQR() },
                backgroundScrimColor = Color.Black,
                contentScrimColor = Color.Black // <--- ADDED THIS LINE
            ) { isBackground ->
                // Show completely black
                if (!isBackground) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        QRPreview(
                            url = userInfo?.html_url,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                } else {
                    // This is the background content when swiping.
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            }
        }

        if (showNotifications) {
            val state = androidx.wear.compose.foundation.rememberSwipeToDismissBoxState()
            SwipeToDismissBox(
                state = state,
                onDismissed = { showNotifications = false },
                backgroundScrimColor = Color.Black,
                contentScrimColor = Color.Black // <--- ADDED THIS LINE
            ) { isBackground ->
                // Show completely black
                if (!isBackground) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        NotificationScreen(
                            onDismiss = { showNotifications = false },
                            token = token,
                        )
                    }
                } else {
                    // This is the background content when swiping.
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            }
        }
    }
}