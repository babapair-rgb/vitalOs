package com.vitalos.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme
import kotlin.math.min

data class RingSpec(val progress: Float, val color: Color, val strokeWidth: Dp)

@Composable
fun ConcentricRingGauge(
    rings: List<RingSpec>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(176.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(176.dp)) {
            val maxStroke = rings.maxOfOrNull { it.strokeWidth.toPx() } ?: 0f
            val baseRadius = (min(size.width, size.height) - maxStroke) / 2f

            rings.forEachIndexed { index, ring ->
                val radius = baseRadius - index * (baseRadius * 0.32f / rings.size.coerceAtLeast(1))
                val strokePx = ring.strokeWidth.toPx()
                val trackStyle = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

                drawCircle(
                    color = VitalOsTheme.colors.surfaceContainerHigh,
                    radius = radius,
                    style = trackStyle,
                )
                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
        }
        centerContent()
    }
}
