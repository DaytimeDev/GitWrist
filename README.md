
# GitWrist ⌚

Meet GitWrist, a project for WearOS which allows devices to interact with the GitHub API to get information such as the users recent notifications and stats.

![GitWrist Preview Image](https://i.ibb.co/MDwq9mWb/Git-Wrist-Icon.png)

---
> This was designed to be a fun project, the code is NOT for production and is a total mess, but feel free to download the app, it's a work in progress

#### About ℹ️
This project is made using [Jetpack Compose](https://developer.android.com/compose) and written in [Kotlin](https://kotlinlang.org/), and relies on the [Github API](https://docs.github.com/en/rest), which also uses a GitHub OAuth app to sign you in.

## App Features 🚀
* Link Git account
* Share your github account with a QR code
* View basic account details such as your username
* View some stats (WIP) like account age, public repos and followers
* View unread notifications and mark them as read from the watch
* Uses `Material 3 Expressive`! The new UI library for Android, check it out here: [MUI3Expressive](https://www.youtube.com/watch?v=n17dnMChX14)

Right now the app does not show private repos since `read-only` access doesn't seem possible, which for security we will not allow the app to have write access

## Installation ⤵️
> This guide will show you how to use the terminal to install to a device -> `Windows`

* Please note this has only been tested on the `Pixel Watch 2` but should be compatible with other WearOS devices.

### 1. Install ADB 
If you do not have ADB (Android Debug Bridge), install it here: [Android Debug Bridge](https://developer.android.com/tools/adb)

Then you should be able to access it in the CLI, if not, try changing to the ADB directory, it can often be found in `C:\Users\example-user\AppData\Local\Android\Sdk\platform-tools` and use `./adb` rather than `adb` (as long as its in the directory).

### 2. Install the latest stable release
Download the APK file from: [GitWear/Releases](https://github.com/DaytimeDev/GitWrist/releases)


### 3. Enable Wireless / USB Debugging
#### *Enable Developer Options*

If you don't have developer options enabled, enable them by clicking the build number on your device about page numerous times until you get a message titled `"You are now a developer"`
(Some watches this may be different and you enable developer options a different way)

Go into the `Developer Options` of your device, turn *on* `ADB Debugging`
if you can plug into your PC, use USB Debugging, otherwise just use Wireless *(Make sure you are on the same Wi-Fi network)*

#### Wireless Debugging
* Enable `Wireless Debugging` in developer options and be on the same network as your pc
* On your watch you should be able to see `IP address and Port`, which looks something like: `---.---.--.--:-----`, you will need that in a second
* Run the command `adb connect (ip address and port)`
* Then check the device is connected by using `adb devices` and it should appear.

#### USB Debugging
* Enable `USB Debugging` in developer options
* Plug in your watch device into your computer, it should now connect via `ADB` as long as USB Debugging is turned *on*
* Then check the device is connected by using `adb devices` and it should appear.

### 4. Install the app
* In the terminal, use the command `./adb install  [your new apk location].apk`


## Bonus: Updating the app ⤴️
* If there is a newer version of the APK, complete steps 1, 2 and 3, then run the command `./adb install -r [your new apk location].apk`, this will update it but keep your data, if you want to use a custom flag on the install check out [ADB Install Commands](https://adbshell.com/commands/adb-install)
