/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchappgesture.presentation

//noinspection SuspiciousImport
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.background
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.foundation.sizeIn
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.curvedText
import com.example.watchappgesture.presentation.Github.GitHubUser
import com.example.watchappgesture.presentation.Github.SetUp
import com.example.watchappgesture.presentation.Github.getUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

val Context.dataStore by preferencesDataStore(name = "settings")

suspend fun saveUserAccessToken(context: Context, token: String) {
    val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    withContext(Dispatchers.IO) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }
}

suspend fun getUserAccessToken(context: Context): String? {
    val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    return context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[ACCESS_TOKEN_KEY] }
        .first()
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun checkInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: Network? = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

suspend fun getUserThemeColor(context: Context): String {
    val THEME_COLOR_KEY = stringPreferencesKey("theme_color")
    return context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[THEME_COLOR_KEY] ?: "#00FA9A" }
        .first()
}

@Composable
fun App() {
    val context = LocalContext.current
    var accessToken by remember { mutableStateOf<String?>(null) }
    var themeColor by remember { mutableStateOf(Color(0xFF00FA9A)) }

    // Load theme and token on startup
    LaunchedEffect(Unit) {
        accessToken = getUserAccessToken(context)
        val hex = getUserThemeColor(context)
        themeColor = Color(android.graphics.Color.parseColor(hex))
    }

    // Dialog shown globally
    MessageDialog(
        visible = DialogState.dialogVisible,
        onDismiss = {
            DialogState.dialogOnDismiss()
            DialogState.dialogVisible = false
        },
        title = DialogState.dialogTitle,
        description = DialogState.dialogDescription,
        themeColor = themeColor,
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = themeColor,
            onPrimary = Color.Black,
            onSurface = Color.White,
        ),
    ) {
        when {
            accessToken.isNullOrEmpty() -> {
                SetUp(context = context, onSignedIn = { token ->
                    accessToken = token
                }, themeColor = themeColor)
            }

            else -> {
                MainScreen(accessToken!!, context, themeColor)
            }
        }

        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName

        CurvedLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp),
            anchor = 90f, // Anchor at the bottom
            anchorType = AnchorType.Center,
        ) {
            curvedRow(
                modifier =
                    CurvedModifier
                        .background(color = Color(0xFFFFA407), StrokeCap.Round),
            ) {
                curvedText(
                    text = "Preview: V$versionName",
                    color = Color.DarkGray,
                    modifier =
                        CurvedModifier
                            .sizeIn(0f, 80f),
                    fontSize = 8.sp,
                    // Flip the text to be readable on a round watch face
                    angularDirection = CurvedDirection.Angular.CounterClockwise
                )
            }
        }
    }
}

fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}

@Composable
fun MainScreen(token: String, context: Context, themeColor: Color) {
    var userInfo by remember { mutableStateOf<GitHubUser?>(null) }

    LaunchedEffect(token) {
        if (!checkInternet(context)) {
            withContext(Dispatchers.Main) {
                DialogState.dialogVisible = true
                DialogState.dialogTitle = "Cannot load account"
                DialogState.dialogDescription =
                    "Please connect to the internet and try again."
            }
            return@LaunchedEffect
        }
        try {
            val user = getUserInfo(token)
            withContext(Dispatchers.Main) {
                userInfo = user
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                DialogState.dialogVisible = true
                DialogState.dialogTitle = "Uh oh..."

                var errorMessage = e.localizedMessage ?: "An unknown error occurred."
                errorMessage =
                    "We might have to sign you out...\n\nIf this persists, please contact us on GitHub.\n\n$errorMessage"

                saveUserAccessToken(context, "")

                DialogState.dialogDescription = errorMessage
                DialogState.dialogOnDismiss = {
                    DialogState.dialogVisible = false
                    restartApp(context)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (userInfo == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                )
            }
        } else {
            HomeScreen(
                userInfo,
                token,
                context,
                themeColor
            )
        }
    }
}