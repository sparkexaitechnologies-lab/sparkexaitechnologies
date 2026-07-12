import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Add states inside ChatScreen function
state_insertion_point = "    val coroutineScope = rememberCoroutineScope()"
state_insertion = """    val coroutineScope = rememberCoroutineScope()
    var activePlan by remember { mutableStateOf("Sparkex Plus") }
    var showDropdown by remember { mutableStateOf(false) }
    var showPlanModal by remember { mutableStateOf<String?>(null) }"""
if state_insertion_point in text:
    text = text.replace(state_insertion_point, state_insertion)
else:
    print("Could not find state insertion point")

# Replace TopAppBar
old_top_bar = """            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Sparkex AI",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }"""

new_top_bar = """            topBar = {
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

if old_top_bar in text:
    text = text.replace(old_top_bar, new_top_bar)
else:
    print("Could not find Top Bar")

# Add Modal Overlay at the end of ChatScreen box
modal_insertion_point = "                // Interactive Live Voice Conversation overlay modal"
modal_insertion = """                // Plan Modal Overlay
                if (showPlanModal != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showPlanModal = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable(enabled = false) {}
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Subscription Plan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = showPlanModal ?: "",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { showPlanModal = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Got it")
                                }
                            }
                        }
                    }
                }

                // Interactive Live Voice Conversation overlay modal"""

if modal_insertion_point in text:
    text = text.replace(modal_insertion_point, modal_insertion)
else:
    print("Could not find Modal insertion point")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
