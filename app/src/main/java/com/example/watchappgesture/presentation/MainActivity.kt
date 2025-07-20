/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchappgesture.presentation

//noinspection SuspiciousImport
import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import coil.compose.AsyncImage
import com.example.watchappgesture.presentation.Github.GitHubNotification
import com.example.watchappgesture.presentation.Github.GitHubUser
import com.example.watchappgesture.presentation.Github.SetUp
import com.example.watchappgesture.presentation.Github.getUserInfo
import com.example.watchappgesture.presentation.Github.getUserNotifications
import com.example.watchappgesture.presentation.Github.markNotificationAsRead
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)


        setContent {
            WearApp()
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
        .map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
        .first() // Collect the first (and only) value
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

object DialogState {
    var dialogVisible by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogDescription by mutableStateOf("")
    var dialogDismissButton by mutableStateOf({})
    var dialogOnDismiss by mutableStateOf({ dialogVisible = false })
}


val primaryColorHex = 0xFF00FA9A


@SuppressLint("MissingPermission")
@Preview(
    device = "id:wearos_small_round",
    name = "Pixel Watch",
    showSystemUi = true,
    showBackground = true,
    backgroundColor = 0xFF4CAF50
)
@Composable
fun WearApp() {
    val context = LocalContext.current
    var accessToken by remember { mutableStateOf<String?>(null) }


    // Load access token on startup
    LaunchedEffect(Unit) {
        accessToken = getUserAccessToken(context)
    }

    // The dialog which can be called at any time for different things
    SingleButtonDialog(
        visible = DialogState.dialogVisible,
        onDismiss = {
            DialogState.dialogOnDismiss()
            DialogState.dialogVisible = false
        },
        title = DialogState.dialogTitle,
        description = DialogState.dialogDescription
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(primaryColorHex),
            onPrimary = Color.Black,
            onSurface = Color.White,
        ),
    ) {
        when {
            accessToken.isNullOrEmpty() -> {
                SetUp(context = context, onSignedIn = { token ->
                    accessToken = token
                })
            }

            else -> {
                MainScreen(accessToken!!, context)
            }
        }

        TimeText()
    }
}


@Composable
fun SingleButtonDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String = "Title",
    description: String = "No description provided.",
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = { },
        dismissButton = {
            AlertDialogDefaults.DismissButton(
                colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(
                    contentColor = Color.Black,
                    containerColor = Color(0xFF00FA9A)
                ),
                onClick = onDismiss,
                content = {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Dismiss",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    )
}

fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)

    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}


@Composable
fun MainScreen(token: String, context: Context) {
    var userInfo by remember { mutableStateOf<GitHubUser?>(null) }
    var shareAccountQR by remember { mutableStateOf(false) }

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
            // If the token is an empty string, we should not attempt to fetch user info
            val user = getUserInfo(token)
            println("Processing token: $token")
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

                // Sign out the user
                saveUserAccessToken(context, "") // Clear the access token


                DialogState.dialogDescription = errorMessage
                DialogState.dialogOnDismiss = {
                    println("User is dismissing the dialog, restarting app...")
                    DialogState.dialogVisible = false
                    restartApp(context)
                }
            }
            return@LaunchedEffect
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
            UserProfileScreen(
                userInfo = userInfo,
                shareAccountQR = shareAccountQR,
                onToggleQR = {
                    shareAccountQR = !shareAccountQR
                },
                token = token
            )
        }
    }
}


@Composable
fun UserProfileScreen(
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
            userScrollEnabled = !showNotifications, // Stop the notifications from preventing scrolling when closed
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
            QRPreview(
                url = userInfo?.html_url,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onToggleQR()
                    }
            )
        }
        if (showNotifications) {
            NotificationScreen(onDismiss = { showNotifications = false }, token)
        }
    }
}

fun generateQrCode(content: String?, size: Int = 512): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val bmp = createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp[x, y] =
                if (bitMatrix[x, y]) primaryColorHex.toInt() else android.graphics.Color.BLACK
        }
    }
    return bmp
}


@Composable
fun QRPreview(url: String?, modifier: Modifier) {
    val qrCodeBitmap by produceState<Bitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.Default) { generateQrCode(url, 512) }
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

@Composable
fun NotificationScreen(onDismiss: () -> Unit, token: String) {

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
            DialogState.dialogDescription = "Failed to load notifications. That's not great.\n\nError: ${e.message}"
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
                                text = notification.subject.title,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notification.subject.type,
                                color = Color.Gray,
                                fontSize = 10.sp
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
                            // if show buttons
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
                                    enabled = markAsReadRequested,
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

                item {
                    Button(
                        onClick = onDismiss,
                        label = { Text("Close") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
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