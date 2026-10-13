package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CategoryDiningColor
import com.example.ui.theme.CategoryEntertainmentColor
import com.example.ui.theme.CategoryGroceriesColor
import com.example.ui.theme.CategoryHomeColor
import com.example.ui.theme.CategoryOtherColor
import com.example.ui.theme.CategoryTripColor
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GroupsScreen(
    viewModel: MainViewModel,
    onSelectGroup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val combinedData by viewModel.combinedData.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentUser = viewModel.getCurrentUser(combinedData.users)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Trip", "Home", "Dining", "Event", "Other")

    val currencySymbol = when (currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> "₹"
    }

    val filteredGroups = combinedData.groups.filter { group ->
        val matchesCategory = selectedCategoryFilter == "All" || group.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || group.name.contains(searchQuery, ignoreCase = true) || group.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("groups_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shared Groups",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage trips, flats, events & split groups",
                        fontSize = 13.sp,
                        color = extendedColors.textSecondary
                    )
                }

                Button(
                    onClick = { viewModel.openAddGroupDialog() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                    modifier = Modifier.testTag("create_group_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Group",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Group", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("group_search_input"),
                placeholder = { Text("Search groups by name...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = extendedColors.textMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategoryFilter == category
                    val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.surfaceSecondary
                    val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else extendedColors.textSecondary

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(chipBg)
                            .clickable { selectedCategoryFilter = category }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("category_filter_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = chipText,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Groups List
        if (filteredGroups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No groups found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ New Group' above to create one!",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredGroups, key = { it.id }) { group ->
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

                val net = groupDetail.userNetBalance
                val isPositive = net >= 0
                val netColor = if (isPositive) extendedColors.positive else extendedColors.negative
                val netPrefix = if (isPositive) "+ " else "- "

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGroup(group.id) }
                        .testTag("group_list_item_${group.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(categoryColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = group.category,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${groupDetail.members.size} members",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open group",
                                tint = extendedColors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = group.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (group.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = group.description,
                                fontSize = 13.sp,
                                color = extendedColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom row: Group Total and User's Net balance
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(extendedColors.surfaceSecondary)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Group Spent",
                                    fontSize = 11.sp,
                                    color = extendedColors.textSecondary
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", groupDetail.totalGroupSpent)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isPositive) "Your standing" else "You owe",
                                    fontSize = 11.sp,
                                    color = extendedColors.textSecondary
                                )
                                Text(
                                    text = "$netPrefix$currencySymbol${String.format("%,.0f", Math.abs(net))}",
                                    fontSize = 14.sp,
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
}
