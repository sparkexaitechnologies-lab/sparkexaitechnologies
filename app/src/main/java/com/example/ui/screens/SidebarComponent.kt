package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatSession
import com.example.data.local.UserProfile
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarComponent(
    sessions: List<ChatSession>,
    activeSessionId: String?,
    profile: UserProfile,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToVideoCreator: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToMemberships: () -> Unit,
    modifier: Modifier = Modifier,
    onCloseSidebar: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter sessions based on search query
    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Group filtered sessions chronologically
    val groupedSessions = remember(filteredSessions) {
        val now = System.currentTimeMillis()
        filteredSessions.groupBy { session ->
            when {
                DateUtils.isToday(session.timestamp) -> "Today"
                DateUtils.isToday(session.timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
                session.timestamp > now - 7 * DateUtils.DAY_IN_MILLIS -> "Last 7 Days"
                else -> "Older Conversations"
            }
        }
    }

    // Ordering groups logically
    val groupOrder = listOf("Today", "Yesterday", "Last 7 Days", "Older Conversations")

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp) // Flat design
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header / Profile Shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onNavigateToSettings()
                        onCloseSidebar()
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAvatar(
                    avatarPath = profile.avatarPath,
                    name = profile.name.ifBlank { "Sparkex User" }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name.ifBlank { "Sparkex User" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "View Profile & Settings",
                        fontSize = 11.sp,
                        color = Color(0xFF6E6E73),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 1.dp)

            // Redesigned Pill-shaped New Chat Button with subtle background and border
            OutlinedButton(
                onClick = {
                    onNewChat()
                    onCloseSidebar()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF7F7F8), // Subtle background
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color(0xFFE5E5EA)), // Subtle border
                shape = RoundedCornerShape(50.dp), // Prominent pill/capsule style
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New Chat Icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Search Past Sessions Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", fontSize = 13.sp, color = Color(0xFF6E6E73)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search Icon",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF6E6E73)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear Search",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF6E6E73)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color(0xFFE5E5EA),
                    focusedContainerColor = Color(0xFFF7F7F8),
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

            // Chat History Section Header
            Text(
                text = "Chat History",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF6E6E73),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Dynamic Scrollable Session List grouped chronologically
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredSessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "No past conversations yet" else "No matches found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6E6E73),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    groupOrder.forEach { groupName ->
                        val groupList = groupedSessions[groupName]
                        if (!groupList.isNullOrEmpty()) {
                            item {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF6E6E73),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            items(groupList, key = { it.id }) { session ->
                                val isSelected = session.id == activeSessionId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) Color(0xFFF7F7F8)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onSelectSession(session.id)
                                            onCloseSidebar()
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFE5E5EA) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isSelected) Color.Black else Color(0xFF6E6E73)
                                        )
                                        Text(
                                            text = session.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (isSelected) Color.Black else Color(0xFF6E6E73)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteSession(session.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete Session",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF6E6E73)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 1.dp)

            // Sidebar Footer Navigation Shortcuts with safe system navigation bar padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SidebarShortcutItem(
                    icon = Icons.Outlined.CardMembership,
                    title = "Membership",
                    onClick = {
                        onNavigateToMemberships()
                        onCloseSidebar()
                    }
                )
                SidebarShortcutItem(
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    onClick = {
                        onNavigateToSettings()
                        onCloseSidebar()
                    }
                )
                SidebarShortcutItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & Guides",
                    onClick = {
                        onNavigateToHelp()
                        onCloseSidebar()
                    }
                )
            }
        }
    }
}

@Composable
fun CircleAvatar(
    avatarPath: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFF7F7F8))
            .border(1.dp, Color(0xFFE5E5EA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarPath != null) {
            AsyncImage(
                model = File(avatarPath),
                contentDescription = "Profile Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = if (name.isNotBlank()) name.take(1).uppercase() else "S"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SidebarShortcutItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6E6E73),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

