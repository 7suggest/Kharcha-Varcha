package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ExpenseEntity
import com.example.data.model.GroupEntity
import com.example.data.model.Member
import com.example.ui.components.ArcGaugeChart
import com.example.ui.components.DebtorCreditorCard
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.PillSegmentedControl
import com.example.ui.theme.CategoryDiningColor
import com.example.ui.theme.CategoryEntertainmentColor
import com.example.ui.theme.CategoryGroceriesColor
import com.example.ui.theme.CategoryHomeColor
import com.example.ui.theme.CategoryOtherColor
import com.example.ui.theme.CategoryTripColor
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToGroup: (String) -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current

    val combinedData by viewModel.combinedData.collectAsState()
    val timeFilter by viewModel.selectedTimeFilter.collectAsState()
    val ledgerTab by viewModel.selectedLedgerTab.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val currencySymbol = when (currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> "₹"
    }

    val currentUser = viewModel.getCurrentUser(combinedData.users)
    val filteredExpenses = viewModel.getFilteredExpenses(combinedData.expenses, timeFilter)
    val balanceSummary = viewModel.calculateBalanceSummary(currentUser.id, filteredExpenses, combinedData.settlements)
    val (creditors, debtors) = viewModel.calculateLedger(
        currentUser.id,
        combinedData.users,
        combinedData.groups,
        filteredExpenses,
        combinedData.settlements
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("dashboard_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar with Greeting & Brand
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kharcha Varcha",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "खर्चा व्‍हर्चा • Split & Settle Bills",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.brandAccent
                    )
                }

                // Profile Avatar chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(extendedColors.surfaceSecondary)
                        .clickable { viewModel.openEditProfileDialog() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("dashboard_profile_chip")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(extendedColors.brandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.firstOrNull()?.toString() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentUser.displayName.split(" ").firstOrNull() ?: "You",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Time Segment Filter Pill (All Time | This Month | This Week)
        item {
            PillSegmentedControl(
                options = listOf("All Time", "This Month", "This Week"),
                selectedIndex = timeFilter,
                onOptionSelected = { viewModel.setTimeFilter(it) },
                testTagPrefix = "time_filter"
            )
        }

        // 3. Hero Balance Card
        item {
            HeroBalanceCard(
                totalBalance = balanceSummary.totalNetBalance,
                currencySymbol = currencySymbol,
                onDetailsClick = onNavigateToAnalytics
            )
        }

        // 4. Metric Breakdown Cards (3 side by side)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // To Receive Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_card_owed"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = extendedColors.positive,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "To Receive",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+$currencySymbol${String.format("%,.0f", balanceSummary.totalOwedToUser)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.positive,
                            maxLines = 1
                        )
                    }
                }

                // To Give Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_card_debt"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = extendedColors.negative,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "To Give",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "-$currencySymbol${String.format("%,.0f", balanceSummary.totalUserOwes)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.negative,
                            maxLines = 1
                        )
                    }
                }

                // Group Spent Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_card_spent"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = extendedColors.brandAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Total Bills",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$currencySymbol${String.format("%,.0f", balanceSummary.totalExpenditure)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 5. Arc Gauge Visual Distribution Widget
        item {
            ArcGaugeChart(
                owedAmount = balanceSummary.totalOwedToUser,
                debtAmount = balanceSummary.totalUserOwes,
                currencySymbol = currencySymbol
            )
        }

        // 6. Granular Debtor / Creditor Ledger Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Individual Balance Ledger",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val count = if (ledgerTab == 0) creditors.size else debtors.size
                    Text(
                        text = "$count ${if (ledgerTab == 0) "people owe you" else "people to pay"}",
                        fontSize = 12.sp,
                        color = extendedColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher between "To Receive From" & "To Give To"
                PillSegmentedControl(
                    options = listOf(
                        "To Receive (${creditors.size})",
                        "To Give (${debtors.size})"
                    ),
                    selectedIndex = ledgerTab,
                    onOptionSelected = { viewModel.setLedgerTab(it) },
                    testTagPrefix = "ledger_tab"
                )
            }
        }

        // 7. Ledger Cards List
        val activeLedgerList = if (ledgerTab == 0) creditors else debtors
        if (activeLedgerList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = extendedColors.positive,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (ledgerTab == 0) "No pending payments to collect!" else "You have zero debts to settle!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Everyone is square in this category.",
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(activeLedgerList, key = { it.member.id + it.isOwedToUser }) { entry ->
                DebtorCreditorCard(
                    entry = entry,
                    currencySymbol = currencySymbol,
                    onActionClick = {
                        if (entry.isOwedToUser) {
                            // Remind action: Share reminder message
                            val message = "Hi ${entry.member.displayName}! Just a friendly reminder from Kharcha Varcha regarding our shared expenses (${entry.groups.joinToString(", ")}). Outstanding balance: $currencySymbol${String.format("%,.2f", entry.amount)}. Settle up when free!"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            try {
                                context.startActivity(Intent.createChooser(shareIntent, "Send Payment Reminder"))
                            } catch (_: Exception) {
                                viewModel.showToast("Reminder: ${entry.member.displayName} owes $currencySymbol${String.format("%,.2f", entry.amount)}")
                            }
                        } else {
                            // Settle Up action
                            viewModel.openSettleUp(
                                member = entry.member,
                                suggestedAmount = entry.amount,
                                isUserPaying = true
                            )
                        }
                    }
                )
            }
        }

        // 8. Active Groups Carousel / Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Shared Groups",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "See All (${combinedData.groups.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.brandAccent,
                    modifier = Modifier
                        .clickable { viewModel.setSelectedTab(1) }
                        .testTag("dashboard_see_all_groups")
                )
            }
        }

        if (combinedData.groups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = extendedColors.brandAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No shared groups yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create your first group (e.g. Flatmates, Road Trip, Office Lunch) to start adding bills with friends.",
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.Button(
                            onClick = { viewModel.openAddGroupDialog() },
                            shape = CircleShape,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                            modifier = Modifier.testTag("dashboard_create_first_group_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create First Group", fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(combinedData.groups, key = { it.id }) { group ->
                        val groupDetail = viewModel.getGroupDetail(
                            group = group,
                            users = combinedData.users,
                            expenses = combinedData.expenses,
                            settlements = combinedData.settlements,
                            currentUserId = currentUser.id
                        )

                        val categoryColor = when (group.category) {
                            "Trip" -> CategoryTripColor
                            "Home" -> CategoryHomeColor
                            "Dining" -> CategoryDiningColor
                            "Event" -> CategoryEntertainmentColor
                            else -> CategoryOtherColor
                        }

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onNavigateToGroup(group.id) }
                                .testTag("group_card_${group.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, extendedColors.borderSubtle),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(categoryColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = group.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = categoryColor
                                        )
                                    }

                                    Text(
                                        text = "${groupDetail.members.size} members",
                                        fontSize = 11.sp,
                                        color = extendedColors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = group.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Total: $currencySymbol${String.format("%,.0f", groupDetail.totalGroupSpent)}",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val net = groupDetail.userNetBalance
                                val netColor = if (net >= 0) extendedColors.positive else extendedColors.negative
                                val netPrefix = if (net >= 0) "+ " else "- "

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (net >= 0) "You get: " else "You owe: ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = extendedColors.textSecondary
                                    )
                                    Text(
                                        text = "$netPrefix$currencySymbol${String.format("%,.0f", Math.abs(net))}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Recent Activity Timeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${filteredExpenses.size} items",
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary
                )
            }
        }

        val recentExpenses = filteredExpenses.take(5)
        if (recentExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recent transactions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the '+' button below to add your first bill.",
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = onNavigateToAddExpense,
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Expense", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(recentExpenses, key = { it.id }) { exp ->
                val group = combinedData.groups.find { it.id == exp.groupId }
                val payer = combinedData.users.find { it.id == exp.paidByUserId }?.displayName ?: "Someone"
                val isPayer = exp.paidByUserId == currentUser.id

                val participants = exp.participantIdsCsv.split(",").filter { it.isNotBlank() }
                val isIncluded = participants.contains(currentUser.id)
                val splitPerPerson = if (participants.isNotEmpty()) exp.totalAmount / participants.size else 0.0

                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(exp.expenseDate))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_expense_${exp.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val catIcon = when (exp.category) {
                            "Dining" -> Icons.Default.Restaurant
                            "Groceries" -> Icons.Default.Payment
                            "Travel" -> Icons.Default.Star
                            "Housing" -> Icons.Default.Home
                            else -> Icons.Default.Receipt
                        }

                        val catColor = when (exp.category) {
                            "Dining" -> CategoryDiningColor
                            "Groceries" -> CategoryGroceriesColor
                            "Travel" -> CategoryTripColor
                            "Housing" -> CategoryHomeColor
                            else -> CategoryOtherColor
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = exp.category,
                                tint = catColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.titleNote,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${group?.name ?: "Expense"} • $payer paid $currencySymbol${String.format("%,.0f", exp.totalAmount)}",
                                fontSize = 12.sp,
                                color = extendedColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = extendedColors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // User status
                        Column(horizontalAlignment = Alignment.End) {
                            if (isPayer) {
                                val lent = exp.totalAmount - (if (isIncluded) splitPerPerson else 0.0)
                                Text(
                                    text = "You lent",
                                    fontSize = 11.sp,
                                    color = extendedColors.positive,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "+$currencySymbol${String.format("%,.0f", lent)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.positive
                                )
                            } else if (isIncluded) {
                                Text(
                                    text = "You borrowed",
                                    fontSize = 11.sp,
                                    color = extendedColors.negative,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "-$currencySymbol${String.format("%,.0f", splitPerPerson)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.negative
                                )
                            } else {
                                Text(
                                    text = "Not involved",
                                    fontSize = 11.sp,
                                    color = extendedColors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
