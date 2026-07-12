import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_top_bar = """            topBar = {
                TopAppBar(
                    title = {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDropdown = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = activePlan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDropDown,
                                    contentDescription = "Select Plan",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sparkex Plus") },
                                    onClick = {
                                        activePlan = "Sparkex Plus"
                                        showDropdown = false
                                        showPlanModal = "Sparkex Plus Tier - ₹199/month"
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sparkex Pro") },
                                    onClick = {
                                        activePlan = "Sparkex Pro"
                                        showDropdown = false
                                        showPlanModal = "Sparkex Pro Tier - ₹399/month"
                                    }
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Sidebar Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }"""

new_top_bar = """            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Menu Icon (Leftmost position)
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))

                    // 2. Sparkex Model Selector Dropdown (Gemini Pro Position)
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePlan, // "Sparkex Plus" or "Sparkex Pro"
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = "Select Plan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sparkex Plus") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Plus"
                                    showPlanModal = "Sparkex Plus Tier - ₹199/month"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sparkex Pro") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Pro"
                                    showPlanModal = "Sparkex Pro Tier - ₹399/month"
                                }
                            )
                        }
                    }
                }
            }"""

if old_top_bar in text:
    text = text.replace(old_top_bar, new_top_bar)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched top bar successfully")
else:
    print("Could not find top bar")
