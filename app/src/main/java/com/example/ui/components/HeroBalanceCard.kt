package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HeroBalanceCard(
    totalBalance: Double,
    currencySymbol: String = "₹",
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val isSettled = Math.abs(totalBalance) < 0.01
    val isPositive = totalBalance > 0.01

    val balanceColor = when {
        isSettled -> extendedColors.textPrimary
        isPositive -> extendedColors.positive
        else -> extendedColors.negative
    }

    val badgeBg = when {
        isSettled -> extendedColors.surfaceSecondary
        isPositive -> extendedColors.positiveContainer
        else -> extendedColors.negativeContainer
    }

    val badgeText = when {
        isSettled -> "All Settled Up! 🎉"
        isPositive -> "You are owed in total"
        else -> "You owe in total"
    }

    val subtext = when {
        isSettled -> "No outstanding dues across your groups"
        isPositive -> "People owe you money across your groups"
        else -> "You have outstanding balances to settle"
    }

    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val absAmount = formatter.format(Math.abs(totalBalance))
    val prefix = when {
        isSettled -> ""
        isPositive -> "+ "
        else -> "- "
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, extendedColors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trend/Status Chip
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("hero_balance_status_badge")
            ) {
                Text(
                    text = badgeText,
                    color = balanceColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Hero Number
            Text(
                text = "$prefix$currencySymbol$absAmount",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                modifier = Modifier.testTag("hero_balance_amount")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtext,
                fontSize = 13.sp,
                color = extendedColors.textSecondary,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Details Action Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, extendedColors.borderSubtle, CircleShape)
                    .background(extendedColors.surfaceSecondary)
                    .clickable { onDetailsClick() }
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .testTag("hero_balance_details_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View Analytics",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
