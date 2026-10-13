package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addGroupAndExpense_authenticatedUser_success() = runBlocking {
        val aliceUid = signInTestUser("alice@test.com")
        val repository = ExpenseRepository(firestore, aliceUid)

        // Ensure user profile
        repository.ensureCurrentUserExists(defaultName = "Alice", defaultEmail = "alice@test.com")

        // Create group
        val groupId = repository.addGroup(
            name = "Weekend Roadtrip",
            description = "Trip to the hills",
            category = "Trip",
            memberNames = listOf("Bob")
        )
        assertNotNull(groupId)
        assertTrue(groupId.startsWith("group_"))

        // Add expense
        repository.addExpense(
            groupId = groupId,
            title = "Fuel & Toll",
            category = "Travel",
            amount = 2500.0,
            paidByUserId = aliceUid,
            participantIds = listOf(aliceUid),
            splitType = "EQUAL"
        )

        // Observe groups
        val groups = repository.groupsFlow.first()
        assertTrue(groups.any { it.id == groupId && it.name == "Weekend Roadtrip" })

        // Observe expenses
        val expenses = repository.expensesFlow.first()
        assertTrue(expenses.any { it.groupId == groupId && it.totalAmount == 2500.0 })
    }

    @Test
    fun unauthenticatedAccess_failsGracefully() = runBlocking {
        auth.signOut()
        val repository = ExpenseRepository(firestore, "")
        var failed = false
        try {
            repository.addGroup("Secret Group", "Desc", "Other", emptyList())
        } catch (e: Exception) {
            failed = true
        }
        assertTrue("Unauthenticated group creation should fail", failed)
    }
}
