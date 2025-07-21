package com.example.watchappgesture.presentation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImage
import com.example.watchappgesture.presentation.Github.GitHubRepository
import com.example.watchappgesture.presentation.Github.getRepositories

@Composable
fun Repos(
    context: Context, themeColor: Color, token: String, goBack: () -> Unit,
    onRepoClick: (String) -> Unit
) {
    val userRepos = remember { mutableStateOf<List<GitHubRepository>>(emptyList()) }

    LaunchedEffect(Unit) {
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        if (userRepos.value.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                )
                {
                    Icon(
                        painter = painterResource(id = com.example.watchappgesture.R.drawable.repos),
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
            // Sort each repo by last updated date
            userRepos.value = userRepos.value.sortedByDescending { it.updatedAt }
            items(userRepos.value) { repo ->
                Chip(
                    onClick = { onRepoClick(repo.fullName) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = repo.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = repo.owner.login,
                            color = Color.LightGray,
                            maxLines = 1
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
                        borderColor = themeColor,
                        borderWidth = 1.dp
                    )
                )
            }
        }
    }
}


@Composable
fun RepoDetailsScreen(
    repoFullName: String,
    context: Context,
    token: String,
    onBack: () -> Boolean
) {

    val repoDetails = remember { mutableStateOf<GitHubRepository?>(null) }

    LaunchedEffect(repoFullName) {
        try {
            repoDetails.value = getRepositories(token).find { it.fullName == repoFullName }
            if (repoDetails.value == null) {
                throw Exception("Repository not found")
            }
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.example.watchappgesture.R.drawable.ic_star),
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
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.example.watchappgesture.R.drawable.ic_fork),
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
        }
    } else {
        // Show loading or error state
    }


}