package ru.workinprogress.feature.main.ui

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.chart.ui.model.ChartUi

class SimpleMainTest {
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

}