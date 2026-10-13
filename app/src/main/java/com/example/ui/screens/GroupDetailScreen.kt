package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PillSegmentedControl
import com.example.ui.theme.CategoryDiningColor
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
fun GroupDetailScreen(
    groupId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current

    BackHandler { onBack() }

    val combinedData by viewModel.combinedData.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentUser = viewModel.getCurrentUser(combinedData.users)

    val group = combinedData.groups.find { it.id == groupId }
    if (group == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Group not found.", color = extendedColors.textSecondary)
        }
        return
    }

    val currencySymbol = when (currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> "₹"
    }

    val groupDetail = viewModel.getGroupDetail(
        group = group,
        users = combinedData.users,
        expenses = combinedData.expenses,
        settlements = combinedData.settlements,
        currentUserId = currentUser.id
    )

    val groupExpenses = combinedData.expenses.filter { it.groupId == group.id }
    val groupSettlements = combinedData.settlements.filter { it.groupId == group.id }

    var selectedFilterIndex by remember { mutableStateOf(0) } // 0: All, 1: Expenses, 2: Settlements, 3: Members

    val categoryColor = when (group.category) {
        "Trip" -> CategoryTripColor
        "Home" -> CategoryHomeColor
        "Dining" -> CategoryDiningColor
        "Event" -> CategoryDiningColor
        else -> CategoryOtherColor
    }

    val userNet = groupDetail.userNetBalance
    val isPositive = userNet >= 0
    val netColor = if (isPositive) extendedColors.positive else extendedColors.negative
    val netPrefix = if (isPositive) "+ " else "- "

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("group_detail_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("group_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val summaryText = buildString {
                            appendLine("📊 Kharcha Varcha: ${group.name} Summary")
                            appendLine("Total Spent: $currencySymbol${String.format("%,.2f", groupDetail.totalGroupSpent)}")
                            appendLine("Members: ${groupDetail.members.joinToString { it.displayName }}")
                            appendLine("--- Member Balances ---")
                            groupDetail.members.forEach { m ->
                                val b = groupDetail.memberBalances[m.id] ?: 0.0
                                val p = if (b >= 0) "+ " else "- "
                                appendLine("${m.displayName}: $p$currencySymbol${String.format("%,.2f", Math.abs(b))}")
                            }
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Group Report"))
                    },
                    modifier = Modifier.testTag("share_group_report_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.deleteGroup(group.id)
                        onBack()
                    },
                    modifier = Modifier.testTag("delete_group_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Group",
                        tint = extendedColors.textMuted
                    )
                }
            }
        }

        // Group Hero Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = group.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }

                        Text(
                            text = "${groupDetail.members.size} members",
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPositive) "Your net standing in this group:" else "You owe in this group:",
                        fontSize = 13.sp,
                        color = extendedColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$netPrefix$currencySymbol${String.format("%,.2f", Math.abs(userNet))}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Total group expenditure: $currencySymbol${String.format("%,.2f", groupDetail.totalGroupSpent)}",
                        fontSize = 13.sp,
                        color = extendedColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddExpense,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("group_detail_add_expense_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Expense", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                // Settle up with someone from group
                                val otherMember = groupDetail.members.find { !it.isCurrentUser }
                                if (otherMember != null) {
                                    val debt = groupDetail.memberBalances[otherMember.id] ?: 0.0
                                    viewModel.openSettleUp(
                                        member = otherMember,
                                        suggestedAmount = Math.abs(debt),
                                        isUserPaying = debt > 0,
                                        groupId = group.id
                                    )
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("group_detail_settle_up_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settle Up", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section Tabs: Activity Feed vs Member Balances
        item {
            PillSegmentedControl(
                options = listOf("Activity (${groupExpenses.size + groupSettlements.size})", "Members (${groupDetail.members.size})"),
                selectedIndex = selectedFilterIndex,
                onOptionSelected = { selectedFilterIndex = it },
                testTagPrefix = "group_detail_tab"
            )
        }

        // 1. Members Breakdown View
        if (selectedFilterIndex == 1) {
            item {
                var showAddMemberInput by remember { mutableStateOf(false) }
                var newMemberName by remember { mutableStateOf("") }
                var newMemberUpi by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Group Members (${groupDetail.members.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { showAddMemberInput = !showAddMemberInput },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                                modifier = Modifier.testTag("toggle_add_member_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showAddMemberInput) "Cancel" else "Add Member", fontSize = 12.sp)
                            }
                        }

                        if (showAddMemberInput) {
                            Spacer(modifier = Modifier.height(12.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = newMemberName,
                                onValueChange = { newMemberName = it },
                                label = { Text("Friend's Name *") },
                                placeholder = { Text("e.g. Alex, Maya") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("add_member_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = newMemberUpi,
                                onValueChange = { newMemberUpi = it },
                                label = { Text("UPI ID (Optional)") },
                                placeholder = { Text("e.g. alex@okaxis") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("add_member_upi_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (newMemberName.isNotBlank()) {
                                        viewModel.addMemberToGroup(group.id, newMemberName, newMemberUpi)
                                        newMemberName = ""
                                        newMemberUpi = ""
                                        showAddMemberInput = false
                                    }
                                },
                                enabled = newMemberName.isNotBlank(),
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth().testTag("confirm_add_member_btn")
                            ) {
                                Text("Add Friend to Group")
                            }
                        }
                    }
                }
            }

            items(groupDetail.members, key = { it.id }) { member ->
                val balance = groupDetail.memberBalances[member.id] ?: 0.0
                val isMemberPositive = balance >= 0
                val memberColor = if (isMemberPositive) extendedColors.positive else extendedColors.negative

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val color = try {
                                Color(android.graphics.Color.parseColor(member.avatarColorHex))
                            } catch (_: Exception) {
                                extendedColors.brandPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.displayName.firstOrNull()?.toString() ?: "M",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = member.displayName + if (member.isCurrentUser) " (You)" else "",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (member.upiId.isNotBlank()) {
                                    Text(
                                        text = "UPI: ${member.upiId}",
                                        fontSize = 11.sp,
                                        color = extendedColors.textSecondary
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isMemberPositive) "gets back" else "owes",
                                fontSize = 11.sp,
                                color = extendedColors.textSecondary
                            )
                            Text(
                                text = (if (isMemberPositive) "+ " else "- ") +
                                        currencySymbol +
                                        String.format("%,.2f", Math.abs(balance)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = memberColor
                            )
                        }
                    }
                }
            }
        } else {
            // 2. Activity Feed (Expenses & Settlements)
            if (groupExpenses.isEmpty() && groupSettlements.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, extendedColors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(28.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No expenses logged in this group yet.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Expense' above to add the first bill!",
                                fontSize = 12.sp,
                                color = extendedColors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(groupExpenses, key = { it.id }) { exp ->
                    val payer = combinedData.users.find { it.id == exp.paidByUserId }?.displayName ?: "Member"
                    val isUserPayer = exp.paidByUserId == currentUser.id
                    val participants = exp.participantIdsCsv.split(",").filter { it.isNotBlank() }
                    val isIncluded = participants.contains(currentUser.id)
                    val share = if (participants.isNotEmpty()) exp.totalAmount / participants.size else 0.0

                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(exp.expenseDate))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_expense_item_${exp.id}"),
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
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.titleNote,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$payer paid $currencySymbol${String.format("%,.2f", exp.totalAmount)} (${participants.size} people • ${exp.splitType})",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = extendedColors.textMuted
                                )
                                if (exp.receiptUri.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = exp.receiptUri,
                                            contentDescription = "Receipt thumbnail",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Receipt attached", fontSize = 11.sp, color = extendedColors.brandAccent)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                if (isUserPayer) {
                                    val lent = exp.totalAmount - (if (isIncluded) share else 0.0)
                                    Text(
                                        text = "You lent",
                                        fontSize = 11.sp,
                                        color = extendedColors.positive
                                    )
                                    Text(
                                        text = "+$currencySymbol${String.format("%,.2f", lent)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.positive
                                    )
                                } else if (isIncluded) {
                                    Text(
                                        text = "You owe",
                                        fontSize = 11.sp,
                                        color = extendedColors.negative
                                    )
                                    Text(
                                        text = "-$currencySymbol${String.format("%,.2f", share)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.negative
                                    )
                                } else {
                                    Text(
                                        text = "Excluded",
                                        fontSize = 11.sp,
                                        color = extendedColors.textMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete expense",
                                    tint = extendedColors.textMuted,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.deleteExpense(exp.id) }
                                )
                            }
                        }
                    }
                }

                items(groupSettlements, key = { it.id }) { set ->
                    val fromUser = combinedData.users.find { it.id == set.fromUserId }?.displayName ?: "Member"
                    val toUser = combinedData.users.find { it.id == set.toUserId }?.displayName ?: "Member"
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(set.settledAt))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceSecondary),
                        border = BorderStroke(1.dp, extendedColors.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(extendedColors.positiveContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = extendedColors.positive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$fromUser paid $toUser",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${set.note} • $dateStr",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                            }

                            Text(
                                text = "$currencySymbol${String.format("%,.2f", set.amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.positive
                            )
                        }
                    }
                }
            }
        }
    }
}
