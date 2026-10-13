package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ArcGaugeChart
import com.example.ui.theme.CategoryDiningColor
import com.example.ui.theme.CategoryEntertainmentColor
import com.example.ui.theme.CategoryGroceriesColor
import com.example.ui.theme.CategoryHomeColor
import com.example.ui.theme.CategoryOtherColor
import com.example.ui.theme.CategoryTripColor
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val combinedData by viewModel.combinedData.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentUser = viewModel.getCurrentUser(combinedData.users)

    val currencySymbol = when (currency) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> "₹"
    }

    val balanceSummary = viewModel.calculateBalanceSummary(
        currentUser.id,
        combinedData.expenses,
        combinedData.settlements
    )

    val categorySpends = viewModel.calculateCategorySpends(combinedData.expenses)
    val totalSpend = combinedData.expenses.sumOf { it.totalAmount }
    val totalSettled = combinedData.settlements.sumOf { it.amount }
    val settlementRatio = if (totalSpend > 0) (totalSettled / totalSpend).toFloat().coerceIn(0f, 1f) else 1f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("analytics_screen"),
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
                    text = "Financial Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Group spend breakdown, cohorts & settlement velocity",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )
            }
        }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.hero_banner_1791910223174),
                        contentDescription = "Splitting Bills Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Smart Group Intelligence",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track shared spending patterns, ensure accurate repayment, and eliminate awkward conversations.",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }
            }
        }

        // Settlement Efficiency Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, extendedColors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Group Settlement Health",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settlementRatio * 100).toInt()}% Settled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.positive
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { settlementRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = extendedColors.positive,
                        trackColor = extendedColors.surfaceSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Logged: $currencySymbol${String.format("%,.0f", totalSpend)}",
                            fontSize = 12.sp,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            text = "Settled: $currencySymbol${String.format("%,.0f", totalSettled)}",
                            fontSize = 12.sp,
                            color = extendedColors.positive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Gauge Chart
        item {
            ArcGaugeChart(
                owedAmount = balanceSummary.totalOwedToUser,
                debtAmount = balanceSummary.totalUserOwes,
                currencySymbol = currencySymbol
            )
        }

        // Category Breakdown Header
        item {
            Text(
                text = "Spending by Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Category Cards
        if (categorySpends.isEmpty()) {
            item {
                Text(
                    text = "No category data available.",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )
            }
        } else {
            items(categorySpends, key = { it.category }) { cat ->
                val catColor = when (cat.category) {
                    "Dining" -> CategoryDiningColor
                    "Groceries" -> CategoryGroceriesColor
                    "Travel" -> CategoryTripColor
                    "Housing" -> CategoryHomeColor
                    "Entertainment" -> CategoryEntertainmentColor
                    else -> CategoryOtherColor
                }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.category,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currencySymbol${String.format("%,.2f", cat.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${String.format("%.1f", cat.percentage)}%)",
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { cat.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = catColor,
                            trackColor = extendedColors.surfaceSecondary
                        )
                    }
                }
            }
        }
    }
}
