package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastTextColor
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageCropFitModal(
    sourceUri: Uri,
    title: String = "FRAME & CROP IMAGE",
    targetAspect: Float = 9f / 16f,
    storagePrefix: String = "app_bg",
    onDismiss: () -> Unit,
    onCropCompleted: (Uri) -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val coroutineScope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Transform state (Pinch zoom & pan offset)
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Measured viewport dimensions in pixels
    var viewportWidthPx by remember { mutableFloatStateOf(0f) }
    var viewportHeightPx by remember { mutableFloatStateOf(0f) }

    // Load bitmap on launch
    LaunchedEffect(sourceUri) {
        isLoading = true
        val bmp = ImageStorageHelper.decodeBitmapFromUri(context, sourceUri)
        loadedBitmap = bmp
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .frostedCyanStyle(
                        cornerRadius = 20.dp,
                        borderWidth = theme.cardBorderWidth,
                        backgroundColor = theme.dialogBgColor,
                        borderColor = theme.dialogBorderColor,
                        glowColor = theme.dialogGlowColor,
                        glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = "Crop",
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = title,
                                color = theme.primaryTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "WHOLE IMAGE IMPORTED • PINCH TO ZOOM",
                                color = theme.secondaryTextColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CENTER CROP PREVIEW AREA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading || loadedBitmap == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = theme.accentColor,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Importing Full Image...",
                                color = theme.secondaryTextColor,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        val bitmap = loadedBitmap!!

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(targetAspect, matchHeightConstraintsFirst = true)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, theme.accentColor, RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(0.6f, 6f)
                                        scale = newScale
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                }
                                .clipToBounds(),
                            contentAlignment = Alignment.Center
                        ) {
                            viewportWidthPx = constraints.maxWidth.toFloat()
                            viewportHeightPx = constraints.maxHeight.toFloat()

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Cropping Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                            )

                            CropGridOverlay(gridColor = theme.accentColor.copy(alpha = 0.35f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GESTURE INSTRUCTION & RESET BAR
                val infoBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 12.dp,
                            borderWidth = 1.dp,
                            backgroundColor = infoBg,
                            borderColor = theme.cardBorderColor.copy(alpha = 0.35f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🤏 Pinch with 2 fingers to zoom • 👆 Drag to frame",
                            color = theme.primaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Reset button
                    Box(
                        modifier = Modifier
                            .background(infoBg, RoundedCornerShape(10.dp))
                            .border(1.dp, theme.accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = theme.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RESET",
                                color = theme.accentColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTTOM ACTION BUTTONS (Cancel and Apply)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val cancelBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(cancelBg, RoundedCornerShape(14.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CANCEL",
                            color = theme.secondaryTextColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(46.dp)
                            .background(theme.accentColor, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isSaving && loadedBitmap != null) {
                                val bitmap = loadedBitmap ?: return@clickable
                                isSaving = true
                                coroutineScope.launch {
                                    val cropped = renderCroppedBitmap(
                                        sourceBitmap = bitmap,
                                        targetAspect = targetAspect,
                                        scale = scale,
                                        panX = offsetX,
                                        panY = offsetY,
                                        viewW = if (viewportWidthPx > 0f) viewportWidthPx else 1080f,
                                        viewH = if (viewportHeightPx > 0f) viewportHeightPx else 1920f
                                    )
                                    val savedUri = ImageStorageHelper.saveCroppedBitmap(
                                        context = context,
                                        bitmap = cropped,
                                        prefix = storagePrefix
                                    )
                                    isSaving = false
                                    if (savedUri != null) {
                                        onCropCompleted(savedUri)
                                    }
                                }
                            }
                            .testTag("apply_crop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = getContrastTextColor(theme.accentColor),
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply",
                                    tint = getContrastTextColor(theme.accentColor),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APPLY CROPPED IMAGE",
                                    color = getContrastTextColor(theme.accentColor),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders rule-of-thirds grid lines and corner cyber brackets over crop viewport.
 */
@Composable
private fun CropGridOverlay(gridColor: Color = Color(0x5500FFFF)) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val strokeW = 1.dp.toPx()

        // Vertical 3x3 rule-of-thirds lines
        drawLine(
            color = gridColor,
            start = Offset(w / 3f, 0f),
            end = Offset(w / 3f, h),
            strokeWidth = strokeW
        )
        drawLine(
            color = gridColor,
            start = Offset(2 * w / 3f, 0f),
            end = Offset(2 * w / 3f, h),
            strokeWidth = strokeW
        )

        // Horizontal 3x3 rule-of-thirds lines
        drawLine(
            color = gridColor,
            start = Offset(0f, h / 3f),
            end = Offset(w, h / 3f),
            strokeWidth = strokeW
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, 2 * h / 3f),
            end = Offset(w, 2 * h / 3f),
            strokeWidth = strokeW
        )
    }
}

/**
 * Renders the exact framed bitmap onto a high-resolution target aspect canvas.
 * Preserves the whole image fitted inside or zoomed/panned precisely as on screen.
 */
private fun renderCroppedBitmap(
    sourceBitmap: Bitmap,
    targetAspect: Float,
    scale: Float,
    panX: Float,
    panY: Float,
    viewW: Float,
    viewH: Float
): Bitmap {
    // Target canvas dimensions (standard high-res 1080x1920 or proportional)
    val outH = 1920
    val outW = (outH * targetAspect).toInt().coerceAtLeast(720)

    val output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(AndroidColor.BLACK)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    // Calculate ContentScale.Fit placement inside target viewport
    val srcW = sourceBitmap.width.toFloat()
    val srcH = sourceBitmap.height.toFloat()
    val srcAspect = srcW / srcH

    val fitScale: Float
    val baseW: Float
    val baseH: Float

    if (srcAspect > targetAspect) {
        // Image is wider than target aspect
        baseW = outW.toFloat()
        baseH = outW / srcAspect
        fitScale = outW / srcW
    } else {
        // Image is taller than or equal to target aspect
        baseH = outH.toFloat()
        baseW = outH * srcAspect
        fitScale = outH / srcH
    }

    val baseX = (outW - baseW) / 2f
    val baseY = (outH - baseH) / 2f

    val matrix = Matrix()
    // 1. Initial fit scaling and centering
    matrix.postScale(fitScale, fitScale)
    matrix.postTranslate(baseX, baseY)

    // 2. User zoom centered on viewport
    matrix.postScale(scale, scale, outW / 2f, outH / 2f)

    // 3. User pan translation (proportional to viewport size)
    val panScaleFactor = if (viewW > 0f) outW / viewW else 1f
    matrix.postTranslate(panX * panScaleFactor, panY * panScaleFactor)

    canvas.drawBitmap(sourceBitmap, matrix, paint)
    return output
}
