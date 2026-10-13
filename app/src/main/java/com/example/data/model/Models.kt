package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String = "",
    val upiId: String = "",
    val avatarColorHex: String = "#0F172A",
    val defaultCurrency: String = "INR",
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "Trip", "Home", "Couple", "Dining", "Event", "Other"
    val createdBy: String,
    val memberIdsCsv: String, // comma-separated user IDs
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val titleNote: String,
    val category: String, // "Travel", "Housing", "Groceries", "Dining", "Entertainment", "Other"
    val totalAmount: Double,
    val paidByUserId: String,
    val splitType: String = "EQUAL", // "EQUAL", "EXACT", "PERCENTAGE"
    val participantIdsCsv: String, // comma-separated user IDs included in this bill
    val customSharesCsv: String = "", // "userId:amount,userId:amount" or "userId:percent,userId:percent"
    val receiptUri: String = "", // Optional attached receipt image URI
    val expenseDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val note: String = "Settled via UPI",
    val settledAt: Long = System.currentTimeMillis()
)

// UI and Domain Models

data class Member(
    val id: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String = "",
    val upiId: String = "",
    val avatarColorHex: String = "#0F172A",
    val isCurrentUser: Boolean = false
)

data class GroupDetail(
    val group: GroupEntity,
    val members: List<Member>,
    val totalGroupSpent: Double,
    val userNetBalance: Double,
    val memberBalances: Map<String, Double>
)

data class ExpenseDetail(
    val expense: ExpenseEntity,
    val groupName: String,
    val payer: Member,
    val participants: List<Member>,
    val userShare: Double,
    val userNetEffect: Double,
    val splitPerPerson: Double
)

data class SettlementDetail(
    val settlement: SettlementEntity,
    val groupName: String,
    val fromMember: Member,
    val toMember: Member
)

data class LedgerEntry(
    val member: Member,
    val groups: List<String>,
    val amount: Double,
    val isOwedToUser: Boolean
)

data class NetBalanceSummary(
    val totalNetBalance: Double,
    val totalOwedToUser: Double,
    val totalUserOwes: Double,
    val totalExpenditure: Double
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float
)
