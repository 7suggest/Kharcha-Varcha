package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerEntry
import com.example.ui.theme.LocalExtendedColors

@Composable
fun DebtorCreditorCard(
    entry: LedgerEntry,
    currencySymbol: String = "₹",
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val isOwedToUser = entry.isOwedToUser

    val amountColor = if (isOwedToUser) extendedColors.positive else extendedColors.negative
    val badgeBg = if (isOwedToUser) extendedColors.positiveContainer else extendedColors.negativeContainer
    val actionText = if (isOwedToUser) "Remind" else "Settle Up"
    val actionIcon = if (isOwedToUser) Icons.Default.Notifications else Icons.Default.CheckCircle

    val avatarColor = try {
        Color(android.graphics.Color.parseColor(entry.member.avatarColorHex))
    } catch (_: Exception) {
        extendedColors.brandAccent
    }

    val initials = entry.member.displayName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isOwedToUser) "creditor_item_${entry.member.id}" else "debtor_item_${entry.member.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, extendedColors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User & Group details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.member.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val groupText = if (entry.groups.isNotEmpty()) {
                    entry.groups.joinToString(", ")
                } else {
                    "Shared balance"
                }

                Text(
                    text = groupText,
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Action Button Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = (if (isOwedToUser) "+ " else "- ") +
                            currencySymbol +
                            String.format("%,.2f", entry.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .clickable { onActionClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(if (isOwedToUser) "remind_btn_${entry.member.id}" else "settle_btn_${entry.member.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionText,
                            tint = amountColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = actionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = amountColor
                        )
                    }
                }
            }
        }
    }
}
