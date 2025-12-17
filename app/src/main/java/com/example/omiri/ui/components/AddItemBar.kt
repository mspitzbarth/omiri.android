package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors

@Composable
fun AddItemBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Input Field
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    "Add item to list...", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Neutral400
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = AppColors.Neutral400
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.Neutral50, // Very light gray from design
                unfocusedContainerColor = AppColors.Neutral50,
                disabledContainerColor = AppColors.Neutral50,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(50.dp),
            singleLine = true
        )
        
        // Add Button
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandOrange),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        }
    }
}
