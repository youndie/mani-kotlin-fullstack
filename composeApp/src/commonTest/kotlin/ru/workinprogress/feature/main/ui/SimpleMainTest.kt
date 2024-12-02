package ru.workinprogress.feature.main.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import ru.workinprogress.feature.chart.ui.ChartComponent
import ru.workinprogress.feature.chart.ui.model.ChartUi
import kotlin.test.Test

class SimpleMainTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainLayoutTest() {
        runComposeUiTest {
            setContent {
                MainContent {
                    ChartComponent(ChartUi(days = persistentMapOf(LocalDate(2000, 1, 1) to 0.0)))
                }
            }

            onNodeWithTag("chartBox").isDisplayed()
            onNodeWithTag("futureInfo").isDisplayed()
            onNodeWithTag("filters").isDisplayed()
            onNodeWithTag("transactions").isDisplayed()
        }
    }
}