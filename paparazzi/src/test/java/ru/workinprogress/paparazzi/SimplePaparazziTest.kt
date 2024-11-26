package ru.workinprogress.paparazzi

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import ru.workinprogress.feature.transaction.ui.component.TransactionItem
import ru.workinprogress.feature.transaction.ui.model.TransactionUiItem
import ru.workinprogress.mani.theme.darkScheme
import ru.workinprogress.mani.theme.lightScheme

@Composable
private fun AppTheme(
    darkTheme: Boolean,
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        content = content
    )
}


class SimplePaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    private val transaction = TransactionUiItem.Empty.copy(amount = 1000.0, comment = "No comment")

    @Test
    fun TransactionItemSnapshot() {
        paparazzi.snapshot {
            AppTheme(darkTheme = false) {
                TransactionItem(
                    transaction = transaction,
                    periodText = "One shot"
                )
            }
        }
    }

    @Test
    fun TransactionItemSnapshotDark() {
        paparazzi.snapshot {
            AppTheme(darkTheme = true) {
                TransactionItem(
                    transaction = transaction,
                    periodText = "One shot"
                )
            }
        }
    }
}