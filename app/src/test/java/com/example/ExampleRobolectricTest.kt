package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseEntity
import com.example.data.model.SettlementEntity
import com.example.data.repository.ExpenseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kharcha Varcha", appName)
  }

  @Test
  fun `verify net balance calculation engine`() {
    val currentUserId = "user_self"

    // User paid 1200 split among 3 people (400 each). Others owe user 800.
    val expenses = listOf(
      ExpenseEntity(
        id = "exp_1",
        groupId = "g1",
        titleNote = "Dinner",
        category = "Dining",
        totalAmount = 1200.0,
        paidByUserId = currentUserId,
        splitType = "EQUAL",
        participantIdsCsv = "user_self,user_a,user_b",
        expenseDate = System.currentTimeMillis()
      )
    )

    val summary = ExpenseRepository.calculateBalanceSummary(currentUserId, expenses, emptyList())
    assertEquals(800.0, summary.totalNetBalance, 0.01)
    assertEquals(800.0, summary.totalOwedToUser, 0.01)
    assertEquals(0.0, summary.totalUserOwes, 0.01)
  }
}
