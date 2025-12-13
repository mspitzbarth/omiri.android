package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.omiri.ui.components.InterestsContent
import com.example.omiri.ui.components.ScreenHeader
import com.example.omiri.viewmodels.SettingsViewModel

@Composable
fun InterestsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val selectedOptions by viewModel.personalizationOptions.collectAsState()

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Interests & Personalization",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .padding(horizontal = com.example.omiri.ui.theme.Spacing.lg)
        ) {
            InterestsContent(
                categories = categories,
                selectedOptions = selectedOptions,
                onOptionToggle = { key, option, isSelected ->
                    viewModel.togglePersonalizationOption(key, option, isSelected)
                },
                showHeader = false
            )
        }
    }
}
