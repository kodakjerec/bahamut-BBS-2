package com.kota.Bahamut.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 浮動工具列按鈕資料
 */
data class FloatingToolbarButtonItem(
    val text: String,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null
)

/**
 * 浮動可拖曳工具列 (對應 propertiesToolbarLocation == 3)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BahaFloatingToolbar(
    buttons: List<FloatingToolbarButtonItem>,
    toolbarOrder: Int = 0,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    val toolbarWidthDp = 72.dp
    val toolbarWidthPx = with(density) { toolbarWidthDp.toPx() }
    var toolbarHeightPx by remember { mutableFloatStateOf(with(density) { 180.dp.toPx() }) }

    // 取得歷史儲存座標，若未儲存則預設放置於畫面右側中央
    val savedLoc = remember { UserSettings.floatingLocation }
    var offsetX by remember {
        mutableFloatStateOf(
            if (savedLoc.size >= 2 && savedLoc[0] != null && savedLoc[0]!! >= 0f) {
                savedLoc[0]!!.coerceIn(0f, (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f))
            } else {
                (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f)
            }
        )
    }
    var offsetY by remember {
        mutableFloatStateOf(
            if (savedLoc.size >= 2 && savedLoc[1] != null && savedLoc[1]!! >= 0f) {
                savedLoc[1]!!.coerceIn(0f, (screenHeightPx - toolbarHeightPx).coerceAtLeast(0f))
            } else {
                (screenHeightPx / 2f - toolbarHeightPx / 2f).coerceAtLeast(0f)
            }
        )
    }

    // 閒置透明度控制
    val idleAlphaTarget = (UserSettings.toolbarAlpha / 100f).coerceIn(0.1f, 1f)
    var isIdle by remember { mutableStateOf(TempSettings.isFloatingInvisible) }
    val currentAlpha by animateFloatAsState(
        targetValue = if (isIdle) idleAlphaTarget else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "BahaFloatingToolbarAlpha"
    )

    var idleTimerJob by remember { mutableStateOf<Job?>(null) }

    fun startIdleTimer() {
        idleTimerJob?.cancel()
        val idleSeconds = UserSettings.toolbarIdle
        if (idleSeconds > 0f) {
            idleTimerJob = coroutineScope.launch {
                delay((idleSeconds * 1000L).toLong())
                isIdle = true
                TempSettings.isFloatingInvisible = true
            }
        }
    }

    fun resetIdle() {
        idleTimerJob?.cancel()
        if (isIdle) {
            isIdle = false
            TempSettings.isFloatingInvisible = false
        }
        startIdleTimer()
    }

    LaunchedEffect(Unit) {
        if (!isIdle) {
            startIdleTimer()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            idleTimerJob?.cancel()
        }
    }

    // 螢幕旋轉或大小變動時確保在螢幕邊界內
    LaunchedEffect(screenWidthPx, screenHeightPx) {
        val maxX = (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f)
        val maxY = (screenHeightPx - toolbarHeightPx).coerceAtLeast(0f)
        offsetX = if (offsetX > screenWidthPx / 2f) maxX else 0f
        offsetY = offsetY.coerceIn(0f, maxY)
    }

    val orderedButtons = if (toolbarOrder == 1) buttons.reversed() else buttons

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .alpha(currentAlpha)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(colors.toolbarBackground)
            .border(1.dp, colors.toolbarDivider, RoundedCornerShape(8.dp))
            .width(toolbarWidthDp)
            .onSizeChanged {
                toolbarHeightPx = it.height.toFloat()
            }
            .pointerInput(screenWidthPx, screenHeightPx, toolbarWidthPx) {
                detectDragGestures(
                    onDragStart = {
                        resetIdle()
                    },
                    onDragEnd = {
                        // 放開後吸附至左右側邊緣
                        val maxX = (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f)
                        offsetX = if (offsetX > screenWidthPx / 2f) maxX else 0f
                        UserSettings.setFloatingLocation(offsetX, offsetY)
                        startIdleTimer()
                    },
                    onDragCancel = {
                        val maxX = (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f)
                        offsetX = if (offsetX > screenWidthPx / 2f) maxX else 0f
                        UserSettings.setFloatingLocation(offsetX, offsetY)
                        startIdleTimer()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        resetIdle()
                        val maxX = (screenWidthPx - toolbarWidthPx).coerceAtLeast(0f)
                        val maxY = (screenHeightPx - toolbarHeightPx).coerceAtLeast(0f)
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 頂部拖曳把手提示條
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(colors.toolbarDivider.copy(alpha = 0.8f))
                )
            }

            // 按鈕列表
            orderedButtons.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.toolbarDivider)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .combinedClickable(
                            onClick = {
                                resetIdle()
                                item.onClick()
                            },
                            onLongClick = {
                                resetIdle()
                                item.onLongClick?.invoke()
                            }
                        )
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = item.text,
                        color = colors.buttonText,
                        fontSize = BahaTextSize.TITLE,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

