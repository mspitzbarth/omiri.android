package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var listName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Voice Search Launcher
    val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText: String? = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                listName = spokenText
            }
        }
    }

    // Auto-focus logic
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .padding(Spacing.lg)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Create New List",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("List Name (e.g., Weekly Groceries)") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak list name...")
                                    }
                                    try {
                                        voiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice input",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = MaterialTheme.shapes.large,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (listName.isNotBlank()) {
                                    onCreate(listName)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Button(
                        onClick = {
                            if (listName.isNotBlank()) {
                                onCreate(listName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA580B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = listName.isNotBlank()
                    ) {
                        Text(
                            text = "Create List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
