package ru.workinprogress.feature.main.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.chart.ui.model.ChartUi
import ru.workinprogress.feature.transaction.Category
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FiltersTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun mainLayoutTest() {
        rule.setContent {
            MainContent {
                ChartComponent(ChartUi(days = persistentMapOf(LocalDate(2000, 1, 1) to 0.0)))
            }
        }

        rule.onNodeWithTag("chartBox").isDisplayed()
        rule.onNodeWithTag("futureInfo").isDisplayed()
        rule.onNodeWithTag("filters").isDisplayed()
        rule.onNodeWithTag("transactions").isDisplayed()
    }

    @Test
    fun filterChipsTest() {
        val targetCategory = Category("1", "Test1")
        val stateFlow = MutableStateFlow(
            FiltersState(
                categories = persistentSetOf(Category("0", "Test0"), targetCategory),
                loading = true
            )
        )

        rule.setContent {
            val state = stateFlow.collectAsState()

            FiltersChips(
                filtersState = state.value,
                modifier = Modifier,
                onUpcomingToggle = {
                    stateFlow.update { state ->
                        state.copy(upcoming = it)
                    }
                }) {
                stateFlow.update { state ->
                    state.copy(category = it)
                }
            }
        }

        rule.onNodeWithTag("filtersShimmer").isDisplayed()

        stateFlow.update { state ->
            state.copy(loading = false)
        }

        rule.onNodeWithText("Upcoming").isDisplayed()
        rule.onNodeWithText("All categories").isDisplayed()

        rule.onNodeWithText("Upcoming").performClick()
        rule.onNodeWithText("Past").isDisplayed()

        rule.onNodeWithText("Past").performClick()
        assertFalse(stateFlow.value.upcoming)

        rule.onNodeWithText("All categories").performClick()
        rule.onNodeWithText(targetCategory.name).isDisplayed()
        rule.onNodeWithText(targetCategory.name).performClick()
        assertEquals(targetCategory, stateFlow.value.category)
    }
}