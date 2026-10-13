package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PillSegmentedControl
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val combinedData by viewModel.combinedData.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentUser = viewModel.getCurrentUser(combinedData.users)

    val currencies = listOf("INR", "USD", "EUR", "GBP", "JPY")
    val selectedCurrencyIndex = currencies.indexOf(currency).takeIf { it >= 0 } ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("settings_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Profile & Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Preferences, privacy compliance & data management",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )
            }
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openEditProfileDialog() }
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(extendedColors.brandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.firstOrNull()?.toString() ?: "U",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentUser.email.isNotBlank()) {
                            Text(
                                text = currentUser.email,
                                fontSize = 13.sp,
                                color = extendedColors.textSecondary
                            )
                        }
                        if (currentUser.upiId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "UPI: ${currentUser.upiId}",
                                fontSize = 12.sp,
                                color = extendedColors.brandAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(extendedColors.surfaceSecondary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Currency Preference Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = extendedColors.brandAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Display Currency Preference",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PillSegmentedControl(
                        options = listOf("₹ INR", "$ USD", "€ EUR", "£ GBP", "¥ JPY"),
                        selectedIndex = selectedCurrencyIndex,
                        onOptionSelected = { idx -> viewModel.setCurrency(currencies[idx]) },
                        testTagPrefix = "currency_pill"
                    )
                }
            }
        }

        // Compliance & Policy Section (Google Play Store compliance)
        item {
            Text(
                text = "Privacy & Store Compliance",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Data Safety & Privacy Info
                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        title = "Data Safety & Privacy Disclosures",
                        subtitle = "Encrypted data, zero tracking, non-banking bookkeeping",
                        iconTint = extendedColors.positive,
                        onClick = { viewModel.openPrivacyDialog() },
                        testTag = "settings_privacy_item"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // In-app Account & Data Deletion (Play Policy requirement)
                    SettingsRowItem(
                        icon = Icons.Default.Delete,
                        title = "Delete Account & Purge Data",
                        subtitle = "Mandatory in-app account & transaction record erasure",
                        iconTint = extendedColors.negative,
                        onClick = { viewModel.openDeleteAccountDialog() },
                        testTag = "settings_delete_account_item"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export Data
                    SettingsRowItem(
                        icon = Icons.Default.Share,
                        title = "Export Expense Statement",
                        subtitle = "Share backup summary of all groups & balances",
                        iconTint = extendedColors.brandAccent,
                        onClick = {
                            val exportText = buildString {
                                appendLine("Kharcha Varcha - Financial Ledger Statement")
                                appendLine("User: ${currentUser.displayName} (${currentUser.email})")
                                appendLine("Total Groups: ${combinedData.groups.size}")
                                appendLine("Total Expenses Logged: ${combinedData.expenses.size}")
                                appendLine("Total Spending: ${viewModel.formatAmount(combinedData.expenses.sumOf { it.totalAmount })}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, exportText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export Statement"))
                        },
                        testTag = "settings_export_item"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit Profile Details
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "Edit Profile & UPI Details",
                        subtitle = "Update your display name, email, and UPI address",
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.openEditProfileDialog() },
                        testTag = "settings_edit_profile_item"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign Out
                    SettingsRowItem(
                        icon = Icons.Default.Person,
                        title = "Sign Out / Switch Account",
                        subtitle = "Sign out from Google and switch account",
                        iconTint = extendedColors.brandAccent,
                        onClick = {
                            val credentialManager = androidx.credentials.CredentialManager.create(context)
                            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                            com.example.ui.screens.signOut(context, credentialManager, onSignOutComplete = {
                                viewModel.showToast("Signed out successfully")
                            }, scope = scope)
                        },
                        testTag = "settings_sign_out_item"
                    )
                }
            }
        }

        // App Information Footer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceSecondary),
                border = BorderStroke(1.dp, extendedColors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = extendedColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kharcha Varcha v1.0 • Built with Kotlin & Jetpack Compose",
                        fontSize = 12.sp,
                        color = extendedColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = extendedColors.textSecondary
            )
        }
    }
}
