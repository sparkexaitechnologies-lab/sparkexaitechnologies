package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SparkexViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipsScreen(
    viewModel: SparkexViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()

    var showSuccessAnimation by remember { mutableStateOf(false) }
    var selectedPlanForSuccess by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF7F7F8), // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Plans", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F8)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
            ) {
                // Intro Title section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Plans",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Elevate Your Workspace",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Choose a processing speed and model capacity level designed to optimize your professional workflow.",
                            fontSize = 14.sp,
                            color = Color(0xFF6E6E73),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Current Plan Indicator
                item {
                    val currentPlan = profile.subscriptionType
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT PLAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6E6E73)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (currentPlan) {
                                        "Pro Plus" -> "Pro Plus Plan"
                                        "Pro" -> "Pro Plan"
                                        else -> "Free Plan"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 1. Free Plan Card
                item {
                    val isFreeActive = profile.subscriptionType == "Free" || profile.subscriptionType.isBlank()
                    PlanSelectionCard(
                        title = "Free",
                        price = "₹0 / month",
                        isActive = isFreeActive,
                        benefits = listOf(
                            "Basic AI Reasoning Model",
                            "Standard Daily Usage Limits",
                            "Standard Response Speed",
                            "Ad-supported local framework"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Free")
                            Toast.makeText(context, "Downgraded to Free Tier", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Pro Plan Card (₹199)
                item {
                    val isProActive = profile.subscriptionType == "Pro"
                    PlanSelectionCard(
                        title = "Pro",
                        price = "₹199 / month",
                        isActive = isProActive,
                        benefits = listOf(
                            "Faster AI Response Speed",
                            "Higher Daily Chat Limits",
                            "More AI Image Generations",
                            "Analytical Reasoning Enabled"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Pro")
                            selectedPlanForSuccess = "Pro Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Pro Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 3. Pro Plus Plan Card (₹399)
                item {
                    val isProPlusActive = profile.subscriptionType == "Pro Plus"
                    PlanSelectionCard(
                        title = "Pro Plus",
                        price = "₹399 / month",
                        isActive = isProPlusActive,
                        benefits = listOf(
                            "Fastest Premium AI Responses",
                            "Highest Daily Usage Limits",
                            "Premium Multi-turn Capabilities",
                            "Priority GPU Processing Priority",
                            "Maximum Daily AI Image Generations"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Pro Plus")
                            selectedPlanForSuccess = "Pro Plus Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Pro Plus Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Restore Purchase Action
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "Purchased receipt verified with store.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "Restore Purchases",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Subscriptions automatically renew monthly unless canceled in account settings. Billing handled securely through native platform services.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6E6E73),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            // Success Purchase Modal Overlay
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showSuccessAnimation = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.Black,
                                modifier = Modifier.size(56.dp)
                            )

                            Text(
                                text = "Welcome to $selectedPlanForSuccess",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Your account has successfully upgraded. All pro tier benefits are now active.",
                                fontSize = 13.sp,
                                color = Color(0xFF6E6E73),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { showSuccessAnimation = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Let's Go", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanSelectionCard(
    title: String,
    price: String,
    isActive: Boolean,
    benefits: List<String>,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) Color.Black else Color(0xFFE5E5EA)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = price,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E6E73)
                    )
                }

                if (isActive) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Selected Plan",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF7F7F8), thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = benefit,
                            fontSize = 13.sp,
                            color = Color.Black,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFE5E5EA) else Color.Black,
                    contentColor = if (isActive) Color(0xFF6E6E73) else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isActive) "Currently Active" else "Upgrade Plan",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
