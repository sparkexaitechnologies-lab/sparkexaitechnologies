package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.SparkexViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubSettingScreen(
    viewModel: SparkexViewModel,
    subRoute: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    
    val title = remember(subRoute) {
        when (subRoute) {
            "edit" -> "Edit Profile"
            "theme" -> "Theme Settings"
            "language" -> "Language"
            "notifications" -> "Notifications"
            "privacy" -> "Privacy Policy"
            "about" -> "About"
            "signout" -> "Sign Out"
            else -> "Settings"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            when (subRoute) {
                "edit" -> EditProfileContent(viewModel, onBack)
                "theme" -> ThemeSettingsContent(viewModel)
                "language" -> LanguageSettingsContent(viewModel)
                "notifications" -> NotificationSettingsContent(viewModel)
                "privacy" -> PrivacyContent()
                "about" -> AboutContent()
                "signout" -> SignOutContent(onBack)
                else -> Text("Section not found", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun EditProfileContent(viewModel: SparkexViewModel, onBack: () -> Unit) {
    val profile by viewModel.userProfile.collectAsState()
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var avatarPrompt by remember { mutableStateOf("") }
    val isGenerating by viewModel.isGeneratingImage.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.avatarPath != null) {
                        AsyncImage(
                            model = File(profile.avatarPath!!),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(60.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("AI Avatar Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Describe your ideal portrait", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            OutlinedTextField(
                value = avatarPrompt,
                onValueChange = { avatarPrompt = it },
                placeholder = { Text("Cyberpunk style, oil painting...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { 
                            if (avatarPrompt.isNotBlank()) {
                                viewModel.generateProfileAvatar(avatarPrompt)
                                avatarPrompt = ""
                            }
                        }) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = "Generate")
                        }
                    }
                }
            )
        }

        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = { 
                    viewModel.updateProfileNameEmail(name, email)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ThemeSettingsContent(viewModel: SparkexViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val modes = listOf("System", "Light", "Dark")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
        modes.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.updateThemeMode(mode) }
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mode, style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = profile.themeMode == mode,
                    onClick = { viewModel.updateThemeMode(mode) }
                )
            }
        }
    }
}

@Composable
fun LanguageSettingsContent(viewModel: SparkexViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val languages = listOf("English", "Telugu", "Hindi", "Spanish", "French")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
        languages.forEach { lang ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.updateSelectedLanguage(lang) }
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(lang, style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = profile.selectedLanguage == lang,
                    onClick = { viewModel.updateSelectedLanguage(lang) }
                )
            }
        }
    }
}

@Composable
fun NotificationSettingsContent(viewModel: SparkexViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Push Notifications", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Receive alerts and updates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = profile.notificationsEnabled,
                onCheckedChange = { 
                    viewModel.updateNotificationsEnabled(it)
                    if (it) {
                        com.example.util.NotificationHelper.sendTestNotification(context)
                    }
                }
            )
        }
    }
}

@Composable
fun PrivacyContent() {
    LazyColumn(contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Your Data Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            Text(
                "Sparkex AI is designed with privacy as a core principle. Your conversations are stored locally on your device and are only processed by secure AI models to provide responses. We do not sell your personal data or use your chats to train public models without your explicit consent.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
        item {
            Text("Key Points:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("- Local storage for chat history\n- Encrypted API communications\n- Transparent data handling\n- User-controlled account data", modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun AboutContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Sparkex AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Version 1.5.0 (Premium Build)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Sparkex AI is a professional AI assistant developed for high-performance productivity and clean interaction spaces.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SignOutContent(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sign Out?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Are you sure you want to sign out?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { 
                    Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Sign Out")
            }
        }
    }
}
