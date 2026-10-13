package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategorySpend
import com.example.data.model.ExpenseEntity
import com.example.data.model.GroupDetail
import com.example.data.model.GroupEntity
import com.example.data.model.LedgerEntry
import com.example.data.model.Member
import com.example.data.model.NetBalanceSummary
import com.example.data.model.SettlementEntity
import com.example.data.model.UserEntity
import com.example.data.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainViewModel(
    application: Application,
    val currentUserId: String
) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application, currentUserId)

    // Navigation and UI state
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    // 0 = All Time, 1 = This Month, 2 = This Week
    private val _selectedTimeFilter = MutableStateFlow(0)
    val selectedTimeFilter: StateFlow<Int> = _selectedTimeFilter.asStateFlow()

    // 0 = To Receive From (Creditors), 1 = To Give To (Debtors)
    private val _selectedLedgerTab = MutableStateFlow(0)
    val selectedLedgerTab: StateFlow<Int> = _selectedLedgerTab.asStateFlow()

    // Currency settings: INR, USD, EUR, GBP
    private val _currency = MutableStateFlow("INR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    // Settle Up Dialog State
    private val _settleUpDialog = MutableStateFlow<SettleUpState?>(null)
    val settleUpDialog: StateFlow<SettleUpState?> = _settleUpDialog.asStateFlow()

    // New Group Dialog State
    private val _isAddGroupDialogOpen = MutableStateFlow(false)
    val isAddGroupDialogOpen: StateFlow<Boolean> = _isAddGroupDialogOpen.asStateFlow()

    // Edit Profile Dialog State
    private val _isEditProfileDialogOpen = MutableStateFlow(false)
    val isEditProfileDialogOpen: StateFlow<Boolean> = _isEditProfileDialogOpen.asStateFlow()

    // Account Deletion Dialog State (Play Policy Compliance)
    private val _isDeleteAccountDialogOpen = MutableStateFlow(false)
    val isDeleteAccountDialogOpen: StateFlow<Boolean> = _isDeleteAccountDialogOpen.asStateFlow()

    // Privacy & Data Safety Dialog
    private val _isPrivacyDialogOpen = MutableStateFlow(false)
    val isPrivacyDialogOpen: StateFlow<Boolean> = _isPrivacyDialogOpen.asStateFlow()

    // Feedback Toast / SnackBar message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    data class SettleUpState(
        val targetMember: Member,
        val suggestedAmount: Double,
        val isUserPaying: Boolean,
        val groupId: String? = null
    )

    // Repository combined real-time flow from Cloud Firestore
    val combinedData: StateFlow<ExpenseRepository.CombinedData> = repository.combinedDataFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ExpenseRepository.CombinedData(emptyList(), emptyList(), emptyList(), emptyList())
        )

    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val name = user?.displayName ?: "You"
            val email = user?.email ?: ""
            repository.ensureCurrentUserExists(defaultName = name, defaultEmail = email)
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedGroup(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun setTimeFilter(filter: Int) {
        _selectedTimeFilter.value = filter
    }

    fun setLedgerTab(tab: Int) {
        _selectedLedgerTab.value = tab
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }

    fun openSettleUp(member: Member, suggestedAmount: Double, isUserPaying: Boolean, groupId: String? = null) {
        _settleUpDialog.value = SettleUpState(
            targetMember = member,
            suggestedAmount = suggestedAmount,
            isUserPaying = isUserPaying,
            groupId = groupId
        )
    }

    fun closeSettleUp() {
        _settleUpDialog.value = null
    }

    fun openAddGroupDialog() {
        _isAddGroupDialogOpen.value = true
    }

    fun closeAddGroupDialog() {
        _isAddGroupDialogOpen.value = false
    }

    fun openEditProfileDialog() {
        _isEditProfileDialogOpen.value = true
    }

    fun closeEditProfileDialog() {
        _isEditProfileDialogOpen.value = false
    }

    fun openDeleteAccountDialog() {
        _isDeleteAccountDialogOpen.value = true
    }

    fun closeDeleteAccountDialog() {
        _isDeleteAccountDialogOpen.value = false
    }

    fun openPrivacyDialog() {
        _isPrivacyDialogOpen.value = true
    }

    fun closePrivacyDialog() {
        _isPrivacyDialogOpen.value = false
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun updateUserProfile(name: String, email: String, upiId: String) {
        viewModelScope.launch {
            try {
                repository.updateUserProfile(name, email, upiId, _currency.value)
                _toastMessage.value = "Profile updated successfully!"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to update profile: ${e.localizedMessage}"
            }
        }
    }

    fun addMemberToGroup(groupId: String, memberName: String, upiId: String = "") {
        viewModelScope.launch {
            try {
                repository.addMemberToGroup(groupId, memberName, upiId)
                _toastMessage.value = "Added $memberName to group"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to add member: ${e.localizedMessage}"
            }
        }
    }

    fun getCurrentUser(users: List<UserEntity>): Member {
        val user = users.find { it.isCurrentUser } ?: run {
            val fbUser = FirebaseAuth.getInstance().currentUser
            UserEntity(
                id = currentUserId,
                displayName = fbUser?.displayName ?: "You",
                email = fbUser?.email ?: "",
                upiId = "",
                avatarColorHex = "#0F172A",
                isCurrentUser = true
            )
        }
        return Member(
            id = user.id,
            displayName = user.displayName,
            email = user.email,
            upiId = user.upiId,
            avatarColorHex = user.avatarColorHex,
            isCurrentUser = true
        )
    }

    fun getFilteredExpenses(allExpenses: List<ExpenseEntity>, filter: Int): List<ExpenseEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        return when (filter) {
            1 -> allExpenses.filter { now - it.expenseDate <= 30 * dayMillis } // Month
            2 -> allExpenses.filter { now - it.expenseDate <= 7 * dayMillis } // Week
            else -> allExpenses // All Time
        }
    }

    fun calculateBalanceSummary(
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>
    ): NetBalanceSummary {
        return ExpenseRepository.calculateBalanceSummary(currentUserId, expenses, settlements)
    }

    fun calculateLedger(
        users: List<UserEntity>,
        groups: List<GroupEntity>,
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>
    ): Pair<List<LedgerEntry>, List<LedgerEntry>> {
        return ExpenseRepository.calculateLedger(currentUserId, users, groups, expenses, settlements)
    }

    fun calculateCategorySpends(expenses: List<ExpenseEntity>): List<CategorySpend> {
        return ExpenseRepository.calculateCategorySpends(expenses)
    }

    fun getGroupDetail(
        group: GroupEntity,
        users: List<UserEntity>,
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>
    ): GroupDetail {
        val userMap = users.associateBy { it.id }
        val memberIds = group.memberIdsCsv.split(",").filter { it.isNotBlank() }
        val members = memberIds.map { id ->
            userMap[id]?.let {
                Member(
                    id = it.id,
                    displayName = it.displayName,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    upiId = it.upiId,
                    avatarColorHex = it.avatarColorHex,
                    isCurrentUser = it.isCurrentUser
                )
            } ?: Member(
                id = id,
                displayName = "Member",
                email = "",
                phoneNumber = "",
                upiId = "",
                avatarColorHex = "#0F172A",
                isCurrentUser = (id == currentUserId)
            )
        }

        val groupExpenses = expenses.filter { it.groupId == group.id }
        val groupSettlements = settlements.filter { it.groupId == group.id }
        val totalSpent = groupExpenses.sumOf { it.totalAmount }

        val memberNetBalances = mutableMapOf<String, Double>()
        members.forEach { m -> memberNetBalances[m.id] = 0.0 }

        groupExpenses.forEach { exp ->
            val pIds = exp.participantIdsCsv.split(",").filter { it.isNotBlank() }
            if (pIds.isNotEmpty()) {
                val share = exp.totalAmount / pIds.size
                memberNetBalances[exp.paidByUserId] = (memberNetBalances[exp.paidByUserId] ?: 0.0) + exp.totalAmount
                pIds.forEach { pId ->
                    memberNetBalances[pId] = (memberNetBalances[pId] ?: 0.0) - share
                }
            }
        }

        groupSettlements.forEach { set ->
            memberNetBalances[set.fromUserId] = (memberNetBalances[set.fromUserId] ?: 0.0) + set.amount
            memberNetBalances[set.toUserId] = (memberNetBalances[set.toUserId] ?: 0.0) - set.amount
        }

        val userNet = memberNetBalances[currentUserId] ?: 0.0

        return GroupDetail(
            group = group,
            members = members,
            totalGroupSpent = totalSpent,
            userNetBalance = userNet,
            memberBalances = memberNetBalances
        )
    }

    // Overload taking explicit currentUserId for existing calls
    fun getGroupDetail(
        group: GroupEntity,
        users: List<UserEntity>,
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>,
        currentUserId: String
    ): GroupDetail = getGroupDetail(group, users, expenses, settlements)

    // Overload taking explicit currentUserId for existing calls
    fun calculateBalanceSummary(
        currentUserId: String,
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>
    ): NetBalanceSummary = calculateBalanceSummary(expenses, settlements)

    // Overload taking explicit currentUserId for existing calls
    fun calculateLedger(
        currentUserId: String,
        users: List<UserEntity>,
        groups: List<GroupEntity>,
        expenses: List<ExpenseEntity>,
        settlements: List<SettlementEntity>
    ): Pair<List<LedgerEntry>, List<LedgerEntry>> = calculateLedger(users, groups, expenses, settlements)

    fun addExpense(
        groupId: String,
        title: String,
        category: String,
        amount: Double,
        paidByUserId: String,
        participantIds: List<String>,
        splitType: String = "EQUAL",
        customSharesCsv: String = "",
        receiptUri: String = "",
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            try {
                repository.addExpense(
                    groupId = groupId,
                    title = title,
                    category = category,
                    amount = amount,
                    paidByUserId = paidByUserId,
                    participantIds = participantIds,
                    splitType = splitType,
                    customSharesCsv = customSharesCsv,
                    receiptUri = receiptUri,
                    date = date
                )
                _selectedTab.value = 0
                _toastMessage.value = "Expense \"$title\" added!"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to add expense: ${e.localizedMessage}"
            }
        }
    }

    fun addGroup(name: String, description: String, category: String, memberNames: List<String>) {
        viewModelScope.launch {
            try {
                val newGroupId = repository.addGroup(name, description, category, memberNames)
                _isAddGroupDialogOpen.value = false
                _selectedGroupId.value = newGroupId
                _selectedTab.value = 1
                _toastMessage.value = "Group \"$name\" created!"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to create group: ${e.localizedMessage}"
            }
        }
    }

    fun recordSettlement(
        groupId: String,
        fromUserId: String,
        toUserId: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            try {
                repository.addSettlement(groupId, fromUserId, toUserId, amount, note)
                _settleUpDialog.value = null
                _toastMessage.value = "Settlement recorded: ${formatAmount(amount)}"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to record settlement: ${e.localizedMessage}"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expenseId)
                _toastMessage.value = "Expense deleted"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to delete expense: ${e.localizedMessage}"
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(groupId)
                if (_selectedGroupId.value == groupId) {
                    _selectedGroupId.value = null
                }
                _toastMessage.value = "Group deleted"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to delete group: ${e.localizedMessage}"
            }
        }
    }

    fun performAccountDeletion() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _isDeleteAccountDialogOpen.value = false
                _selectedGroupId.value = null
                _selectedTab.value = 0
                _toastMessage.value = "Account data cleared."
            } catch (e: Exception) {
                _toastMessage.value = "Failed to clear account: ${e.localizedMessage}"
            }
        }
    }

    fun formatAmount(amount: Double): String {
        val curr = _currency.value
        val symbol = when (curr) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            else -> "₹"
        }
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return "$symbol${formatter.format(amount)}"
    }

    companion object {
        fun provideFactory(application: Application, currentUserId: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application, currentUserId) as T
                }
            }
        }
    }
}
