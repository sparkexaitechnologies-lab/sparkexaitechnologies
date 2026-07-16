package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.SparkexViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SparkexViewModel,
    onNavigateToSubSetting: (String) -> Unit,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
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
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile.name.ifBlank { "User" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.email.ifBlank { "No email provided" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Profile Options
            item {
                ProfileOption(
                    icon = Icons.Outlined.Person,
                    title = "Profile",
                    subtitle = "Manage name, email, and avatar",
                    onClick = { onNavigateToSubSetting("edit") }
                )
            }
            item {
                ProfileOption(
                    icon = Icons.Outlined.Palette,
                    title = "Theme",
                    subtitle = "Light, Dark, or System Default",
                    onClick = { onNavigateToSubSetting("theme") }
                )
            }
            item {
                ProfileOption(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    subtitle = profile.selectedLanguage,
                    onClick = { onNavigateToSubSetting("language") }
                )
            }
            item {
                ProfileOption(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = if (profile.notificationsEnabled) "Enabled" else "Disabled",
                    onClick = { onNavigateToSubSetting("notifications") }
                )
            }
            item {
                ProfileOption(
                    icon = Icons.Outlined.Security,
                    title = "Privacy",
                    subtitle = "Data and security settings",
                    onClick = { onNavigateToSubSetting("privacy") }
                )
            }
            item {
                ProfileOption(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    subtitle = "App version and info",
                    onClick = { onNavigateToSubSetting("about") }
                )
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            item {
                ProfileOption(
                    icon = Icons.Outlined.Logout,
                    title = "Sign Out",
                    subtitle = "Log out from your account",
                    color = MaterialTheme.colorScheme.error,
                    onClick = { onNavigateToSubSetting("signout") }
                )
            }
        }
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
