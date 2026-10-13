package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Member
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddExpenseScreen(
    viewModel: MainViewModel,
    preselectedGroupId: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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

    var selectedGroupId by remember(preselectedGroupId, combinedData.groups) {
        mutableStateOf(preselectedGroupId ?: combinedData.groups.firstOrNull()?.id ?: "")
    }

    val selectedGroup = combinedData.groups.find { it.id == selectedGroupId }

    val groupMembers = remember(selectedGroup, combinedData.users) {
        if (selectedGroup != null) {
            val userMap = combinedData.users.associateBy { it.id }
            selectedGroup.memberIdsCsv.split(",").filter { it.isNotBlank() }.map { id ->
                userMap[id]?.let {
                    Member(it.id, it.displayName, it.email, it.phoneNumber, it.upiId, it.avatarColorHex, it.isCurrentUser)
                } ?: Member(id, "Member", "", "", "", "#0F172A", false)
            }
        } else {
            emptyList()
        }
    }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Dining") }
    var paidByUserId by remember(currentUser) { mutableStateOf(currentUser.id) }

    // Split Type: 0 = EQUAL, 1 = EXACT, 2 = PERCENTAGE
    var splitTypeIndex by remember { mutableStateOf(0) }
    val splitTypes = listOf("EQUAL", "EXACT", "PERCENTAGE")

    // Included member IDs (default: all members included)
    val includedMemberIds = remember(groupMembers) {
        mutableStateListOf<String>().apply {
            addAll(groupMembers.map { it.id })
        }
    }

    // Custom shares map (for EXACT and PERCENTAGE)
    val customSharesMap = remember(groupMembers) {
        mutableStateMapOf<String, String>()
    }

    // Date & Timestamp
    var expenseTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Receipt Image URI
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedReceiptUri = uri
    }

    val categories = listOf("Dining", "Groceries", "Travel", "Housing", "Entertainment", "Other")
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val includedCount = includedMemberIds.size
    val liveSplitPerPerson = if (includedCount > 0 && parsedAmount > 0) parsedAmount / includedCount else 0.0

    // Calendar for DatePicker
    val calendar = Calendar.getInstance().apply { timeInMillis = expenseTimestamp }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvas)
            .testTag("add_expense_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Expense",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Split equally, by exact amount, or percentages",
                        fontSize = 13.sp,
                        color = extendedColors.textSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_expense_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        if (combinedData.groups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Groups Found",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To log and split an expense, create a group first to add friends and flatmates.",
                            fontSize = 13.sp,
                            color = extendedColors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openAddGroupDialog() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                            modifier = Modifier.testTag("create_group_first_btn")
                        ) {
                            Text("Create Group Now")
                        }
                    }
                }
            }
        } else {
            // 1. Amount Input Card with Presets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Expense Amount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currencySymbol,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.brandAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                placeholder = { Text("0.00", fontSize = 28.sp) },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .width(200.dp)
                                    .testTag("expense_amount_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset Chips (+100, +500, +1000, +2000)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(100, 500, 1000, 2000).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(extendedColors.surfaceSecondary)
                                        .clickable {
                                            val current = amountText.toDoubleOrNull() ?: 0.0
                                            amountText = String.format(Locale.US, "%.0f", current + preset)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .testTag("preset_chip_$preset")
                                ) {
                                    Text(
                                        text = "+$currencySymbol$preset",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Title / Description
            item {
                Column {
                    Text(
                        text = "Description / Title",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Fisherman's Wharf Dinner, Uber Ride...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input")
                    )
                }
            }

            // 3. Category Selector
            item {
                Column {
                    Text(
                        text = "Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            val catColor = when (cat) {
                                "Dining" -> CategoryDiningColor
                                "Groceries" -> CategoryGroceriesColor
                                "Travel" -> CategoryTripColor
                                "Housing" -> CategoryHomeColor
                                "Entertainment" -> CategoryEntertainmentColor
                                else -> CategoryOtherColor
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) catColor else extendedColors.surfaceSecondary)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("expense_category_$cat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else extendedColors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 4. Group Selector
            item {
                Column {
                    Text(
                        text = "Select Group",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(combinedData.groups, key = { it.id }) { g ->
                            val isSelected = selectedGroupId == g.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.surfaceSecondary)
                                    .clickable { selectedGroupId = g.id }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("expense_group_select_${g.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g.name,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 5. Payer Selector (Who paid upfront?)
            item {
                Column {
                    Text(
                        text = "Paid By (Upfront Payer)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(groupMembers, key = { it.id }) { m ->
                            val isPayer = paidByUserId == m.id
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isPayer) extendedColors.positiveContainer else extendedColors.surfaceSecondary)
                                    .border(
                                        width = if (isPayer) 1.5.dp else 1.dp,
                                        color = if (isPayer) extendedColors.positive else extendedColors.borderSubtle,
                                        shape = CircleShape
                                    )
                                    .clickable { paidByUserId = m.id }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("payer_chip_${m.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (m.isCurrentUser) "You (${m.displayName})" else m.displayName,
                                    color = if (isPayer) extendedColors.positive else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = if (isPayer) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 6. Split Type Selector (Section 2.2 & 3.4: EQUAL, EXACT, PERCENTAGE)
            item {
                Column {
                    Text(
                        text = "Split Method",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PillSegmentedControl(
                        options = listOf("Equally (÷)", "Exact ($currencySymbol)", "Percentage (%)"),
                        selectedIndex = splitTypeIndex,
                        onOptionSelected = { splitTypeIndex = it },
                        testTagPrefix = "split_type_pill"
                    )
                }
            }

            // 7. Granular Participant Selector with Custom Shares Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, extendedColors.borderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Participants ($includedCount/${groupMembers.size} included)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (splitTypeIndex) {
                                        1 -> "Enter exact amounts for each member"
                                        2 -> "Enter percentages for each member (total 100%)"
                                        else -> "Equal split among checked members"
                                    },
                                    fontSize = 12.sp,
                                    color = extendedColors.textSecondary
                                )
                            }

                            if (splitTypeIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(extendedColors.positiveContainer)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$currencySymbol${String.format("%,.2f", liveSplitPerPerson)} / person",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.positive
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        groupMembers.forEach { member ->
                            val isChecked = includedMemberIds.contains(member.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            includedMemberIds.add(member.id)
                                        } else if (includedMemberIds.size > 1) {
                                            includedMemberIds.remove(member.id)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = extendedColors.brandAccent),
                                    modifier = Modifier.testTag("checkbox_member_${member.id}")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = member.displayName + if (member.isCurrentUser) " (You)" else "",
                                    fontSize = 14.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else extendedColors.textMuted,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isChecked) {
                                    when (splitTypeIndex) {
                                        1 -> {
                                            // EXACT amount input
                                            OutlinedTextField(
                                                value = customSharesMap[member.id] ?: "",
                                                onValueChange = { customSharesMap[member.id] = it },
                                                placeholder = { Text("0.00", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                modifier = Modifier.width(90.dp).height(48.dp)
                                            )
                                        }
                                        2 -> {
                                            // PERCENTAGE input
                                            OutlinedTextField(
                                                value = customSharesMap[member.id] ?: "",
                                                onValueChange = { customSharesMap[member.id] = it },
                                                placeholder = { Text("25%", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.width(80.dp).height(48.dp)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "$currencySymbol${String.format("%,.2f", liveSplitPerPerson)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = extendedColors.positive
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Excluded",
                                        fontSize = 12.sp,
                                        color = extendedColors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. Date & Time Picker (Section 3.4 #4: Tappable to change past or future dates)
            item {
                val formattedDate = SimpleDateFormat("EEEE, dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(expenseTimestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extendedColors.surfaceSecondary)
                        .clickable {
                            // Launch DatePicker then TimePicker
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            expenseTimestamp = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("expense_date_picker_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = extendedColors.brandAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expense Date & Timestamp",
                            fontSize = 11.sp,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.brandAccent
                    )
                }
            }

            // 9. Receipt Attachment (Section 2.1 & 2.2: Optional Receipt Upload)
            item {
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
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = extendedColors.brandAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Attach Receipt / Bill Photo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (selectedReceiptUri == null) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("pick_receipt_btn")
                                ) {
                                    Text("Add Photo", fontSize = 12.sp)
                                }
                            }
                        }

                        if (selectedReceiptUri != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = selectedReceiptUri,
                                    contentDescription = "Receipt Preview",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Receipt Attached ✓",
                                    fontSize = 12.sp,
                                    color = extendedColors.positive,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { selectedReceiptUri = null }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove receipt", tint = extendedColors.negative)
                                }
                            }
                        }
                    }
                }
            }

            // 10. Save Button
            item {
                val canSave = parsedAmount > 0 && title.isNotBlank() && selectedGroupId.isNotBlank() && includedMemberIds.isNotEmpty()

                Button(
                    onClick = {
                        if (canSave) {
                            val splitType = splitTypes[splitTypeIndex]
                            val customSharesCsv = when (splitType) {
                                "EXACT", "PERCENTAGE" -> {
                                    includedMemberIds.joinToString(",") { uid ->
                                        val value = customSharesMap[uid]?.toDoubleOrNull() ?: 0.0
                                        "$uid:$value"
                                    }
                                }
                                else -> ""
                            }

                            viewModel.addExpense(
                                groupId = selectedGroupId,
                                title = title,
                                category = selectedCategory,
                                amount = parsedAmount,
                                paidByUserId = paidByUserId,
                                participantIds = includedMemberIds.toList(),
                                splitType = splitType,
                                customSharesCsv = customSharesCsv,
                                receiptUri = selectedReceiptUri?.toString() ?: "",
                                date = expenseTimestamp
                            )
                            onDismiss()
                        }
                    },
                    enabled = canSave,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.brandAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_expense_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Expense & Split",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
