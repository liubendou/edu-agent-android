package com.eduagent.core.design.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.R
import com.eduagent.core.design.atom.ChipToggle
import com.eduagent.core.design.atom.MasteryRing
import com.eduagent.core.design.atom.Metric
import com.eduagent.core.design.atom.ModeChipRow
import com.eduagent.core.design.atom.PressScaleButton
import com.eduagent.core.design.atom.ReviewBreathDot
import com.eduagent.core.design.atom.ThreeDotLoading
import com.eduagent.core.design.atom.ThinkingPlaceholder
import com.eduagent.core.design.atom.TopToast
import com.eduagent.core.design.haptics.GatedHaptics
import com.eduagent.core.design.haptics.HapticKind
import com.eduagent.core.design.haptics.LocalHaptics
import com.eduagent.core.design.haptics.rememberAndroidHaptics
import com.eduagent.core.design.motion.CorrectStroke
import com.eduagent.core.design.motion.DeviceTier
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.LocalMotionState
import com.eduagent.core.design.motion.MotionLevel
import com.eduagent.core.design.motion.rememberSystemAnimationScaleIsZero
import com.eduagent.core.design.motion.wrongShake
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens
import kotlinx.coroutines.delay

/**
 * 动效演示页（契约 §12 V1 / V2；任务包 05 §7-E）。
 *
 * 逐字接口：`@Composable fun MotionGalleryScreen(onBack: () -> Unit)`，仅 debug 变体由 `:app` 挂载
 * （第 10 步接入路由，release 不含）。
 *
 * 页面职责：
 * 1. 列出 10 条配方，每条可**手动触发**（V1：动效有可见终态）；
 * 2. 提供 full / reduced / minimal 三档切换（V1：切换即生效）；
 * 3. 系统「移除动画」时**自动进入 minimal 且隐藏切档入口**（§8.3 / D27：不给反向覆盖入口）。
 */
@Composable
fun MotionGalleryScreen(onBack: () -> Unit) {
    val systemScaleIsZero = rememberSystemAnimationScaleIsZero()
    var override by remember { mutableStateOf<MotionLevel?>(null) }
    val state = MotionLevel.resolve(
        systemScaleIsZero = systemScaleIsZero,
        userOverride = override,
        deviceTier = DeviceTier.STANDARD,
    )
    var hapticsEnabled by remember { mutableStateOf(true) }
    val deviceHaptics = rememberAndroidHaptics(enabled = true)
    val haptics = remember(deviceHaptics, hapticsEnabled) { GatedHaptics(deviceHaptics, hapticsEnabled) }

    EduTheme {
        CompositionLocalProvider(
            LocalMotionLevel provides state.level,
            LocalMotionState provides state,
            LocalHaptics provides haptics,
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = ColorTokens.Canvas) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PressScaleButton(onClick = onBack) {
                            Text(
                                text = stringResource(R.string.action_back),
                                style = MaterialTheme.typography.labelLarge,
                                color = ColorTokens.OnAccent,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.motion_gallery_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = ColorTokens.Ink,
                        )
                    }

                    Text(
                        text = stringResource(R.string.motion_gallery_section_level),
                        style = MaterialTheme.typography.labelLarge,
                        color = ColorTokens.InkMuted,
                    )
                    if (state.canUserOverride) {
                        ModeChipRow(
                            chips = listOf(
                                stringResource(R.string.motion_level_full),
                                stringResource(R.string.motion_level_reduced),
                                stringResource(R.string.motion_level_minimal),
                            ),
                            selectedIndex = (override ?: state.level).ordinal,
                            onSelect = { index ->
                                override = MotionLevel.entries.getOrNull(index)
                                haptics.perform(HapticKind.CONTEXT_CLICK)
                            },
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.motion_gallery_locked),
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorTokens.Warning,
                        )
                    }

                    Text(
                        text = stringResource(R.string.motion_gallery_section_haptics),
                        style = MaterialTheme.typography.labelLarge,
                        color = ColorTokens.InkMuted,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ChipToggle(
                            label = stringResource(R.string.haptics_on),
                            selected = hapticsEnabled,
                            onToggle = { hapticsEnabled = true },
                        )
                        ChipToggle(
                            label = stringResource(R.string.haptics_off),
                            selected = !hapticsEnabled,
                            onToggle = { hapticsEnabled = false },
                        )
                    }

                    Text(
                        text = stringResource(R.string.motion_gallery_section_recipes),
                        style = MaterialTheme.typography.labelLarge,
                        color = ColorTokens.InkMuted,
                    )

                    Recipe01PressRebound()
                    Recipe02ModeIndicator()
                    Recipe03ChipBounce()
                    Recipe04ThreeDot()
                    Recipe05WrongShake()
                    Recipe06CorrectStroke()
                    Recipe07Thinking()
                    Recipe08TopToast()
                    Recipe09ReviewBreath()
                    Recipe10MasteryGrowth()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 演示卡片容器
// ---------------------------------------------------------------------------

@Composable
private fun RecipeCard(titleRes: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.Surface)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.Ink,
        )
        content()
    }
}

@Composable
private fun InkLabel(text: String, color: Color = ColorTokens.Ink) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    PressScaleButton(onClick = onClick, enabled = enabled) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.OnAccent,
        )
    }
}

// ---------------------------------------------------------------------------
// 1 按压回弹（反馈闭环：点击次数可见）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe01PressRebound() {
    val haptics = LocalHaptics.current
    var clicks by remember { mutableIntStateOf(0) }
    RecipeCard(R.string.recipe_01_press_rebound) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionButton(
                label = stringResource(R.string.motion_gallery_trigger),
                onClick = {
                    clicks += 1
                    haptics.perform(HapticKind.CONTEXT_CLICK)
                },
            )
            InkLabel(stringResource(R.string.gallery_clicks_prefix) + " " + clicks)
        }
    }
}

// ---------------------------------------------------------------------------
// 2 模式指示滑动（五档切换：空间连续）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe02ModeIndicator() {
    val haptics = LocalHaptics.current
    var index by remember { mutableIntStateOf(0) }
    RecipeCard(R.string.recipe_02_mode_indicator) {
        ModeChipRow(
            chips = listOf(
                stringResource(R.string.mode_quick),
                stringResource(R.string.mode_standard),
                stringResource(R.string.mode_deep),
                stringResource(R.string.mode_review),
                stringResource(R.string.mode_boost),
            ),
            selectedIndex = index,
            onSelect = { selected ->
                index = selected
                haptics.perform(HapticKind.CONTEXT_CLICK)
            },
        )
    }
}

// ---------------------------------------------------------------------------
// 3 选项 chips 弹跳（题数 3/5/8）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe03ChipBounce() {
    val haptics = LocalHaptics.current
    var selected by remember { mutableIntStateOf(3) }
    RecipeCard(R.string.recipe_03_chip_bounce) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 5, 8).forEach { count ->
                ChipToggle(
                    label = count.toString(),
                    selected = selected == count,
                    onToggle = {
                        selected = count
                        haptics.perform(HapticKind.CONTEXT_CLICK)
                    },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4 提交态三点加载（按钮文案换「动作中」并禁用）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe04ThreeDot() {
    var loading by remember { mutableStateOf(false) }
    LaunchedEffect(loading) {
        if (loading) {
            delay(1_500L)
            loading = false
        }
    }
    RecipeCard(R.string.recipe_04_submit_three_dot) {
        PressScaleButton(onClick = { loading = true }, enabled = !loading) {
            if (loading) {
                ThreeDotLoading(
                    label = stringResource(R.string.action_in_progress),
                    tint = ColorTokens.OnAccent,
                )
            } else {
                Text(
                    text = stringResource(R.string.motion_gallery_trigger),
                    style = MaterialTheme.typography.labelLarge,
                    color = ColorTokens.OnAccent,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5 答错摇头（minimal 必须有红色文案 + 错号图标）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe05WrongShake() {
    val haptics = LocalHaptics.current
    var shakeKey by remember { mutableIntStateOf(0) }
    RecipeCard(R.string.recipe_05_wrong_shake) {
        Column(
            modifier = Modifier.wrongShake(shakeKey),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WrongMark()
                InkLabel(stringResource(R.string.answer_wrong), ColorTokens.Error)
            }
        }
        ActionButton(
            label = stringResource(R.string.motion_gallery_trigger),
            onClick = {
                shakeKey += 1
                haptics.perform(HapticKind.REJECT)
            },
        )
    }
}

/** 错号图标：Canvas 手绘（M6：不依赖外部图片资源）。 */
@Composable
private fun WrongMark(diameter: Dp = 18.dp, color: Color = ColorTokens.Error) {
    Canvas(modifier = Modifier.size(diameter)) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.2f),
            end = Offset(size.width * 0.8f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.8f, size.height * 0.2f),
            end = Offset(size.width * 0.2f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

// ---------------------------------------------------------------------------
// 6 答对描边（minimal 直接满描边静态显示）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe06CorrectStroke() {
    val haptics = LocalHaptics.current
    var playKey by remember { mutableIntStateOf(0) }
    RecipeCard(R.string.recipe_06_correct_stroke) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CorrectStroke(playKey = playKey, diameter = 32.dp)
            InkLabel(stringResource(R.string.answer_correct), ColorTokens.Success)
        }
        ActionButton(
            label = stringResource(R.string.motion_gallery_trigger),
            onClick = {
                playKey += 1
                haptics.perform(HapticKind.CONFIRM)
            },
        )
    }
}

// ---------------------------------------------------------------------------
// 7 等待思考（超 8s 必须明示「模型正在思考」）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe07Thinking() {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running) {
            delay(200L)
            elapsedMs += 200L
        }
    }
    RecipeCard(R.string.recipe_07_thinking) {
        ThinkingPlaceholder(elapsedMs = elapsedMs)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                label = stringResource(R.string.motion_gallery_trigger),
                onClick = {
                    elapsedMs = 0L
                    running = true
                },
            )
            ActionButton(
                label = stringResource(R.string.action_in_progress),
                onClick = {
                    running = false
                    elapsedMs = 0L
                },
                enabled = running,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 8 顶部轻提示（居中靠外层容器；3.2s 自动退场）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe08TopToast() {
    val haptics = LocalHaptics.current
    var visible by remember { mutableStateOf(false) }
    RecipeCard(R.string.recipe_08_top_toast) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TopToast(
                message = stringResource(R.string.toast_import_success),
                visible = visible,
                onDismiss = { visible = false },
            )
        }
        ActionButton(
            label = stringResource(R.string.motion_gallery_trigger),
            onClick = {
                visible = true
                haptics.perform(HapticKind.CONFIRM)
            },
        )
    }
}

// ---------------------------------------------------------------------------
// 9 待复习呼吸点（minimal 不渲染呼吸层，保留静态实心点）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe09ReviewBreath() {
    var dueCount by remember { mutableIntStateOf(1) }
    RecipeCard(R.string.recipe_09_review_breath) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReviewBreathDot(dueCount = dueCount)
            InkLabel(stringResource(R.string.gallery_due_count_prefix) + " " + dueCount)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipToggle(
                label = "1",
                selected = dueCount > 0,
                onToggle = { dueCount = 1 },
            )
            ChipToggle(
                label = "0",
                selected = dueCount == 0,
                onToggle = { dueCount = 0 },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 10 掌握度增长（minimal 直接跳到目标值）
// ---------------------------------------------------------------------------

@Composable
private fun Recipe10MasteryGrowth() {
    val values = remember { listOf(0.35f, 0.62f, 0.88f, 0.20f) }
    var index by remember { mutableIntStateOf(0) }
    RecipeCard(R.string.recipe_10_mastery_growth) {
        MasteryRing(value = values[index], metric = Metric.MASTERY)
        ActionButton(
            label = stringResource(R.string.motion_gallery_trigger),
            onClick = { index = (index + 1) % values.size },
        )
    }
}
