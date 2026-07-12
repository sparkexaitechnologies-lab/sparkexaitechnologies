import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# 1. Update the activePlan to default to Sparkex Free and add shared preferences logic
state_insert = """    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var activePlan by remember { mutableStateOf("Sparkex Plus") }
    var showDropdown by remember { mutableStateOf(false) }"""

new_state_insert = """    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    val prefs = context.getSharedPreferences("sparkex_prefs", android.content.Context.MODE_PRIVATE)
    var activePlan by remember { mutableStateOf(prefs.getString("active_plan", "Sparkex Free") ?: "Sparkex Free") }
    var freeUses by remember { mutableStateOf(prefs.getInt("free_uses", 0)) }
    var showLimitModal by remember { mutableStateOf(false) }

    var showDropdown by remember { mutableStateOf(false) }"""

if state_insert in text:
    text = text.replace(state_insert, new_state_insert)
    print("State replaced")
else:
    print("State insert not found")

# 2. Update the dropdown items to save the selected plan
dropdown_item_old = """                            DropdownMenuItem(
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
                            )"""

dropdown_item_new = """                            DropdownMenuItem(
                                text = { Text("Sparkex Free") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Free"
                                    prefs.edit().putString("active_plan", "Sparkex Free").apply()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sparkex Plus") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Plus"
                                    prefs.edit().putString("active_plan", "Sparkex Plus").apply()
                                    showPlanModal = "Sparkex Plus Tier - ₹199/month"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sparkex Pro") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Pro"
                                    prefs.edit().putString("active_plan", "Sparkex Pro").apply()
                                    showPlanModal = "Sparkex Pro Tier - ₹399/month"
                                }
                            )"""

if dropdown_item_old in text:
    text = text.replace(dropdown_item_old, dropdown_item_new)
    print("Dropdown replaced")
else:
    print("Dropdown not found")

# 3. Update the send button logic to increment limits
send_btn_old = """                            // Send Button
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) {
                                        viewModel.sendTextMessage(
                                            text = textInput,
                                            attachedImagePath = attachedImagePath,
                                            attachedVoicePath = attachedVoicePath
                                        )
                                        textInput = ""
                                        attachedImagePath = null
                                        attachedVoicePath = null
                                    }
                                },"""

send_btn_new = """                            // Send Button
                            IconButton(
                                onClick = {
                                    if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                        showLimitModal = true
                                    } else {
                                        if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) {
                                            if (activePlan == "Sparkex Free") {
                                                val newCount = freeUses + 1
                                                freeUses = newCount
                                                prefs.edit().putInt("free_uses", newCount).apply()
                                            }
                                            viewModel.sendTextMessage(
                                                text = textInput,
                                                attachedImagePath = attachedImagePath,
                                                attachedVoicePath = attachedVoicePath
                                            )
                                            textInput = ""
                                            attachedImagePath = null
                                            attachedVoicePath = null
                                        }
                                    }
                                },"""

if send_btn_old in text:
    text = text.replace(send_btn_old, send_btn_new)
    print("Send button replaced")
else:
    print("Send button not found")


with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
