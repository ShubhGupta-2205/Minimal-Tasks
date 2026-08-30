package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor

@Composable
fun AddTaskBar(
    onAddTask: (String) -> Unit,
    onOpenCreateMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current
    var text by remember { mutableStateOf("") }

    val barBg = if (theme.isMonochrome) Color(0xFF141414) else theme.taskCardColor
    val textColor = getContrastTextColor(barBg)
    val placeholderColor = getContrastSecondaryTextColor(barBg)
    val accentColor = theme.accentColor

    fun submit() {
        if (text.isNotBlank()) {
            onAddTask(text.trim())
            text = ""
        } else {
            onOpenCreateMenu()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = theme.cardCornerRadius.coerceAtLeast(16.dp),
                borderWidth = theme.cardBorderWidth,
                backgroundColor = barBg,
                borderColor = theme.cardBorderColor,
                glowColor = theme.cardGlowColor,
                glowRadius = if (theme.isMonochrome) 0.dp else 10.dp
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search and Add",
            tint = accentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .testTag("add_task_input"),
            textStyle = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(accentColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "Add task or tap + for alarms & habits...",
                        color = placeholderColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = accentColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = { submit() })
                .testTag("add_task_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add or Create Options",
                tint = getContrastTextColor(accentColor),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


