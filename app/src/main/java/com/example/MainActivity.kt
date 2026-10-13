package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.FloatingBottomBar
import com.example.ui.screens.AccountDeletionDialog
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AddGroupDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditProfileDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.PrivacyPolicyDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettleUpDialog
import com.example.ui.theme.KharchaVarchaTheme
import com.example.ui.viewmodel.MainViewModel

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KharchaVarchaTheme {
                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                    FirebaseAuth.getInstance().addAuthStateListener(listener)
                    onDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(listener)
                    }
                }

                if (currentUser == null) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = FirebaseAuth.getInstance().currentUser
                        }
                    )
                } else {
                    val viewModel: MainViewModel = viewModel(
                        key = currentUser!!.uid,
                        factory = MainViewModel.provideFactory(application, currentUser!!.uid)
                    )
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val settleUpDialog by viewModel.settleUpDialog.collectAsState()
    val isAddGroupDialogOpen by viewModel.isAddGroupDialogOpen.collectAsState()
    val isDeleteAccountDialogOpen by viewModel.isDeleteAccountDialogOpen.collectAsState()
    val isPrivacyDialogOpen by viewModel.isPrivacyDialogOpen.collectAsState()
    val isEditProfileDialogOpen by viewModel.isEditProfileDialogOpen.collectAsState()
    val combinedData by viewModel.combinedData.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Handle back navigation for tabs and nested group detail
    BackHandler(enabled = selectedGroupId != null || selectedTab != 0) {
        if (selectedGroupId != null) {
            viewModel.setSelectedGroup(null)
        } else if (selectedTab != 0) {
            viewModel.setSelectedTab(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Show bottom bar on all main screens (hide when inside AddExpense tab if desired, or keep accessible)
            if (selectedGroupId == null && selectedTab != 2) {
                FloatingBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tabIndex ->
                        if (tabIndex == 1) {
                            viewModel.setSelectedGroup(null)
                        }
                        viewModel.setSelectedTab(tabIndex)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedGroupId != null -> {
                    GroupDetailScreen(
                        groupId = selectedGroupId!!,
                        viewModel = viewModel,
                        onBack = { viewModel.setSelectedGroup(null) },
                        onAddExpense = { viewModel.setSelectedTab(2) }
                    )
                }
                selectedTab == 0 -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToGroup = { gId ->
                            viewModel.setSelectedGroup(gId)
                        },
                        onNavigateToAddExpense = {
                            viewModel.setSelectedTab(2)
                        },
                        onNavigateToAnalytics = {
                            viewModel.setSelectedTab(3)
                        }
                    )
                }
                selectedTab == 1 -> {
                    GroupsScreen(
                        viewModel = viewModel,
                        onSelectGroup = { gId ->
                            viewModel.setSelectedGroup(gId)
                        }
                    )
                }
                selectedTab == 2 -> {
                    AddExpenseScreen(
                        viewModel = viewModel,
                        preselectedGroupId = selectedGroupId,
                        onDismiss = { viewModel.setSelectedTab(0) }
                    )
                }
                selectedTab == 3 -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                selectedTab == 4 -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Dialogs
    settleUpDialog?.let { state ->
        SettleUpDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { viewModel.closeSettleUp() }
        )
    }

    if (isAddGroupDialogOpen) {
        AddGroupDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeAddGroupDialog() }
        )
    }

    if (isDeleteAccountDialogOpen) {
        AccountDeletionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDeleteAccountDialog() }
        )
    }

    if (isPrivacyDialogOpen) {
        PrivacyPolicyDialog(
            onDismiss = { viewModel.closePrivacyDialog() }
        )
    }

    if (isEditProfileDialogOpen) {
        val currentUser = viewModel.getCurrentUser(combinedData.users)
        EditProfileDialog(
            currentUser = currentUser,
            viewModel = viewModel,
            onDismiss = { viewModel.closeEditProfileDialog() }
        )
    }
}
