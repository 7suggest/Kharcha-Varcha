package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun SettleUpDialog(
    state: MainViewModel.SettleUpState,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val targetMember = state.targetMember

    var amountText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", state.suggestedAmount))
    }
    var note by remember { mutableStateOf("Settled via UPI / PhonePe") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Settle Up with ${targetMember.displayName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (state.isUserPaying)
                        "You are recording a payment to clear your debt to ${targetMember.displayName}."
                    else
                        "${targetMember.displayName} is paying you to settle balances.",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )

                // Amount TextField
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount to Settle") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input")
                )

                // Note TextField
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Note / Method") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_note_input")
                )

                // Pay via UPI Button if target has upiId
                if (state.isUserPaying && targetMember.upiId.isNotBlank() && amount > 0) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceSecondary),
                        border = BorderStroke(1.dp, extendedColors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Pay using UPI Apps",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = targetMember.upiId,
                                    fontSize = 11.sp,
                                    color = extendedColors.textSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    val upiUri = Uri.parse(
                                        "upi://pay?pa=${targetMember.upiId}&pn=${Uri.encode(targetMember.displayName)}&am=${String.format(Locale.US, "%.2f", amount)}&cu=INR&tn=${Uri.encode(note)}"
                                    )
                                    val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                    try {
                                        context.startActivity(Intent.createChooser(upiIntent, "Pay via UPI"))
                                    } catch (_: Exception) {
                                        viewModel.showToast("No UPI apps found. Recording settlement locally.")
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.positive),
                                modifier = Modifier.testTag("launch_upi_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay Now", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0) {
                        val fromUser = if (state.isUserPaying) "user_self" else targetMember.id
                        val toUser = if (state.isUserPaying) targetMember.id else "user_self"
                        val groupId = state.groupId ?: viewModel.combinedData.value.groups.firstOrNull()?.id ?: ""
                        viewModel.recordSettlement(
                            groupId = groupId,
                            fromUserId = fromUser,
                            toUserId = toUser,
                            amount = amount,
                            note = note
                        )
                    }
                },
                enabled = amount > 0,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.positive),
                modifier = Modifier.testTag("confirm_settle_up_btn")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirm Settlement")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_settle_up_btn")
            ) {
                Text("Cancel", color = extendedColors.textSecondary)
            }
        }
    )
}
