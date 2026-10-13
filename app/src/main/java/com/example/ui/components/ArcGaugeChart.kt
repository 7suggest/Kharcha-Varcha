package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors

@Composable
fun ArcGaugeChart(
    owedAmount: Double,
    debtAmount: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val total = owedAmount + debtAmount
    val owedRatio = if (total > 0) (owedAmount / total).toFloat().coerceIn(0f, 1f) else 0.5f
    val debtRatio = if (total > 0) (debtAmount / total).toFloat().coerceIn(0f, 1f) else 0.5f

    val owedPercent = (owedRatio * 100).toInt()
    val debtPercent = (debtRatio * 100).toInt()

    val greenColor = extendedColors.positive
    val redColor = extendedColors.negative
    val trackColor = extendedColors.surfaceSecondary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("arc_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, extendedColors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settlement Ratio & Balance Flow",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Arc
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 24.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // 270 degree arc starts at 135 degrees (bottom left) and sweeps 270 degrees
                    val startAngle = 135f
                    val totalSweep = 270f

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (total > 0) {
                        // Green (Owed to user)
                        val greenSweep = totalSweep * owedRatio
                        if (greenSweep > 0) {
                            drawArc(
                                color = greenColor,
                                startAngle = startAngle,
                                sweepAngle = greenSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Red (User owes)
                        val redSweep = totalSweep * debtRatio
                        if (redSweep > 0) {
                            drawArc(
                                color = redColor,
                                startAngle = startAngle + greenSweep,
                                sweepAngle = redSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Center Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (total > 0) "${owedPercent}%" else "100%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (owedAmount >= debtAmount) greenColor else redColor
                    )
                    Text(
                        text = if (total > 0) "Owed Ratio" else "Settled",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Green Chip (To Receive)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(extendedColors.positiveContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(greenColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "To Receive: $currencySymbol${String.format("%,.0f", owedAmount)} ($owedPercent%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = greenColor
                    )
                }

                // Red Chip (To Pay)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(extendedColors.negativeContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(redColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "To Give: $currencySymbol${String.format("%,.0f", debtAmount)} ($debtPercent%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = redColor
                    )
                }
            }
        }
    }
}
