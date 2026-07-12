import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    text = f.read()

# Make sure imports are present
imports = """import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
"""
if "import android.Manifest" not in text:
    text = text.replace("import androidx.compose.runtime.remember", imports + "import androidx.compose.runtime.remember")

launcher_code = """    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateNotificationsEnabled(true)
            com.example.util.NotificationHelper.sendTestNotification(context)
        } else {
            viewModel.updateNotificationsEnabled(false)
        }
    }
"""

if "val permissionLauncher" not in text:
    text = text.replace("val snackbarHostState = remember { SnackbarHostState() }", "val snackbarHostState = remember { SnackbarHostState() }\n" + launcher_code)

# Replace the Switch onCheckedChange and the row clickable
old_clickable = ".clickable { viewModel.updateNotificationsEnabled(!profile.notificationsEnabled) }"
new_clickable = """.clickable {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !profile.notificationsEnabled) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val newState = !profile.notificationsEnabled
                                        viewModel.updateNotificationsEnabled(newState)
                                        if (newState) {
                                            com.example.util.NotificationHelper.sendTestNotification(context)
                                        }
                                    }
                                }"""
text = text.replace(old_clickable, new_clickable)

old_switch = "onCheckedChange = { viewModel.updateNotificationsEnabled(it) }"
new_switch = """onCheckedChange = { 
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && it) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.updateNotificationsEnabled(it)
                                        if (it) {
                                            com.example.util.NotificationHelper.sendTestNotification(context)
                                        }
                                    }
                                }"""
text = text.replace(old_switch, new_switch)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(text)

