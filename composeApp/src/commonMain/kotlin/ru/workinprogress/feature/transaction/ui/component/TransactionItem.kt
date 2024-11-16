package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: TransactionUiItem,
    selected: Boolean,
    selectionMode: Boolean,
    onItemSelected: (TransactionUiItem) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        if (selected) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            ListItemDefaults.containerColor
        }
    )

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectionMode) {
                        onItemSelected(transaction)
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemSelected(transaction)
                },
                onLongClickLabel = "Long Click Label",
            ),
        colors = ListItemDefaults.colors(containerColor = containerColor,),
        supportingContent = { Text("${transaction.period}") },
        trailingContent = {
            if (transaction.income) {
                Text(transaction.amount.roundToInt().toString())
            } else {
                Text((transaction.amount.roundToInt() * -1).toString())
            }
        },
        headlineContent = { Text(transaction.comment) })
}

