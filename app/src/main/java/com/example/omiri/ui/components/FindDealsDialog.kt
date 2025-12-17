package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.omiri.data.api.models.ProductResponse
import com.example.omiri.ui.theme.AppColors

@Composable
fun FindDealsDialog(
    initialQuery: String,
    onDismissRequest: () -> Unit,
    onSearch: (String) -> Unit,
    isLoading: Boolean,
    results: List<ProductResponse>,
    onSelectProduct: (ProductResponse) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = AppColors.Surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Find Deals",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search products...") },
                    trailingIcon = {
                        IconButton(onClick = { onSearch(query) }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BrandOrange,
                        cursorColor = AppColors.BrandOrange
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Results
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.BrandOrange)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(results) { product ->
                            ListItem(
                                headlineContent = { Text(product.title) },
                                supportingContent = { 
                                    Row {
                                       Text(
                                           text = "${product.retailer}",
                                           style = MaterialTheme.typography.bodySmall,
                                           color = AppColors.Neutral500
                                       )
                                       Spacer(Modifier.width(4.dp))
                                       Text(
                                            text = "• €${product.priceAmount}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.BrandOrange
                                       )
                                    }
                                },
                                modifier = Modifier.clickable { onSelectProduct(product) }
                            )
                            HorizontalDivider(color = AppColors.Neutral200)
                        }
                        if (results.isEmpty()) {
                            item {
                                Text("No results found", modifier = Modifier.padding(16.dp), color = AppColors.Neutral500)
                            }
                        }
                    }
                }
            }
        }
    }
}
