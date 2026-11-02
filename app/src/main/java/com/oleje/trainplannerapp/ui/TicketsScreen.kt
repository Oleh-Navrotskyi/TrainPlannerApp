package com.oleje.trainplannerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oleje.trainplannerapp.domain.*
import com.oleje.trainplannerapp.ui.theme.*
import java.io.File


@Preview(showBackground = true, backgroundColor = 0xFF0B132B)
@Composable
fun TicketsScreenPreview() {
    TrainPlannerAppTheme {
        TicketsScreen(viewModel = MainViewModel())
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val catalogTickets by viewModel.catalogTickets.collectAsState()
    val purchasedTickets by viewModel.purchasedTickets.collectAsState()
    val userCategory by viewModel.currentUserCategory.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Каталог рейсів, 1: Куплені квитки
    var filterText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    var editingCatalogTicket by remember { mutableStateOf<TravelTicket?>(null) }
    var transferringPurchasedTicket by remember { mutableStateOf<NamedTravelTicket?>(null) }
    var newOwnerInput by remember { mutableStateOf("") }
    var showAddTicketDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UzBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Selector (Каталог vs Куплені)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UzCardHeader)
                    .padding(4.dp)
            ) {
                listOf("Каталог рейсів", "Куплені квитки (${purchasedTickets.size})").forEachIndexed { idx, title ->
                    val isSelected = selectedSubTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) UzBlueAccent else Color.Transparent)
                            .clickable { selectedSubTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else UzTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Filter & Sort Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    placeholder = { Text("Фільтр за містом...", fontSize = 12.sp, color = UzTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UzAccentYellow,
                        unfocusedBorderColor = UzDivider,
                        focusedTextColor = UzTextPrimary,
                        unfocusedTextColor = UzTextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Sort Dropdown button triggering custom MergeSort
                Box {
                    Button(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = UzCardHeader),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⇅ Сортувати", fontSize = 11.sp, color = UzAccentYellow)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(UzCardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("За ціною (MergeSort)", color = UzTextPrimary) },
                            onClick = {
                                viewModel.sortCatalogWithCustomMergeSort(TicketSorter.BY_PRICE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("За тривалістю (MergeSort)", color = UzTextPrimary) },
                            onClick = {
                                viewModel.sortCatalogWithCustomMergeSort(TicketSorter.BY_DURATION_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("За типом транспорту (MergeSort)", color = UzTextPrimary) },
                            onClick = {
                                viewModel.sortCatalogWithCustomMergeSort(TicketSorter.BY_TRANSPORT_TYPE)
                                showSortMenu = false
                            }
                        )
                    }
                }

                // Export to .txt button
                Button(
                    onClick = {
                        val file = File(context.cacheDir, "tickets_export.txt")
                        val listToExport: List<StreamableTicket> = if (selectedSubTab == 0) catalogTickets else purchasedTickets
                        viewModel.exportToTxtFile(file, listToExport)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzCardHeader),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Експорт .txt", fontSize = 11.sp, color = UzAccentYellow)
                }
            }
        }

        // Add Ticket Button if Admin
        if (selectedSubTab == 0 && userCategory == CitizenCategory.ADMIN) {
            item {
                Button(
                    onClick = { showAddTicketDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = UzSuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Додати новий рейс (Адмін)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Items List
        if (selectedSubTab == 0) {
            val filteredCatalog = catalogTickets.filter {
                filterText.isEmpty() ||
                        it.departureCity.contains(filterText, ignoreCase = true) ||
                        it.destinationCity.contains(filterText, ignoreCase = true)
            }

            items(filteredCatalog) { ticket ->
                CatalogTicketCard(
                    ticket = ticket,
                    isAdmin = userCategory == CitizenCategory.ADMIN,
                    onEditClick = { editingCatalogTicket = ticket }
                )
            }
        } else {
            val filteredPurchased = purchasedTickets.filter {
                filterText.isEmpty() ||
                        it.departureCity.contains(filterText, ignoreCase = true) ||
                        it.destinationCity.contains(filterText, ignoreCase = true) ||
                        it.ownerFullName.contains(filterText, ignoreCase = true)
            }

            items(filteredPurchased) { purchasedTicket ->
                PurchasedTicketCard(
                    ticket = purchasedTicket,
                    onChangeOwnerClick = {
                        transferringPurchasedTicket = purchasedTicket
                        newOwnerInput = ""
                    }
                )
            }
        }
    }

    // Change Owner Dialog (Requirement #6)
    transferringPurchasedTicket?.let { ticket ->
        AlertDialog(
            onDismissRequest = { transferringPurchasedTicket = null },
            containerColor = UzCardBg,
            title = { Text("Зміна власника квитка", color = UzTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Поточний власник: ${ticket.ownerFullName}", color = UzTextSecondary, fontSize = 13.sp)
                    Text("Маршрут: ${ticket.departureCity} ➔ ${ticket.destinationCity}", color = UzTextPrimary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = newOwnerInput,
                        onValueChange = { newOwnerInput = it },
                        label = { Text("ПІБ нового власника") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UzAccentYellow,
                            unfocusedBorderColor = UzDivider,
                            focusedTextColor = UzTextPrimary,
                            unfocusedTextColor = UzTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeTicketOwner(ticket.id, newOwnerInput)
                        transferringPurchasedTicket = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent)
                ) {
                    Text("Переписати квиток", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transferringPurchasedTicket = null }) {
                    Text("Скасувати", color = UzTextSecondary)
                }
            }
        )
    }

    // Edit Catalog Ticket Dialog (Admin)
    editingCatalogTicket?.let { ticket ->
        var editBasePrice by remember { mutableStateOf(ticket.basePrice.toString()) }
        var editDuration by remember { mutableStateOf(ticket.durationMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { editingCatalogTicket = null },
            containerColor = UzCardBg,
            title = { Text("Редагування рейсу ${ticket.departureCity} ➔ ${ticket.destinationCity}", color = UzTextPrimary, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editBasePrice,
                        onValueChange = { editBasePrice = it },
                        label = { Text("Базова ціна (грн)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UzAccentYellow,
                            unfocusedBorderColor = UzDivider,
                            focusedTextColor = UzTextPrimary,
                            unfocusedTextColor = UzTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Тривалість (хвилин)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UzAccentYellow,
                            unfocusedBorderColor = UzDivider,
                            focusedTextColor = UzTextPrimary,
                            unfocusedTextColor = UzTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val updated = TravelTicket(ticket)
                            updated.basePrice = editBasePrice.toDouble()
                            updated.durationMinutes = editDuration.toInt()
                            viewModel.updateCatalogTicket(updated)
                            editingCatalogTicket = null
                        } catch (e: Exception) {
                            // Handled via VM
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent)
                ) {
                    Text("Зберегти", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCatalogTicket(ticket.id)
                        editingCatalogTicket = null
                    }
                ) {
                    Text("Видалити рейс", color = UzErrorRed)
                }
            }
        )
    }

    // Add Ticket Dialog (Admin)
    if (showAddTicketDialog) {
        var newFrom by remember { mutableStateOf("") }
        var newTo by remember { mutableStateOf("") }
        var newPrice by remember { mutableStateOf("250") }
        var newDuration by remember { mutableStateOf("120") }

        AlertDialog(
            onDismissRequest = { showAddTicketDialog = false },
            containerColor = UzCardBg,
            title = { Text("Додавання нових параметрів квитка", color = UzTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFrom, onValueChange = { newFrom = it }, label = { Text("Звідки") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = UzTextPrimary, unfocusedTextColor = UzTextPrimary)
                    )
                    OutlinedTextField(
                        value = newTo, onValueChange = { newTo = it }, label = { Text("Куди") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = UzTextPrimary, unfocusedTextColor = UzTextPrimary)
                    )
                    OutlinedTextField(
                        value = newPrice, onValueChange = { newPrice = it }, label = { Text("Ціна (грн)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = UzTextPrimary, unfocusedTextColor = UzTextPrimary)
                    )
                    OutlinedTextField(
                        value = newDuration, onValueChange = { newDuration = it }, label = { Text("Тривалість (хв)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = UzTextPrimary, unfocusedTextColor = UzTextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val t = TravelTicket(
                                null, newFrom, newTo, CitizenCategory.REGULAR, 0.0,
                                newPrice.toDouble(), TransportType.COUPE, 500, newDuration.toInt()
                            )
                            viewModel.addCatalogTicket(t)
                            showAddTicketDialog = false
                        } catch (e: Exception) {
                            // Handled by VM
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent)
                ) {
                    Text("Створити квиток", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTicketDialog = false }) {
                    Text("Скасувати", color = UzTextSecondary)
                }
            }
        )
    }
}

@Composable
fun CatalogTicketCard(
    ticket: TravelTicket,
    isAdmin: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UzCardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${ticket.departureCity} ➔ ${ticket.destinationCity}",
                    color = UzTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${ticket.transportType.displayName} • ${ticket.durationMinutes} хв у дорозі",
                    color = UzTextSecondary,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${ticket.basePrice.toInt()} грн",
                    color = UzAccentYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onEditClick) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchasedTicketCard(
    ticket: NamedTravelTicket,
    onChangeOwnerClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UzCardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ticket.departureCity} ➔ ${ticket.destinationCity}",
                    color = UzTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${ticket.finalPaidPrice.toInt()} грн",
                    color = UzAccentYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "👤 ", fontSize = 14.sp)
                Text(
                    text = "Пасажир: ${ticket.ownerFullName}",
                    color = UzTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Застосований бонус: ${ticket.appliedBonusDescription}",
                color = UzTextSecondary,
                fontSize = 11.sp
            )

            Button(
                onClick = onChangeOwnerClick,
                colors = ButtonDefaults.buttonColors(containerColor = UzCardHeader),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Змінити власника (Пункт 6)", fontSize = 12.sp, color = UzAccentYellow)
            }
        }
    }
}
