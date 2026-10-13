package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AccountDeletionDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Account & Purge Data?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.negative
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "In compliance with Google Play Policy & App Store Guidelines, deleting your account will permanently wipe all your transaction records, groups, balances, and profile from this device.",
                    fontSize = 13.sp,
                    color = extendedColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action is irreversible. You can restore sample data at any time from Settings.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.performAccountDeletion() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.negative),
                modifier = Modifier.testTag("confirm_delete_account_btn")
            ) {
                Text("Delete Everything")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_account_btn")
            ) {
                Text("Cancel", color = extendedColors.textSecondary)
            }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Data Safety & Privacy Disclosures",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "• Classification: Kharcha Varcha is a personal bookkeeping & bill splitting utility. It is not a lending institution, credit provider, or bank.",
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Zero Tracking: We do not sell user data or include third-party ad tracking SDKs.",
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Account & Data Deletion: Users can delete their data in-app or via our web portal (https://kharchavarcha.com/delete-account).",
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier.testTag("close_privacy_dialog_btn")
            ) {
                Text("Understood")
            }
        }
    )
}
