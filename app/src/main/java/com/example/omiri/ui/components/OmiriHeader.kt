package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.omiri.R
import com.example.omiri.ui.theme.Spacing

@Composable
fun OmiriHeader(
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    customAction: (@Composable () -> Unit)? = null,
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

                    IconButton(onClick = onNotificationClick) {
                        if (notificationCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = notificationCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF3F4F6), androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color(0xFF1F2937),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF3F4F6), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    IconButton(onClick = onProfileClick) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF3F4F6), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxs))
        }
    }
}
