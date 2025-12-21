package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

/**
 * Bottom sheet for inputting ingredients and generating a recipe via AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeGenerationBottomSheet(
    onDismiss: () -> Unit,
    onGenerate: (ingredients: List<String>, preferences: String?, language: String, includeShoppingList: Boolean) -> Unit,
    isGenerating: Boolean = false
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        var ingredientsText by remember { mutableStateOf("") }
        var preferencesText by remember { mutableStateOf("") }
        var selectedLanguage by remember { mutableStateOf("en") }
        val languages = listOf(
            "en" to "English 🇺🇸",
            "de" to "Deutsch 🇩🇪",
            "fr" to "Français 🇫🇷",
            "es" to "Español 🇪🇸",
            "it" to "Italiano 🇮🇹"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
                .padding(bottom = Spacing.xl)
        ) {
            Text(
                text = "AI Recipe Generator",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.BrandInk
            )
            
            Text(
                text = "Tell us what you have in your fridge!",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Neutral500
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.BrandInk
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            OmiriTextField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                placeholder = "e.g. Chicken, Spinach, Onion",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "Preferences (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.BrandInk
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            OmiriTextArea(
                value = preferencesText,
                onValueChange = { preferencesText = it },
                placeholder = "e.g. Vegetarian, under 30 mins, spicy",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "Language",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.BrandInk
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { (code, label) ->
                    FilterChip(
                        selected = selectedLanguage == code,
                        onClick = { selectedLanguage = code },
                        label = { Text(label.split(" ").last()) }, // Show just the flag for compactness
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.BrandOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            var includeShoppingList by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include ingredients I need to buy",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandInk
                    )
                    Text(
                        text = "Suggested items will show prices and stores",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Neutral500
                    )
                }
                Switch(
                    checked = includeShoppingList,
                    onCheckedChange = { includeShoppingList = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = AppColors.BrandOrange, checkedTrackColor = AppColors.BrandOrange.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = {
                    val ingredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (ingredients.isNotEmpty()) {
                        onGenerate(ingredients, preferencesText.takeIf { it.isNotBlank() }, selectedLanguage, includeShoppingList)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ingredientsText.isNotBlank() && !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandOrange),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Generate Recipe ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
