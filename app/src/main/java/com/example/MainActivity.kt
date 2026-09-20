package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.InvoiceEditScreen
import com.example.ui.screens.InvoiceHistoryScreen
import com.example.ui.screens.InvoicePreviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.AdManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: InvoiceViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode) {
                InvoiceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InvoiceApp(viewModel: InvoiceViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == AppTab.EDIT,
                    onClick = { viewModel.setTab(AppTab.EDIT) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == AppTab.EDIT) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                            contentDescription = "Edit Invoice"
                        )
                    },
                    label = { Text("Invoice", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_edit")
                )

                NavigationBarItem(
                    selected = activeTab == AppTab.PREVIEW,
                    onClick = {
                        viewModel.generatePdfAndSwitchToPreview()
                    },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == AppTab.PREVIEW) Icons.Filled.PictureAsPdf else Icons.Outlined.PictureAsPdf,
                            contentDescription = "Preview"
                        )
                    },
                    label = { Text("Preview", fontWeight = FontWeight.SemiBold, maxLines = 1) },
                    modifier = Modifier.testTag("nav_tab_preview")
                )

                NavigationBarItem(
                    selected = activeTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("Saved", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.EDIT -> InvoiceEditScreen(viewModel = viewModel)
                    AppTab.PREVIEW -> InvoicePreviewScreen(viewModel = viewModel)
                    AppTab.HISTORY -> InvoiceHistoryScreen(viewModel = viewModel)
                }
            }
        }
    }
}
