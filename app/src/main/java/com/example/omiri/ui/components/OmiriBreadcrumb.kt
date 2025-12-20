package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors

data class OmiriBreadcrumbItem(
    val label: String,
    val onClick: () -> Unit,
    val isLast: Boolean = false
)

@Composable
fun OmiriBreadcrumb(
    items: List<OmiriBreadcrumbItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isLast) FontWeight.Bold else FontWeight.Medium,
                color = if (item.isLast) AppColors.Neutral900 else AppColors.BrandOrange,
                modifier = Modifier.clickable(!item.isLast) { item.onClick() }
            )
            
            if (!item.isLast) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.Neutral400,
                    modifier = Modifier.size(20.dp).padding(horizontal = 8.dp)
                )
            }
        }
    }
}
