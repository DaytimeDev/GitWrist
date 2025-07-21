package com.example.watchappgesture.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import coil.compose.AsyncImage
import com.example.watchappgesture.presentation.Github.GitHubUser
import com.example.watchappgesture.presentation.Github.QRPreview
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    userInfo: GitHubUser?,
    token: String,
    context: Context,
    themeColor: Color = MaterialTheme.colorScheme.primary
) {


    AppScaffold {
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "user_screen"
        )
        {
            composable("user_screen")
            {


                val listState = rememberScalingLazyListState()

                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = listState,
                    userScrollEnabled = true,
                    anchorType = ScalingLazyListAnchorType.ItemStart
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
                            onClick = { navController.navigate("qr_screen") },
                            label = { Text(text = "Share Account QR") },
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                        )
                    }
                    item {
                        CompactButton(
                            onClick = { navController.navigate("notification_screen") },
                            label = { Text("Notifications") },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    item {
                        OutlinedButton(
                            onClick = { navController.navigate("repos_screen") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = com.example.watchappgesture.R.drawable.repos),
                                    contentDescription = "Repos Icon",
                                    modifier = Modifier.size(25.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.size(10.dp))
                                Text(
                                    text = "My Repos",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
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
                    item {
                        EdgeButton(
                            onClick = { navController.navigate("settings_screen") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                        )
                        {
                            Text(
                                text = "Settings",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                ScrollIndicator(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 4.dp)
                )
            }


            composable("notification_screen")
            {
                NotificationScreen(
                    token = token,
                )
            }

            composable("qr_screen")
            {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    QRPreview(
                        url = userInfo?.html_url,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    )
                }
            }

            composable("settings_screen")
            {
                SettingsScreen(context)
            }

            composable("repos_screen")
            {
                Repos(
                    context, themeColor, token, { navController.navigateUp() },
                    onRepoClick = { repoFullName ->
                        val encoded =
                            URLEncoder.encode(repoFullName, StandardCharsets.UTF_8.toString())
                        navController.navigate("repo_details/$encoded")
                        // Encode so the / in the repo name doesn't break the composable navigation
                    }
                )
            }

            composable("repo_details/{repoFullName}") { backStackEntry ->
                val encodedRepoFullName = backStackEntry.arguments?.getString("repoFullName")
                val repoFullName = java.net.URLDecoder.decode(encodedRepoFullName ?: "", "UTF-8")
                RepoDetailsScreen(
                    repoFullName = repoFullName,
                    context = context,
                    token = token,
                    onBack = { navController.navigateUp() } // Navigate back to the repos screen
                )
            }
        }
    }
}