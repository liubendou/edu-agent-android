package com.eduagent.core.design.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 十条微交互配方（契约 §8.4，逐条对应 `assets/contract/motion-tokens.json` 的 `recipes`）。
 *
 * 本文件承担两件事：
 * 1. 机器可读的配方登记表 [Motion]（`MotionRecipe -> MotionSpec`），含 **minimal 静态表达计划**；
 * 2. 配方的**可复用实现**（配方 1 按压回弹 / 配方 5 答错摇头 / 配方 6 答对描边）。
 *
 * 纪律：
 * - 动效只写在本模块（红线 R-T10：模型不得输出任何动效参数）；
 * - 页面严禁裸写时长 / 曲线（§14.4），一律走 [MotionTokens] 或 [Motion.recipe]。
 * - minimal **不是什么都不发生**，而是用静态样式表达同一状态（文字 / 颜色 / 图标）。
 */
enum class MotionRecipe {
    /** 1 按压回弹。 */
    PRESS_REBOUND,

    /** 2 模式指示滑动。 */
    MODE_INDICATOR_SLIDE,

    /** 3 选项 chips 弹跳。 */
    CHIP_BOUNCE,

    /** 4 提交态三点加载。 */
    SUBMIT_THREE_DOT,

    /** 5 答错摇头。 */
    WRONG_SHAKE,

    /** 6 答对描边。 */
    CORRECT_STROKE,

    /** 7 等待思考。 */
    THINKING_WAIT,

    /** 8 顶部轻提示。 */
    TOP_TOAST,

    /** 9 待复习呼吸点。 */
    REVIEW_BREATH_DOT,

    /** 10 掌握度增长。 */
    MASTERY_GROWTH,
}

/** minimal 档下「信息不丢失」的静态载体（契约 §4.2 判定方法）。 */
enum class InfoCarrier {
    /** 文字（如「答错了」）。 */
    TEXT,

    /** 颜色（如红色）。 */
    COLOR,

    /** 图标（如错号）。 */
    ICON,

    /** 形状（如静态实心点）。 */
    SHAPE,

    /** 数值直显。 */
    VALUE,
}

/** 某配方的 minimal 静态表达计划。 */
data class MinimalPlan(
    /** minimal 档仍渲染（退化为静态），而非隐藏。 */
    val rendersStatic: Boolean,
    /** 静态状态下仍能分清「对 / 错 / 有 / 无」的载体；不得为空。 */
    val carriers: Set<InfoCarrier>,
    /** 人读说明（ASCII，遵守仓库文本纪律）。 */
    val note: String,
)

/** 单个配方的规格（供 gallery 与第 6 步消费）。 */
data class MotionSpec(
    val recipe: MotionRecipe,
    /** 主时长（ms）；0 表示由外部状态驱动、无固定时长。 */
    val durationMs: Int,
    /** 是否循环（reduced / minimal 一律不渲染循环层）。 */
    val looping: Boolean,
    /** 物理弹簧（仅位移 / 尺寸类配方有；透明度 / 颜色类走时长 + ease-out）。 */
    val spring: SpringSpec<Float>?,
    val minimal: MinimalPlan,
)

/**
 * 配方登记表：`fun recipe(id): MotionSpec`（契约 §7-A 逐字接口）。
 * 数值须与 `assets/contract/motion-tokens.json` 的 `recipes` 一致。
 */
object Motion {
    private val specs: List<MotionSpec> = listOf(
        MotionSpec(
            recipe = MotionRecipe.PRESS_REBOUND,
            durationMs = MotionTokens.microMs,
            looping = false,
            spring = MotionTokens.snappy,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.SHAPE),
                note = "scale 0.97 removed; border and enabled state unchanged, tap still works",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.MODE_INDICATOR_SLIDE,
            durationMs = MotionTokens.baseMs,
            looping = false,
            spring = MotionTokens.default,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.COLOR),
                note = "indicator snaps to target; selection still readable by fill color",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.CHIP_BOUNCE,
            durationMs = MotionTokens.microMs,
            looping = false,
            spring = MotionTokens.snappy,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.COLOR),
                note = "only fill color change remains",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.SUBMIT_THREE_DOT,
            durationMs = 500,
            looping = true,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.TEXT),
                note = "dots become a static alpha snapshot; button label shows in-progress and is disabled",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.WRONG_SHAKE,
            durationMs = 420,
            looping = false,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.TEXT, InfoCarrier.COLOR, InfoCarrier.ICON),
                note = "x stays 0; red text (answer_wrong) plus cross icon carry the result",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.CORRECT_STROKE,
            durationMs = MotionTokens.drawMs,
            looping = false,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.COLOR, InfoCarrier.ICON),
                note = "circle and check are drawn fully stroked at once",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.THINKING_WAIT,
            durationMs = 0,
            looping = true,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.TEXT),
                note = "static text only (thinking_short, thinking_long past 8s)",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.TOP_TOAST,
            durationMs = MotionTokens.baseMs,
            looping = false,
            spring = MotionTokens.soft,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.TEXT, InfoCarrier.SHAPE),
                note = "translation removed, instant fade only; message stays inside top container",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.REVIEW_BREATH_DOT,
            durationMs = 1200,
            looping = true,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.SHAPE),
                note = "breathing layer not rendered; static solid dot remains",
            ),
        ),
        MotionSpec(
            recipe = MotionRecipe.MASTERY_GROWTH,
            durationMs = MotionTokens.slowMs,
            looping = false,
            spring = null,
            minimal = MinimalPlan(
                rendersStatic = true,
                carriers = setOf(InfoCarrier.TEXT, InfoCarrier.VALUE),
                note = "jump straight to the target value; value and unit rendered as text",
            ),
        ),
    )

    /** 按 id 取规格（契约 §7-A 逐字：`fun recipe(id: MotionRecipe): MotionSpec`）。 */
    fun recipe(id: MotionRecipe): MotionSpec = specs.first { it.recipe == id }

    /** 全表（gallery 与测试遍历用）。 */
    val all: List<MotionSpec> get() = specs
}

// ---------------------------------------------------------------------------
// 配方 1：按压回弹（whileTap scale 0.97 + snappy；disabled 时不播放）
// ---------------------------------------------------------------------------

/**
 * 配方 1 可复用实现。minimal / reduced 档自动取消 scale（信息由按钮态本身承载）。
 * @param enabled 为 false 时**不播放**按压反馈（契约 §3 按压反馈统一规则）。
 */
@Composable
fun Modifier.pressRebound(enabled: Boolean, onClick: () -> Unit): Modifier {
    val level = LocalMotionLevel.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && level.allowsTranslation) 0.97f else 1f,
        animationSpec = MotionTokens.snappy,
        label = "press_rebound_scale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

// ---------------------------------------------------------------------------
// 配方 5：答错摇头（420ms，x 恒 0 于 minimal；第 6 步判题零件直接复用）
// ---------------------------------------------------------------------------

/**
 * 配方 5 可复用实现：内容层独立容器位移 `x [0,-9,9,-6,6,-2,0]`，420ms ease-out。
 * minimal / reduced 档 `x` 恒为 0 —— 调用方**必须**同时给出红色文案 + 错号图标（信息不丢失）。
 *
 * @param shakeKey 每次 +1 触发一次摇头；0 表示尚未触发（静止）。
 */
@Composable
fun Modifier.wrongShake(shakeKey: Int): Modifier {
    val level = LocalMotionLevel.current
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(shakeKey, level) {
        if (shakeKey == 0 || !level.allowsTranslation) {
            offsetX.snapTo(0f)
            return@LaunchedEffect
        }
        offsetX.snapTo(0f)
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 420
                0f at 0
                -9f at 70
                9f at 140
                -6f at 230
                6f at 300
                -2f at 380
                0f at 420
            },
        )
    }
    return if (level.allowsTranslation) {
        this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
    } else {
        this
    }
}

// ---------------------------------------------------------------------------
// 配方 6：答对描边（圆圈 pathLength 0->1 600ms，对勾延迟 160ms 再描边 800ms）
// ---------------------------------------------------------------------------

/**
 * 配方 6 可复用实现：圆圈描边 + 对勾。minimal / reduced 档直接满描边静态显示。
 *
 * @param playKey 每次 +1 播放一次；0 表示直接显示终态。
 */
@Composable
fun CorrectStroke(
    playKey: Int,
    diameter: Dp = 32.dp,
    modifier: Modifier = Modifier,
    color: Color = ColorTokens.Success,
) {
    val level = LocalMotionLevel.current
    val circleProgress = remember { Animatable(1f) }
    val checkProgress = remember { Animatable(1f) }
    LaunchedEffect(playKey, level) {
        if (playKey == 0 || !level.allowsTranslation) {
            circleProgress.snapTo(1f)
            checkProgress.snapTo(1f)
            return@LaunchedEffect
        }
        circleProgress.snapTo(0f)
        checkProgress.snapTo(0f)
        circleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = MotionTokens.slowMs, easing = LinearEasing),
        )
        delay(160)
        checkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = MotionTokens.drawMs, easing = LinearEasing),
        )
    }
    Canvas(modifier = modifier.size(diameter)) {
        val strokeWidth = 1.7.dp.toPx()
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * circleProgress.value,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawCheckMark(color = color, strokeWidth = strokeWidth, progress = checkProgress.value)
    }
}

/**
 * 对勾两段线（A→B→C）的按 `progress` 部分描边。
 *
 * 描边进度按**弧长**分配：先画完 A→B，再画 B→C，使 pathLength 0→1 的语义与圆圈一致
 * （契约 §8.4-6：圆圈 `pathLength 0→1`（600ms），对勾延迟 160ms 后描边（800ms））。
 */
private fun DrawScope.drawCheckMark(color: Color, strokeWidth: Float, progress: Float) {
    if (progress <= 0f) return
    val a = Offset(size.width * 0.28f, size.height * 0.52f)
    val b = Offset(size.width * 0.44f, size.height * 0.68f)
    val c = Offset(size.width * 0.74f, size.height * 0.34f)
    val len1 = (b - a).getDistance()
    val len2 = (c - b).getDistance()
    val travelled = progress * (len1 + len2)
    val t1 = min(1f, travelled / len1)
    val t2 = if (travelled <= len1) 0f else min(1f, (travelled - len1) / len2)
    drawLine(
        color = color,
        start = a,
        end = a + (b - a) * t1,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    if (t2 > 0f) {
        drawLine(
            color = color,
            start = b,
            end = b + (c - b) * t2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
