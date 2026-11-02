package com.oleje.trainplannerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oleje.trainplannerapp.domain.CitizenCategory
import com.oleje.trainplannerapp.ui.theme.*
import java.io.File


@Preview(showBackground = true, backgroundColor = 0xFF0B132B)
@Composable
fun SettingsScreenPreview() {
    TrainPlannerAppTheme {
        SettingsScreen(viewModel = MainViewModel())
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentCategory by viewModel.currentUserCategory.collectAsState()
    val bonuses by viewModel.userBonuses.collectAsState()

    var showAddBonusDialog by remember { mutableStateOf(false) }
    var bonusType by remember { mutableStateOf(0) } // 0: Certificate (UAH), 1: Promo (Percent)
    var bonusValueInput by remember { mutableStateOf("200") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UzBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Citizen Category & Role Selector
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
                    Text(
                        text = "Профіль пасажира та Категорія пільг",
                        color = UzTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Оберіть категорію для автоматичного розрахунку знижок та пільг на квитки:",
                        color = UzTextSecondary,
                        fontSize = 12.sp
                    )

                    CitizenCategory.values().forEach { category ->
                        val isSelected = currentCategory == category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) UzCardHeader else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setCurrentUserCategory(category) },
                                colors = RadioButtonDefaults.colors(selectedColor = UzAccentYellow)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = category.displayName,
                                    color = UzTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (category == CitizenCategory.PRIVILEGED) {
                                    Text("100% безкоштовні приміські електрички", color = UzSuccessGreen, fontSize = 11.sp)
                                } else if (category == CitizenCategory.STUDENT) {
                                    Text("50% знижки на базовий тариф", color = UzAccentYellow, fontSize = 11.sp)
                                } else if (category == CitizenCategory.ADMIN) {
                                    Text("Права редагування каталогу рейсів", color = UzBlueAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Dynamic Bonus Wallet (Certificates & Promo codes)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Гаманець бонусів та знижок",
                            color = UzTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Button(
                            onClick = { showAddBonusDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Додати", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    if (bonuses.isEmpty()) {
                        Text(
                            text = "У вас немає активних бонусних сертифікатів або промокодів",
                            color = UzTextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            bonuses.forEach { bonus ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(UzCardHeader, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🎟️ ", fontSize = 16.sp)
                                        Text(text = bonus.title, color = UzTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    }

                                    TextButton(onClick = { viewModel.removeBonusDiscount(bonus.id) }) {
                                        Text("Видалити", color = UzErrorRed, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: File I/O Management
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
                    Text(
                        text = "Ввід / Вивід даних у .txt файли (Заміна << та >>)",
                        color = UzTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Button(
                        onClick = {
                            val sampleFile = File(context.cacheDir, "sample_routes.txt")
                            if (!sampleFile.exists()) {
                                sampleFile.writeText(
                                    "t901;Львів;Ворохта;REGULAR;0.0;220.0;SUBURBAN;420;180\n" +
                                            "t902;Ворохта;Івано-Франківськ;REGULAR;0.0;110.0;SUBURBAN;630;90\n"
                                )
                            }
                            viewModel.importFromTxtFile(sampleFile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UzCardHeader),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Імпортувати розклад з файлу .txt", color = UzAccentYellow)
                    }

                    Button(
                        onClick = {
                            val exportFile = File(context.cacheDir, "maga_database_export.txt")
                            viewModel.exportToTxtFile(exportFile, viewModel.catalogTickets.value)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UzCardHeader),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зберегти всю базу у файл .txt", color = UzTextPrimary)
                    }
                }
            }
        }
    }

    // Add Bonus Dialog
    if (showAddBonusDialog) {
        AlertDialog(
            onDismissRequest = { showAddBonusDialog = false },
            containerColor = UzCardBg,
            title = { Text("Додати новий бонус у гаманець", color = UzTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { bonusType = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = if (bonusType == 0) UzBlueAccent else UzCardHeader),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Сертифікат (грн)", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { bonusType = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = if (bonusType == 1) UzBlueAccent else UzCardHeader),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Промокод (%)", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = bonusValueInput,
                        onValueChange = { bonusValueInput = it },
                        label = { Text(if (bonusType == 0) "Сума сертифіката (грн)" else "Відсоток знижки (%)") },
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
                        val value = bonusValueInput.toDoubleOrNull() ?: 0.0
                        if (bonusType == 0) {
                            viewModel.addCertificateDiscount(value)
                        } else {
                            viewModel.addPromoCodeDiscount(value)
                        }
                        showAddBonusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UzBlueAccent)
                ) {
                    Text("Додати", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBonusDialog = false }) {
                    Text("Скасувати", color = UzTextSecondary)
                }
            }
        )
    }
}
