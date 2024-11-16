package ru.workinprogress.feature.chart.ui

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.*
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RowScope.ChartImpl(values: ImmutableList<Double>, labels: ImmutableList<String>) {
    val color = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    val data = remember(values) {
        listOf(
            Line(
                label = "Transactions",
                values = values,
                color = SolidColor(color),
                firstGradientFillColor = color.copy(alpha = .5f),
                secondGradientFillColor = Color.Transparent,
                strokeAnimationSpec = tween(1200, easing = EaseInOutCubic),
                gradientAnimationDelay = 600,
                drawStyle = DrawStyle.Stroke(2.dp),
                curvedEdges = false,
            ),
        )
    }

    Card(
        modifier = Modifier.height(270.dp)
            .fillMaxWidth()
            .weight(1f)
            .border(2.dp, Color.Transparent, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp, horizontal = 12.dp)) {
            LineChart(
                modifier = Modifier
                    .fillMaxSize(),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 300L
                }),
                zeroLineProperties = ZeroLineProperties(
                    enabled = true,
                    color = SolidColor(secondary),
                ),
                dividerProperties = DividerProperties(enabled = false),
                gridProperties = GridProperties(
                    xAxisProperties = GridProperties.AxisProperties(
                        thickness = .2.dp,
                        color = SolidColor(color.copy(alpha = .3f)),
                        style = StrokeStyle.Dashed(intervals = floatArrayOf(15f, 15f), phase = 10f),
                    ),
                    yAxisProperties = GridProperties.AxisProperties(
                        thickness = .2.dp,
                        color = SolidColor(color.copy(alpha = .2f)),
                        style = StrokeStyle.Dashed(intervals = floatArrayOf(15f, 15f), phase = 10f),
                    ),
                ),
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = labels,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                ),
                popupProperties = PopupProperties(
                    textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface),
                    contentBuilder = {
                        it.format(0)
                    },
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    contentBuilder = {
                        it.format(0)
                    },
                    count = IndicatorCount.CountBased(3),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                ),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                curvedEdges = false
            )
        }
    }
}
