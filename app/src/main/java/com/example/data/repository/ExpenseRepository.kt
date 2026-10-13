package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CategorySpend
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseFirestoreDoc
import com.example.data.model.GroupEntity
import com.example.data.model.GroupFirestoreDoc
import com.example.data.model.LedgerEntry
import com.example.data.model.Member
import com.example.data.model.NetBalanceSummary
import com.example.data.model.SettlementEntity
import com.example.data.model.SettlementFirestoreDoc
import com.example.data.model.UserEntity
import com.example.data.model.UserFirestoreDoc
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExpenseRepository(
    private val db: FirebaseFirestore,
    val currentUserId: String
) {
    // Secondary constructor resolving named database ID from firebase_applet_config.xml
    constructor(context: Context, currentUserId: String) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        currentUserId
    )

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: if (currentUserId.isNotBlank()) currentUserId else throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    val usersFlow: Flow<List<UserEntity>> = callbackFlow {
        val path = "users"
        val registration = db.collection(path).addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserFirestoreDoc::class.java)?.toEntity(doc.id, currentUserId)
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { registration.remove() }
    }

    val groupsFlow: Flow<List<GroupEntity>> = callbackFlow {
        val uid = requireUserId()
        val path = "groups"
        val registration = db.collection(path)
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDoc::class.java)?.toEntity()
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { registration.remove() }
    }

    val expensesFlow: Flow<List<ExpenseEntity>> = callbackFlow {
        val uid = requireUserId()
        val path = "expenses"
        val registration = db.collection(path)
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExpenseFirestoreDoc::class.java)?.toEntity()
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { registration.remove() }
    }

    val settlementsFlow: Flow<List<SettlementEntity>> = callbackFlow {
        val uid = requireUserId()
        val path = "settlements"
        val registration = db.collection(path)
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val settlements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SettlementFirestoreDoc::class.java)?.toEntity()
                } ?: emptyList()
                trySend(settlements)
            }
        awaitClose { registration.remove() }
    }

    data class CombinedData(
        val users: List<UserEntity>,
        val groups: List<GroupEntity>,
        val expenses: List<ExpenseEntity>,
        val settlements: List<SettlementEntity>
    )

    val combinedDataFlow: Flow<CombinedData> = combine(
        usersFlow,
        groupsFlow,
        expensesFlow,
        settlementsFlow
    ) { users, groups, expenses, settlements ->
        CombinedData(users, groups, expenses, settlements)
    }

    suspend fun ensureCurrentUserExists(defaultName: String = "You", defaultEmail: String = "", defaultUpiId: String = "") {
        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        try {
            val snapshot = userDocRef.get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "id" to uid,
                    "displayName" to defaultName.ifEmpty { "You" },
                    "email" to defaultEmail,
                    "phoneNumber" to "",
                    "upiId" to defaultUpiId,
                    "avatarColorHex" to "#0F172A",
                    "defaultCurrency" to "INR",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocRef.path)
        }
    }

    suspend fun updateUserProfile(displayName: String, email: String, upiId: String, currency: String = "INR") {
        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        val payload = mapOf(
            "id" to uid,
            "displayName" to displayName.trim().ifEmpty { "You" },
            "email" to email.trim(),
            "upiId" to upiId.trim(),
            "defaultCurrency" to currency,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDocRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDocRef.path)
            throw e
        }
    }

    suspend fun addExpense(
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
        val uid = requireUserId()
        val groupSnapshot = db.collection("groups").document(groupId).get().await()
        val groupDoc = groupSnapshot.toObject(GroupFirestoreDoc::class.java)
        val memberIds = groupDoc?.memberIds ?: listOf(uid)

        val newExpenseId = "exp_${UUID.randomUUID().toString().take(8)}"
        val expenseDocRef = db.collection("expenses").document(newExpenseId)
        val payload = mapOf(
            "id" to newExpenseId,
            "groupId" to groupId,
            "titleNote" to title.trim(),
            "category" to category,
            "totalAmount" to amount,
            "paidByUserId" to paidByUserId,
            "splitType" to splitType,
            "participantIds" to participantIds,
            "memberIds" to memberIds,
            "customSharesCsv" to customSharesCsv,
            "receiptUri" to receiptUri,
            "expenseDate" to date,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            expenseDocRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, expenseDocRef.path)
            throw e
        }
    }

    suspend fun addGroup(
        name: String,
        description: String,
        category: String,
        memberNames: List<String>
    ): String {
        val uid = requireUserId()
        val memberIds = mutableListOf(uid)
        val memberNamesMap = mutableMapOf<String, String>()
        val currentUserName = auth.currentUser?.displayName ?: "You"
        memberNamesMap[uid] = currentUserName

        memberNames.forEach { nameItem ->
            val trimmed = nameItem.trim()
            if (trimmed.isNotBlank()) {
                val usersQuery = try {
                    db.collection("users").get().await()
                } catch (e: Exception) {
                    null
                }
                val existing = usersQuery?.documents?.find { doc ->
                    val userDoc = doc.toObject(UserFirestoreDoc::class.java)
                    userDoc?.email.equals(trimmed, ignoreCase = true) ||
                            userDoc?.displayName.equals(trimmed, ignoreCase = true)
                }

                if (existing != null) {
                    if (!memberIds.contains(existing.id)) {
                        memberIds.add(existing.id)
                        val name = existing.toObject(UserFirestoreDoc::class.java)?.displayName ?: trimmed
                        memberNamesMap[existing.id] = name
                    }
                } else {
                    val newId = "user_${UUID.randomUUID().toString().take(6)}"
                    memberIds.add(newId)
                    memberNamesMap[newId] = trimmed
                }
            }
        }

        val newGroupId = "group_${UUID.randomUUID().toString().take(8)}"
        val groupDocRef = db.collection("groups").document(newGroupId)
        val payload = mapOf(
            "id" to newGroupId,
            "name" to name.trim(),
            "description" to description.trim(),
            "category" to category,
            "createdBy" to uid,
            "memberIds" to memberIds,
            "memberNames" to memberNamesMap,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            groupDocRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, groupDocRef.path)
            throw e
        }
        return newGroupId
    }

    suspend fun addMemberToGroup(groupId: String, memberName: String, upiId: String = "") {
        val groupDocRef = db.collection("groups").document(groupId)
        val groupSnapshot = groupDocRef.get().await()
        val groupDoc = groupSnapshot.toObject(GroupFirestoreDoc::class.java) ?: return

        val trimmed = memberName.trim()
        if (trimmed.isBlank()) return

        val usersQuery = try {
            db.collection("users").get().await()
        } catch (e: Exception) {
            null
        }
        val existing = usersQuery?.documents?.find { doc ->
            val user = doc.toObject(UserFirestoreDoc::class.java)
            user?.email.equals(trimmed, ignoreCase = true) ||
                    user?.displayName.equals(trimmed, ignoreCase = true)
        }

        val memberId = if (existing != null) {
            existing.id
        } else {
            "user_${UUID.randomUUID().toString().take(6)}"
        }

        val updatedMembers = (groupDoc.memberIds + memberId).distinct()
        val updatedNames = groupDoc.memberNames.toMutableMap().apply {
            put(memberId, trimmed)
        }
        try {
            groupDocRef.update(
                "memberIds", updatedMembers,
                "memberNames", updatedNames,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, groupDocRef.path)
            throw e
        }
    }

    suspend fun addSettlement(
        groupId: String,
        fromUserId: String,
        toUserId: String,
        amount: Double,
        note: String
    ) {
        val uid = requireUserId()
        val groupSnapshot = db.collection("groups").document(groupId).get().await()
        val groupDoc = groupSnapshot.toObject(GroupFirestoreDoc::class.java)
        val memberIds = groupDoc?.memberIds ?: listOf(uid)

        val newSettlementId = "set_${UUID.randomUUID().toString().take(8)}"
        val settlementDocRef = db.collection("settlements").document(newSettlementId)
        val payload = mapOf(
            "id" to newSettlementId,
            "groupId" to groupId,
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "amount" to amount,
            "note" to note.trim().ifEmpty { "Settled via UPI" },
            "settledAt" to System.currentTimeMillis(),
            "memberIds" to memberIds,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            settlementDocRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, settlementDocRef.path)
            throw e
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        val docRef = db.collection("expenses").document(expenseId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGroup(groupId: String) {
        val groupDocRef = db.collection("groups").document(groupId)
        try {
            // Delete expenses in this group
            val expQuery = db.collection("expenses").whereEqualTo("groupId", groupId).get().await()
            expQuery.documents.forEach { it.reference.delete().await() }

            // Delete settlements in this group
            val setQuery = db.collection("settlements").whereEqualTo("groupId", groupId).get().await()
            setQuery.documents.forEach { it.reference.delete().await() }

            // Delete group
            groupDocRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, groupDocRef.path)
            throw e
        }
    }

    suspend fun clearAllData() {
        val uid = requireUserId()
        try {
            val userDocRef = db.collection("users").document(uid)
            userDocRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid")
        }
    }

    companion object {
        fun getParticipantShares(exp: ExpenseEntity): Map<String, Double> {
            val participants = exp.participantIdsCsv.split(",").filter { it.isNotBlank() }
            if (participants.isEmpty()) return emptyMap()

            return when (exp.splitType) {
                "EXACT" -> {
                    val map = mutableMapOf<String, Double>()
                    exp.customSharesCsv.split(",").forEach { item ->
                        val parts = item.split(":")
                        if (parts.size == 2) {
                            val uid = parts[0]
                            val amt = parts[1].toDoubleOrNull() ?: 0.0
                            map[uid] = amt
                        }
                    }
                    participants.associateWith { map[it] ?: (exp.totalAmount / participants.size) }
                }
                "PERCENTAGE" -> {
                    val map = mutableMapOf<String, Double>()
                    exp.customSharesCsv.split(",").forEach { item ->
                        val parts = item.split(":")
                        if (parts.size == 2) {
                            val uid = parts[0]
                            val pct = parts[1].toDoubleOrNull() ?: 0.0
                            map[uid] = exp.totalAmount * (pct / 100.0)
                        }
                    }
                    participants.associateWith { map[it] ?: (exp.totalAmount / participants.size) }
                }
                else -> {
                    val share = exp.totalAmount / participants.size
                    participants.associateWith { share }
                }
            }
        }

        fun calculateBalanceSummary(
            currentUserId: String,
            expenses: List<ExpenseEntity>,
            settlements: List<SettlementEntity>
        ): NetBalanceSummary {
            var owedToUser = 0.0
            var userOwes = 0.0
            var totalExpenditure = 0.0

            val pairwise = mutableMapOf<String, Double>()

            expenses.forEach { exp ->
                totalExpenditure += exp.totalAmount
                val shares = getParticipantShares(exp)

                if (exp.paidByUserId == currentUserId) {
                    shares.forEach { (pId, share) ->
                        if (pId != currentUserId) {
                            pairwise[pId] = pairwise.getOrDefault(pId, 0.0) + share
                        }
                    }
                } else {
                    if (shares.containsKey(currentUserId)) {
                        val payerId = exp.paidByUserId
                        val userShare = shares[currentUserId] ?: 0.0
                        pairwise[payerId] = pairwise.getOrDefault(payerId, 0.0) - userShare
                    }
                }
            }

            settlements.forEach { set ->
                if (set.fromUserId == currentUserId) {
                    val toId = set.toUserId
                    pairwise[toId] = pairwise.getOrDefault(toId, 0.0) + set.amount
                } else if (set.toUserId == currentUserId) {
                    val fromId = set.fromUserId
                    pairwise[fromId] = pairwise.getOrDefault(fromId, 0.0) - set.amount
                }
            }

            pairwise.values.forEach { net ->
                if (net > 0.001) {
                    owedToUser += net
                } else if (net < -0.001) {
                    userOwes += -net
                }
            }

            val totalNet = owedToUser - userOwes
            return NetBalanceSummary(
                totalNetBalance = totalNet,
                totalOwedToUser = owedToUser,
                totalUserOwes = userOwes,
                totalExpenditure = totalExpenditure
            )
        }

        fun calculateLedger(
            currentUserId: String,
            users: List<UserEntity>,
            groups: List<GroupEntity>,
            expenses: List<ExpenseEntity>,
            settlements: List<SettlementEntity>
        ): Pair<List<LedgerEntry>, List<LedgerEntry>> {
            val userMap = users.associateBy { it.id }
            val groupMap = groups.associateBy { it.id }

            val pairwise = mutableMapOf<String, Double>()
            val pairwiseGroups = mutableMapOf<String, MutableSet<String>>()

            expenses.forEach { exp ->
                val groupName = groupMap[exp.groupId]?.name ?: "Shared Expense"
                val shares = getParticipantShares(exp)

                if (exp.paidByUserId == currentUserId) {
                    shares.forEach { (pId, share) ->
                        if (pId != currentUserId) {
                            pairwise[pId] = pairwise.getOrDefault(pId, 0.0) + share
                            pairwiseGroups.getOrPut(pId) { mutableSetOf() }.add(groupName)
                        }
                    }
                } else {
                    if (shares.containsKey(currentUserId)) {
                        val payerId = exp.paidByUserId
                        val userShare = shares[currentUserId] ?: 0.0
                        pairwise[payerId] = pairwise.getOrDefault(payerId, 0.0) - userShare
                        pairwiseGroups.getOrPut(payerId) { mutableSetOf() }.add(groupName)
                    }
                }
            }

            settlements.forEach { set ->
                val groupName = groupMap[set.groupId]?.name ?: "Settlement"
                if (set.fromUserId == currentUserId) {
                    val toId = set.toUserId
                    pairwise[toId] = pairwise.getOrDefault(toId, 0.0) + set.amount
                    pairwiseGroups.getOrPut(toId) { mutableSetOf() }.add(groupName)
                } else if (set.toUserId == currentUserId) {
                    val fromId = set.fromUserId
                    pairwise[fromId] = pairwise.getOrDefault(fromId, 0.0) - set.amount
                    pairwiseGroups.getOrPut(fromId) { mutableSetOf() }.add(groupName)
                }
            }

            val creditors = mutableListOf<LedgerEntry>()
            val debtors = mutableListOf<LedgerEntry>()

            pairwise.forEach { (otherId, balance) ->
                val user = userMap[otherId] ?: UserEntity(
                    id = otherId,
                    displayName = "Member",
                    email = ""
                )
                val member = Member(
                    id = user.id,
                    displayName = user.displayName,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    upiId = user.upiId,
                    avatarColorHex = user.avatarColorHex,
                    isCurrentUser = user.isCurrentUser
                )
                val groupList = pairwiseGroups[otherId]?.toList() ?: emptyList()

                if (balance > 0.01) {
                    creditors.add(
                        LedgerEntry(
                            member = member,
                            groups = groupList,
                            amount = balance,
                            isOwedToUser = true
                        )
                    )
                } else if (balance < -0.01) {
                    debtors.add(
                        LedgerEntry(
                            member = member,
                            groups = groupList,
                            amount = -balance,
                            isOwedToUser = false
                        )
                    )
                }
            }

            return Pair(
                creditors.sortedByDescending { it.amount },
                debtors.sortedByDescending { it.amount }
            )
        }

        fun calculateCategorySpends(expenses: List<ExpenseEntity>): List<CategorySpend> {
            val total = expenses.sumOf { it.totalAmount }
            if (total <= 0.0) return emptyList()

            val byCat = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

            return byCat.map { (cat, amount) ->
                CategorySpend(
                    category = cat,
                    amount = amount,
                    percentage = ((amount / total) * 100).toFloat()
                )
            }.sortedByDescending { it.amount }
        }
    }
}
