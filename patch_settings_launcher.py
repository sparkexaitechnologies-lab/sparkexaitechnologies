import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    text = f.read()

launcher_code = """    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateNotificationsEnabled(true)
            com.example.util.NotificationHelper.sendTestNotification(context)
        } else {
            viewModel.updateNotificationsEnabled(false)
        }
    }
"""

if "val permissionLauncher =" not in text:
    text = text.replace("val context = LocalContext.current", "val context = LocalContext.current\n" + launcher_code)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(text)

