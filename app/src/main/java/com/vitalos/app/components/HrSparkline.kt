package com.vitalos.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun HrSparkline(
    samples: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = VitalOsTheme.colors.readinessLime,
) {
    if (samples.size < 2) return
    val minValue = samples.min().toFloat()
    val maxValue = samples.max().toFloat().coerceAtLeast(minValue + 1f)

    Canvas(modifier = modifier.fillMaxWidth().height(64.dp)) {
        val stepX = size.width / (samples.size - 1)
        fun yFor(value: Int): Float {
            val normalized = (value - minValue) / (maxValue - minValue)
            return size.height - (normalized * size.height)
        }

        val linePath = Path().apply {
            moveTo(0f, yFor(samples[0]))
            samples.forEachIndexed { index, value ->
                if (index > 0) lineTo(index * stepX, yFor(value))
            }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(path = fillPath, color = lineColor.copy(alpha = 0.18f), style = Fill)
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 4f))
        drawCircle(color = lineColor, radius = 6f, center = Offset(size.width, yFor(samples.last())))
    }
}
