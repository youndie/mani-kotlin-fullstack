package ru.workinprogress.feature.transaction.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDatePicker(
    modifier: Modifier = Modifier,
    label: String,
    value: String?,
    datePickerState: DatePickerState,
    showDialog: Boolean,
    onToggleDatePicker: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {

    OutlinedTextField(
        value = value.orEmpty(),
        onValueChange = {

        },
        singleLine = true,
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            onToggleDatePicker()
                        }
                    }
                }
            },
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = onToggleDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis
                        ?.toDate
                        ?.let {
                            onDateSelected(it)
                        }
                }) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = onToggleDatePicker) {
                    Text("Cancel")
                }
            }) {
            DatePicker(state = datePickerState)
        }
    }
}