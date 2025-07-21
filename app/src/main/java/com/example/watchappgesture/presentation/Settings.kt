package com.example.watchappgesture.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


suspend fun saveUserThemeColor(context: Context, colorHex: String) {
    val THEME_COLOR_KEY = stringPreferencesKey("theme_color")
    withContext(Dispatchers.IO) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = colorHex
        }


        // Restart the app to apply the new theme color
        // This is a workaround since we can't change the theme dynamically in Wear OS

        restartApp(context)
    }
}


@Composable
fun SettingsScreen(context: Context) {
    val scope = rememberCoroutineScope()
    var selectedColor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        selectedColor = getUserThemeColor(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val listState = rememberScalingLazyListState()

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            userScrollEnabled = true,
            anchorType = ScalingLazyListAnchorType.ItemStart
        ) {
            item {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            val colors = listOf(
                "#00FA9A", "#87dbff", "#ff5036",
                "#5736ff", "#ff3636", "#e136ff",
            )

            colors.chunked(3).forEach { rowColors ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        rowColors.forEach { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(color.toColorInt()).copy(alpha = if (isSelected) 1f else 0.5f))
                                    .clickable {
                                        selectedColor = color
                                        scope.launch {
                                            saveUserThemeColor(context, color)
                                        }
                                    }
                            )
                        }
                        repeat(3 - rowColors.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "More settings will be added soon.",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}