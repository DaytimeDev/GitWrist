package com.example.watchappgesture.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.Text
import com.example.watchappgesture.presentation.Github.GitHubNotification
import com.example.watchappgesture.presentation.Github.getUserNotifications
import com.example.watchappgesture.presentation.Github.markNotificationAsRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NotificationScreen(token: String) {

    var notifications: List<GitHubNotification> by remember { mutableStateOf(emptyList<GitHubNotification>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            notifications = getUserNotifications(token)
            isLoading = false
            println("Got notifications: ${notifications.size}")
            println("Notifications: $notifications")
        } catch (e: Exception) {
            isLoading = false
            println("Error fetching notifications: ${e.message}")
            DialogState.dialogVisible = true
            DialogState.dialogTitle = "Uh oh..."
            DialogState.dialogDescription =
                "Failed to load notifications. That's not great.\n\nError: ${e.message}"
            // Call on dismiss to close the notification screen
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "Notifications Incoming...",
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        } else {
            val notificationListState = rememberScalingLazyListState()
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = notificationListState,
                userScrollEnabled = true
            ) {
                item {
                    Text(
                        text = "Notifications",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                // Say how many unread
                item {
                    Text(
                        text = "${notifications.size} Unread",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                items(notifications.size) { index ->
                    val notification = notifications[index]

                    var showButtons by remember { mutableStateOf(false) }
                    Card(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .clickable {
                                showButtons = !showButtons
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = notification.subject.type,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = notification.subject.title,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notification.repository.fullName,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Updated: " + notification.updatedAt.replace("T", " ")
                                    .replace("Z", ""),
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                            if (showButtons) {
                                // Mark as read button
                                var markAsReadRequested by remember { mutableStateOf(false) }
                                if (markAsReadRequested) {
                                    LaunchedEffect(notification.id) {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                markNotificationAsRead(token, notification.id)
                                            }

                                            // Then reload all notifications
                                            notifications = getUserNotifications(token)
                                            isLoading = false
                                        } catch (e: Exception) {
                                            println("Error marking notification as read: ${e.message}")
                                            DialogState.dialogVisible = true
                                            DialogState.dialogTitle = "Error"
                                            DialogState.dialogDescription =
                                                "Failed to mark notification as read. Please try again."
                                        } finally {
                                            markAsReadRequested = false
                                        }
                                    }
                                }
                                CompactButton(
                                    enabled = !markAsReadRequested,
                                    onClick = {
                                        println("Marking notification as read: ${notification.id}")
                                        markAsReadRequested = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text("Mark as Read", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            ScrollIndicator(
                state = notificationListState,
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}