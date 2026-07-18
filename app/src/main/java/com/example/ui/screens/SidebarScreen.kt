package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatSession
import com.example.ui.viewmodel.SparkexViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarScreen(
    viewModel: SparkexViewModel,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState(initial = emptyList())
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val groupedSessions = remember(sessions) {
        val now = System.currentTimeMillis()
        sessions.groupBy { session ->
            when {
                DateUtils.isToday(session.timestamp) -> "Today"
                DateUtils.isToday(session.timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
                session.timestamp > now - 7 * DateUtils.DAY_IN_MILLIS -> "Last 7 Days"
                else -> "Older Conversations"
            }
        }
    }
    val groupOrder = listOf("Today", "Yesterday", "Last 7 Days", "Older Conversations")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            var isSearching by remember { mutableStateOf(false) }
            val searchQuery by viewModel.searchQuery.collectAsState()

            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search conversations...", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                        )
                    } else {
                        Text("Sparkex AI", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearching) {
                                isSearching = false
                                viewModel.setSearchQuery("")
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearching) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Conversations", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // New Chat Button
            Button(
                onClick = { 
                    viewModel.startNewSession()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("New Chat", fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Conversations",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupOrder.forEach { group ->
                    val groupSessions = groupedSessions[group]
                    if (!groupSessions.isNullOrEmpty()) {
                        item {
                            Text(
                                text = group,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                        items(groupSessions) { session ->
                            val isSelected = session.id == activeSessionId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                    .clickable { 
                                        viewModel.selectSession(session.id)
                                        onBack()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // User Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToProfile() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        Text(
                            text = profile.name.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = profile.name.ifBlank { "User Profile" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
