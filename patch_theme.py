import re

with open("app/src/main/java/com/example/ui/theme/Theme.kt", "r") as f:
    text = f.read()

imports = """import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
"""

if "import androidx.compose.runtime.SideEffect" not in text:
    text = text.replace("import androidx.compose.runtime.Composable", imports + "import androidx.compose.runtime.Composable")

side_effect = """
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
"""

if "val view = LocalView.current" not in text:
    text = text.replace("val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme", "val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme\n" + side_effect)

with open("app/src/main/java/com/example/ui/theme/Theme.kt", "w") as f:
    f.write(text)
