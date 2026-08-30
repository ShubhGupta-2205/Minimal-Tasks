package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueOrbGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanNeonGlow
import com.example.ui.theme.CyanOrbGlow
import com.example.ui.theme.FrostedBackground
import com.example.ui.theme.VoidBlack

/**
 * Immersive UI Ambient Orbital Glow Canvas
 * Only active in Cyberpunk mode, pure black in Monochrome
 */
@Composable
fun ImmersiveAmbientBackground(
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current
    if (theme.isMonochrome) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
        )
    } else if (theme.isCustom) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(theme.backgroundColor)
        )
    } else {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            val w = size.width
            val h = size.height

            // Top-left cyan orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CyanNeon.copy(alpha = 0.22f),
                        CyanOrbGlow,
                        Color.Transparent
                    ),
                    center = Offset(w * 0.1f, h * 0.05f),
                    radius = w * 0.7f
                ),
                center = Offset(w * 0.1f, h * 0.05f),
                radius = w * 0.7f
            )

            // Bottom-right deep indigo/blue orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1565C0).copy(alpha = 0.35f),
                        BlueOrbGlow,
                        Color.Transparent
                    ),
                    center = Offset(w * 0.9f, h * 0.9f),
                    radius = w * 0.85f
                ),
                center = Offset(w * 0.9f, h * 0.9f),
                radius = w * 0.85f
            )
        }
    }
}

/**
 * Implements the themed frosted container
 */
@Composable
fun FrostedCyanBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.current.cardBgColor,
    borderColor: Color = AppTheme.current.cardBorderColor,
    glowColor: Color = AppTheme.current.cardGlowColor,
    cornerRadius: Dp = AppTheme.current.cardCornerRadius,
    borderWidth: Dp = AppTheme.current.cardBorderWidth,
    padding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .drawBehind {
                if (glowColor != Color.Transparent && glowColor.alpha > 0f) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            this.color = glowColor.toArgb()
                            this.setShadowLayer(
                                10.dp.toPx(),
                                0f,
                                0f,
                                glowColor.toArgb()
                            )
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                            cornerRadius.toPx(),
                            cornerRadius.toPx(),
                            paint
                        )
                    }
                }
            }
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
            .padding(padding),
        content = content
    )
}

/**
 * Modifier extension to apply frosted style with glow
 */
fun Modifier.frostedCyanStyle(
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = FrostedBackground,
    borderColor: Color = CyanNeon,
    glowColor: Color = CyanNeonGlow,
    glowRadius: Dp = 10.dp
): Modifier = this
    .drawBehind {
        if (glowColor != Color.Transparent && glowColor.alpha > 0f && glowRadius > 0.dp) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = glowColor.toArgb()
                    this.setShadowLayer(
                        glowRadius.toPx(),
                        0f,
                        0f,
                        glowColor.toArgb()
                    )
                }
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    cornerRadius.toPx(),
                    cornerRadius.toPx(),
                    paint
                )
            }
        }
    }
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))


