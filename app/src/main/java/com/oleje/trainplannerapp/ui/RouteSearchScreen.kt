package com.oleje.trainplannerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oleje.trainplannerapp.domain.RoutePlanner
import com.oleje.trainplannerapp.ui.theme.*


@Preview(showBackground = true, backgroundColor = 0xFF0B132B)
@Composable
fun RouteSearchScreenPreview() {
    TrainPlannerAppTheme {
        RouteSearchScreen(viewModel = MainViewModel())
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreen(viewModel: MainViewModel) {
    val origin by viewModel.searchOrigin.collectAsState()
    val destination by viewModel.searchDestination.collectAsState()
    val mode by viewModel.optimizationMode.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val plans by viewModel.searchResultPlans.collectAsState()
    val userCategory by viewModel.currentUserCategory.collectAsState()

    var selectedTabCategory by remember { mutableStateOf(0) } // 0: Дальні, 1: Приміські, 2: City Express
    var showPurchaseDialog by remember { mutableStateOf<RoutePlanner.PersonalTravelPlan?>(null) }
    var passengerNameInput by remember { mutableStateOf("Олег Навроцький") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UzBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Category Pills (matching screenshot)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("Дальні", "Приміські", "City Express")
                categories.forEachIndexed { index, title ->
                    val isSelected = selectedTabCategory == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) UzTextPrimary else UzCardBg)
                            .clickable { selectedTabCategory = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) UzBackground else UzTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Search Card (Dark Card matching UZ design)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = UzCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Station From & To inputs with Swap Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Станція відправлення", color = UzTextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = origin,
                                onValueChange = { viewModel.updateSearchOrigin(it) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UzAccentYellow,
                                    unfocusedBorderColor = UzDivider,
                                    focusedTextColor = UzTextPrimary,
                                    unfocusedTextColor = UzTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "Станція прибуття", color = UzTextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = destination,
                                onValueChange = { viewModel.updateSearchDestination(it) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UzAccentYellow,
                                    unfocusedBorderColor = UzDivider,
                                    focusedTextColor = UzTextPrimary,
                                    unfocusedTextColor = UzTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = { viewModel.swapSearchStations() },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                                .background(UzCardHeader)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Поміняти місцями",
                                tint = UzAccentYellow
                            )
                        }
                    }

                    // Optimization Mode Selector Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isCheapest = mode == RoutePlanner.OptimizationMode.CHEAPEST
                        Button(
                            onClick = { viewModel.setOptimizationMode(RoutePlanner.OptimizationMode.CHEAPEST) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCheapest) UzBlueAccent else UzCardHeader
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Персональний план", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.setOptimizationMode(RoutePlanner.OptimizationMode.FASTEST) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isCheapest) UzBlueAccent else UzCardHeader
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Найшвидший", fontSize = 11.sp)
                        }
                    }

                    // Primary Action Button "Знайти"
                    Button(
                        onClick = { viewModel.searchRoutes() },
                        colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSearching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(color = UzTextPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Знайти маршрут", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Search Results Section
        item {
            Text(
                text = "Знайдені варіанти (Топ-3)",
                color = UzTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (plans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Введіть міста та натисніть «Знайти маршрут» для формування персонального плану",
                        color = UzTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            itemsIndexed(plans) { index, plan ->
                PlanCard(
                    plan = plan,
                    planIndex = index + 1,
                    userCategory = userCategory,
                    onBuyClick = { showPurchaseDialog = plan }
                )
            }
        }
    }

    // Purchase Dialog
    showPurchaseDialog?.let { plan ->
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = null },
            containerColor = UzCardBg,
            title = { Text("Оформлення квитка", color = UzTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Введіть ПІБ пасажира для оформлення іменного квитка:", color = UzTextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = passengerNameInput,
                        onValueChange = { passengerNameInput = it },
                        label = { Text("ПІБ пасажира") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UzAccentYellow,
                            unfocusedBorderColor = UzDivider,
                            focusedTextColor = UzTextPrimary,
                            unfocusedTextColor = UzTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "До сплати: ${plan.finalDiscountedCost.toInt()} грн (Економія: ${plan.totalSaved.toInt()} грн)",
                        color = UzAccentYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purchasePlan(plan, passengerNameInput)
                        showPurchaseDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent)
                ) {
                    Text("Підтвердити і купити", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = null }) {
                    Text("Скасувати", color = UzTextSecondary)
                }
            }
        )
    }
}

@Composable
fun PlanCard(
    plan: RoutePlanner.PersonalTravelPlan,
    planIndex: Int,
    userCategory: com.oleje.trainplannerapp.domain.CitizenCategory,
    onBuyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UzCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (planIndex == 1) 1.5.dp else 0.dp,
                color = if (planIndex == 1) UzAccentYellow else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (planIndex == 1) UzAccentYellow else UzCardHeader),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$planIndex",
                            color = if (planIndex == 1) UzBackground else UzTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (planIndex == 1) "Найвигідніший план" else "Альтернатива #$planIndex",
                        color = UzTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (plan.totalSaved > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(UzSuccessGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Економія -${plan.totalSaved.toInt()} грн",
                            color = UzSuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = UzDivider)

            // Segments
            plan.segments.forEach { ticket ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚆 ", fontSize = 14.sp)
                        Text(
                            text = "${ticket.departureCity} ➔ ${ticket.destinationCity}",
                            color = UzTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${ticket.transportType.displayName} (${ticket.calculateCategoryPrice(userCategory).toInt()} грн)",
                        color = UzTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Show applied bonus discount for this segment
                plan.appliedDiscounts[ticket]?.let { bonus ->
                    Row(
                        modifier = Modifier.padding(start = 26.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎟️ ", fontSize = 12.sp)
                        Text(
                            text = "Застосовано: ${bonus.title}",
                            color = UzAccentYellow,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = UzDivider)

            // Price & Duration Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏱️ ", fontSize = 14.sp)
                    Text(
                        text = "${plan.totalDurationMinutes / 60} год ${plan.totalDurationMinutes % 60} хв у дорозі",
                        color = UzTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (plan.totalSaved > 0) {
                        Text(
                            text = "${plan.baseTotalCost.toInt()} грн",
                            color = UzTextSecondary,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                    Text(
                        text = "${plan.finalDiscountedCost.toInt()} грн",
                        color = UzAccentYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Купити персональний план", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
