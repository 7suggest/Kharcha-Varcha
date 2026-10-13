package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CategoryDiningColor
import com.example.ui.theme.CategoryEntertainmentColor
import com.example.ui.theme.CategoryHomeColor
import com.example.ui.theme.CategoryOtherColor
import com.example.ui.theme.CategoryTripColor
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AddGroupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Trip") }
    var memberNamesInput by remember { mutableStateOf("") }

    val categories = listOf("Trip", "Home", "Dining", "Couple", "Event", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Group",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name *") },
                    placeholder = { Text("e.g. Manali Trek 2026, Flat 101...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Short note on purpose...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_desc_input")
                )

                Column {
                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            val catColor = when (cat) {
                                "Trip" -> CategoryTripColor
                                "Home" -> CategoryHomeColor
                                "Dining" -> CategoryDiningColor
                                "Event" -> CategoryEntertainmentColor
                                else -> CategoryOtherColor
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) catColor else extendedColors.surfaceSecondary)
                                    .clickable { category = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("create_group_cat_$cat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else extendedColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = memberNamesInput,
                    onValueChange = { memberNamesInput = it },
                    label = { Text("Add Members (Emails or Names)") },
                    placeholder = { Text("e.g. friend@gmail.com, Priya, Rahul...") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_members_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val memberList = memberNamesInput.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        viewModel.addGroup(
                            name = name,
                            description = description,
                            category = category,
                            memberNames = memberList
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                modifier = Modifier.testTag("confirm_create_group_btn")
            ) {
                Text("Create Group")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_group_btn")
            ) {
                Text("Cancel", color = extendedColors.textSecondary)
            }
        }
    )
}
