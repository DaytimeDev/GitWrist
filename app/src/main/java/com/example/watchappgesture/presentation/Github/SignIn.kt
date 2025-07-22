package com.example.watchappgesture.presentation.Github

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.watchappgesture.presentation.DialogState
import com.example.watchappgesture.presentation.checkInternet
import com.example.watchappgesture.presentation.saveUserAccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


// Allow the user to sign in with github by opening a link on their phone
class SignIn(private val client: OkHttpClient = OkHttpClient()) {

    val githubClientID = "Ov23liVxTJf2IFPnmfnb"


    // Define what scopes the watch app needs

    val scopes = "read:user,notifications,repo"


    suspend fun requestDeviceCode(): Pair<String?, String?> {
        val getCodeURL =
            "https://github.com/login/device/code?client_id=$githubClientID&scope=$scopes"
        val postBody = ""

        val request = Request.Builder().url(getCodeURL).post(postBody.toRequestBody(null)).build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Unexpected code $response")
                val bodyString = response.body?.string()
                val userCode = bodyString?.substringAfter("user_code=")?.substringBefore("&")
                val deviceCode = bodyString?.substringAfter("device_code=")?.substringBefore("&")
                Pair(userCode, deviceCode)
            }
        }
    }

    fun pollGithub(deviceCode: String): String? {
        val grantType = "urn:ietf:params:oauth:grant-type:device_code"

        val pollUrl =
            "https://github.com/login/oauth/access_token?client_id=$githubClientID&device_code=$deviceCode&grant_type=$grantType"
        val request = Request.Builder()
            .url(pollUrl)
            .post("".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull()))
            .build()
        val verificationResponse = client.newCall(request).execute()
        if (!verificationResponse.isSuccessful) {
            println("${verificationResponse.code}: Response message: ${verificationResponse.message}")
            return null
        }
        val verificationResponseBody = verificationResponse.body?.string()


        // Check if there is an error in the response; e.g. Response from GitHub: error=authorization_pending&error_description=The+authorization+request+is+still+pending.&error_uri=https%3A%2F%2Fdocs.github.com%2Fdevelopers%2Fapps%2Fauthorizing-oauth-apps%23error-codes-for-the-device-flow
        if (verificationResponseBody?.contains("error") == true) {
            println("Error in response: $verificationResponseBody")
            // Only allow authorization_pending error, which means the user has not yet authorized the app
            if (verificationResponseBody.contains("authorization_pending")) {
                println("Authorization pending, waiting for user to authorize the app")
            } else {
                error("Error in response: $verificationResponseBody")
                return null
            }
            return null
        } else {
            if (verificationResponseBody?.contains("access_token") == true) {
                val accessToken =
                    verificationResponseBody.substringAfter("access_token=").substringBefore("&")
                return accessToken
            } else {
                println("No access token found in response: $verificationResponseBody")
                return null
            }
        }

    }


}

@Composable
fun UserCodePreview(userCode: String, context: Context) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .wrapContentSize(Alignment.Center)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = userCode,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "github.com/login/device",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}


@Composable
fun SetUp(context: Context, onSignedIn: (String) -> Unit, themeColor: Color) {
    var userCode by remember { mutableStateOf("") }
    var deviceCode by remember { mutableStateOf("") }
    var isLoadingCode by remember { mutableStateOf(false) }

    val signIn = SignIn()

    suspend fun returnCodes(): Pair<String?, String?> {
        val (userCode, deviceCode) = signIn.requestDeviceCode()
        return Pair(userCode, deviceCode)
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        if (userCode.isNotEmpty()) {
            UserCodePreview(userCode, context)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            }
            Button(
                enabled = !isLoadingCode,
                // Use the theme color
                modifier = Modifier
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1f1f1f),
                ),
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val hasInternet = checkInternet(context)

                        if (!hasInternet) {
                            DialogState.dialogVisible = true
                            DialogState.dialogTitle = "Unable to Connect"
                            DialogState.dialogDescription =
                                "Please connect to the internet and try again."
                            return@launch
                        }

                        isLoadingCode = true
                        val (newUserCode, newDeviceCode) = returnCodes()
                        println("Got user code back to the button: $userCode")
                        userCode = newUserCode ?: "Failed to get user code"
                        deviceCode = newDeviceCode ?: "Failed to get device code"

                        println("User code --: $userCode")
                        println("Device code --: $deviceCode")

                        isLoadingCode = false

                        // Keep polling github to see if the user has authorized the app
                        // Start an interval loop
                        while (true) {
                            val accessToken = signIn.pollGithub(deviceCode)
                            if (accessToken != null) {
                                println("Got the access token! $accessToken")
                                saveUserAccessToken(context, accessToken)
                                withContext(Dispatchers.Main) {
                                    onSignedIn(accessToken)
                                }


                                // STORE THE ACCESS TOKEN
                                // DO NOT USE MORE THAN ONE DATASTORE INSTANCE AT ONCE
                                break
                            }
                            delay(5000) // wait 5 seconds
                        }
                    }
                }) {
                Text(
                    text = "Sign in with GitHub",
                    color = Color.White
                )
            }
        }
    }
}
