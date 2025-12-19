package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.omiri.R
import com.example.omiri.ui.theme.Spacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.material.icons.filled.MoreVert

@Composable
fun OmiriHeader(
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    customAction: (@Composable () -> Unit)? = null,
    dropdownContent: (@Composable ColumnScope.(onDismiss: () -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.ic_omiri_logo),
            contentDescription = "Omiri Logo",
            modifier = Modifier.height(28.dp),
            tint = Color.Unspecified
        )
    }
) {
    var showMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.xxs))

            // Top bar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Start Content (Logo or Custom)
                startContent()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customAction != null) {
                        customAction()
                        Spacer(modifier = Modifier.width(Spacing.sm))
                    }

                    // Notification Button
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp, // Subtle shadow
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .clickable(onClick = onNotificationClick)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 4.dp, y = (-2).dp)
                                    .size(18.dp)
                                    .background(Color(0xFFFE8357), androidx.compose.foundation.shape.CircleShape)
                                    .border(1.5.dp, Color.White, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = notificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp, 
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Profile Button
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable(onClick = onProfileClick)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Overflow Menu
                    if (dropdownContent != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                             IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color(0xFF1F2937)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color.White),
                                content = {
                                    dropdownContent { showMenu = false }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxs))
        }
    }
}
