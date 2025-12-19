package com.example.omiri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatActionBottomSheet(
    onDismiss: () -> Unit,
    onActionClick: (String) -> Unit
) {
    // Standardize divider color
    val dividerColor = Color(0xFFF3F4F6)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Bottom padding for navigation bar
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = dividerColor)

            QuickActionsGrid(
                dividerColor = dividerColor,
                onActionClick = { action ->
                    onActionClick(action)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    dividerColor: Color,
    onActionClick: (String) -> Unit
) {
    Column {
        // First Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionItem(
                icon = Icons.AutoMirrored.Outlined.List,
                title = "Shopping List",
                color = Color(0xFF2563EB),
                bgColor = Color(0xFFDBEAFE),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Shopping List") }
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp), thickness = 1.dp, color = dividerColor)
            QuickActionItem(
                icon = Icons.Outlined.Map,
                title = "Best Route",
                color = Color(0xFF9333EA),
                bgColor = Color(0xFFF3E8FF),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Best Route") }
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp), thickness = 1.dp, color = dividerColor)
            QuickActionItem(
                icon = Icons.Outlined.Percent,
                title = "Discounts",
                color = Color(0xFFEA580C),
                bgColor = Color(0xFFFFEDD5),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Discounts") }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = dividerColor)

        // Second Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionItem(
                icon = Icons.Outlined.RestaurantMenu,
                title = "Recipes",
                color = Color(0xFF16A34A),
                bgColor = Color(0xFFDCFCE7),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Recipes") }
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp), thickness = 1.dp, color = dividerColor)
            QuickActionItem(
                icon = Icons.Outlined.Storefront,
                title = "Find Stores",
                color = Color(0xFFDC2626),
                bgColor = Color(0xFFFEE2E2),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Find Stores") }
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp), thickness = 1.dp, color = dividerColor)
            QuickActionItem(
                icon = Icons.Outlined.AttachMoney,
                title = "Save Money",
                color = Color(0xFFD97706),
                bgColor = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("Save Money") }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                fontSize = 13.sp
            )
        }
    }
}
