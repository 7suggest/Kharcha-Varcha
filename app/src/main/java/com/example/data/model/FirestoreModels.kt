package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserFirestoreDoc(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val upiId: String = "",
    val avatarColorHex: String = "#0F172A",
    val defaultCurrency: String = "INR",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toEntity(docId: String, currentUserId: String): UserEntity {
        val actualId = if (id.isNotBlank()) id else docId
        return UserEntity(
            id = actualId,
            displayName = displayName.ifEmpty { "User" },
            email = email,
            phoneNumber = phoneNumber,
            upiId = upiId,
            avatarColorHex = avatarColorHex.ifEmpty { "#0F172A" },
            defaultCurrency = defaultCurrency.ifEmpty { "INR" },
            isCurrentUser = (actualId == currentUserId)
        )
    }
}

@IgnoreExtraProperties
data class GroupFirestoreDoc(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "Other",
    val createdBy: String = "",
    val memberIds: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toEntity(docId: String = ""): GroupEntity {
        val actualId = if (id.isNotBlank()) id else docId
        return GroupEntity(
            id = actualId,
            name = name,
            description = description,
            category = category,
            createdBy = createdBy,
            memberIdsCsv = memberIds.joinToString(","),
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }
}

@IgnoreExtraProperties
data class ExpenseFirestoreDoc(
    val id: String = "",
    val groupId: String = "",
    val titleNote: String = "",
    val category: String = "Other",
    val totalAmount: Double = 0.0,
    val paidByUserId: String = "",
    val splitType: String = "EQUAL",
    val participantIds: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val customSharesCsv: String = "",
    val receiptUri: String = "",
    val expenseDate: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toEntity(docId: String = ""): ExpenseEntity {
        val actualId = if (id.isNotBlank()) id else docId
        return ExpenseEntity(
            id = actualId,
            groupId = groupId,
            titleNote = titleNote,
            category = category,
            totalAmount = totalAmount,
            paidByUserId = paidByUserId,
            splitType = splitType,
            participantIdsCsv = participantIds.joinToString(","),
            customSharesCsv = customSharesCsv,
            receiptUri = receiptUri,
            expenseDate = if (expenseDate > 0L) expenseDate else (createdAt?.toDate()?.time ?: System.currentTimeMillis()),
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }
}

@IgnoreExtraProperties
data class SettlementFirestoreDoc(
    val id: String = "",
    val groupId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val amount: Double = 0.0,
    val note: String = "Settled via UPI",
    val settledAt: Long = 0L,
    val memberIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toEntity(docId: String = ""): SettlementEntity {
        val actualId = if (id.isNotBlank()) id else docId
        return SettlementEntity(
            id = actualId,
            groupId = groupId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            amount = amount,
            note = note,
            settledAt = if (settledAt > 0L) settledAt else (createdAt?.toDate()?.time ?: System.currentTimeMillis())
        )
    }
}
