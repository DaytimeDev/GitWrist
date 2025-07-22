package com.example.watchappgesture.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.ScrollIndicator
import coil.compose.AsyncImage
import com.example.watchappgesture.R
import com.example.watchappgesture.presentation.Github.GitHubRepoEvent
import com.example.watchappgesture.presentation.Github.GitHubRepository
import com.example.watchappgesture.presentation.Github.getRepoEvents
import com.example.watchappgesture.presentation.Github.getRepositories

@Composable
fun Repos(
    context: Context,
    themeColor: Color,
    token: String,
    goBack: () -> Unit,
    onRepoClick: (String) -> Unit
) {
    val userRepos = remember { mutableStateOf<List<GitHubRepository>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (userRepos.value.isNotEmpty()) return@LaunchedEffect // Avoid refetching if already loaded
        try {
            userRepos.value = getRepositories(token)
        } catch (e: Exception) {
            println("Error fetching Repos: ${e.message}")
            DialogState.dialogVisible = true
            DialogState.dialogTitle = "Uh oh..."
            DialogState.dialogDescription =
                "Failed to load your repos. That's not great.\n\nError: ${e.message}"
            DialogState.dialogOnDismiss = {
                DialogState.dialogVisible = false
                goBack()
            }
        }
    }


    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userRepos.value.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.repos),
                        contentDescription = "Loading Repos",
                        tint = themeColor,
                        modifier = Modifier.size(32.dp)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(75.dp),
                        color = androidx.wear.compose.material3.MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                }
            }
        } else {
            item {
                Spacer(modifier = Modifier.size(30.dp))
            }
            item {
                Text(
                    text = "My Repos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // Sort each repo by last updated date
            userRepos.value = userRepos.value.sortedByDescending { it.updatedAt }
            items(userRepos.value) { repo ->
                Chip(
                    onClick = { onRepoClick(repo.fullName) },
                    modifier = Modifier.fillMaxWidth(1f),
                    label = {
                        Text(
                            text = repo.name, color = themeColor, fontWeight = FontWeight.Bold
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = repo.owner.login, color = Color.LightGray, maxLines = 1
                        )
                    },
                    icon = {
                        AsyncImage(
                            model = repo.owner.avatarUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(24.dp)
                        )
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = Color.Black),
                    border = ChipDefaults.outlinedChipBorder(
                        borderColor = Color.DarkGray, borderWidth = 1.dp
                    )
                )
            }
            item {
                Text(
                    text = "Only public repos are being shown since extra " + "permissions are required to access private repos.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
    ScrollIndicator(
        state = rememberScalingLazyListState(),
    )
}


@Composable
fun RepoDetailsScreen(
    repoFullName: String,
    context: Context,
    token: String,
    onBack: () -> Boolean,
    themeColor: androidx.compose.ui.graphics.Color
) {
    val repoDetails = remember { mutableStateOf<GitHubRepository?>(null) }
    val recentEvents = remember { mutableStateOf<List<GitHubRepoEvent>>(emptyList()) }

    LaunchedEffect(repoFullName) {
        try {
            repoDetails.value = getRepositories(token).find { it.fullName == repoFullName }
            if (repoDetails.value == null) {
                throw Exception("Repository not found")
            }

            // Fetch recent events for the repo
            recentEvents.value = getRepoEvents(token, repoFullName)
        } catch (e: Exception) {
            println("Error fetching Repo Details: ${e.message}")
            DialogState.dialogVisible = true
            DialogState.dialogTitle = "Uh oh..."
            DialogState.dialogDescription =
                "Failed to load repo details. That's not great.\n\nError: ${e.message}"
            DialogState.dialogOnDismiss = {
                DialogState.dialogVisible = false
                onBack()
            }
        }
    }

    if (repoDetails.value != null) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.size(30.dp))
            }
            item {
                Text(
                    text = repoDetails.value!!.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            item {
                AsyncImage(
                    model = repoDetails.value!!.owner.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                )
            }
            item {
                Text(
                    text = "Owner: ${repoDetails.value!!.owner.login}",
                    color = Color.White,
                )
            }
            item {
                Text(
                    text = "\"${repoDetails.value!!.description ?: "No description"}\"",
                    color = Color.LightGray,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Stars",
                        tint = Color(0xFFFFAA00),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Stars: ${repoDetails.value!!.stargazersCount}",
                        color = Color(0xFFFFAA00),
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fork),
                        contentDescription = "Forks",
                        tint = Color(0xFF0099FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Forks: ${repoDetails.value!!.forksCount}",
                        color = Color(0xFF0099FF),
                    )
                }
            }

            item {
                // Events title
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            items(recentEvents.value) { event ->
                val hasCommits = !event.payload?.commits.isNullOrEmpty()
                val hasPR = event.payload?.pull_request != null
                val shouldShowDropdown = hasCommits || hasPR

                if (shouldShowDropdown) {
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFF1E1E1E), contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .clickable { expanded = !expanded }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Dropdown Header Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = event.type,
                                    color = themeColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(
                                        id = if (expanded)
                                            R.drawable.ic_arrow_up
                                        else
                                            R.drawable.ic_arrow_down
                                    ),
                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Branch
                            val branchName = event.payload?.ref?.removePrefix("refs/heads/")
                            if (!branchName.isNullOrBlank() && branchName != "unknown") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_branch),
                                        contentDescription = "Branch",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = branchName,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Expanded Content
                            if (expanded) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Commits Section
                                event.payload?.commits?.forEach { commit ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp, horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_commit),
                                                contentDescription = "Commit",
                                                tint = themeColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Commit by ${commit.author.name}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = commit.message,
                                            color = Color.LightGray,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 22.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                // Pull Request Section
                                val pr = event.payload?.pull_request
                                if (pr != null) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_pull_request),
                                                contentDescription = "Pull Request",
                                                tint = themeColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PR #${pr.number}: ${pr.title}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val headRef = pr.head?.ref ?: "?"
                                        val baseRef = pr.base?.ref ?: "?"

                                        Text(
                                            text = "Merged: $headRef → $baseRef",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 22.dp)
                                        )

                                        Text(
                                            text = "State: ${pr.state}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 22.dp)
                                        )

                                        Text(
                                            text = "Created by: ${pr.user.login}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 22.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = event.createdAt.replace("T", " ").replace("Z", ""),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(end = 6.dp)
                            )
                        }
                    }
                } else { // If no dropdown, show basic details
                    Card(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFF1E1E1E), contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .clickable {
                                // Handle click if needed
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = event.type,
                                color = themeColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Event at: ${event.createdAt.replace("T", " ").replace("Z", "")}",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                }
            }

        }
    } else {
        // Show loading or error state
    }


}