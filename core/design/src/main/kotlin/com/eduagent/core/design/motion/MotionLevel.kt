package com.eduagent.core.design.motion

import android.app.ActivityManager
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * 三档动效纪律（契约 §8.3 / §13 D27 / M4）。
 *
 * - [FULL]：全部动效。
 * - [REDUCED]：去位移与循环，只保留 ≤`micro` 的透明度/颜色变化（用户可选，或低端机自动降档）。
 * - [MINIMAL]：全静态（duration=0 退化为瞬时，**不是隐藏**）；由系统「移除动画」强制进入，**用户不可反向覆盖**。
 */
enum class MotionLevel {
    FULL,
    REDUCED,
    MINIMAL;

    /** 是否允许循环动画（reduced / minimal 一律禁止，§8.3 规则 2）。 */
    val allowsLooping: Boolean get() = this == FULL

    /** 是否允许位移 / 尺寸类物理动画（reduced / minimal 一律禁止）。 */
    val allowsTranslation: Boolean get() = this == FULL

    /** 是否允许 ≤`micro` 的透明度 / 颜色过渡（minimal 为全静态 0ms）。 */
    val allowsFade: Boolean get() = this != MINIMAL

    companion object {
        /**
         * 三档判定**纯函数**（任务包 05 Q2A）：无 Android / Compose 依赖，可纯 JVM 全覆盖。
         *
         * 优先级（D27）：系统「移除动画」> 用户显式覆盖 > 低端机自动降档 > 默认 full。
         *
         * @param systemScaleIsZero `Settings.Global.ANIMATOR_DURATION_SCALE == 0f`，即系统「移除动画」。
         * @param userOverride `motion_user_override`；为 null 表示用户未显式选择。
         * @param deviceTier 低端机判定（M4：内存 / 帧率超阈）。系统 minimal 时**被忽略**。
         */
        fun resolve(
            systemScaleIsZero: Boolean,
            userOverride: MotionLevel?,
            deviceTier: DeviceTier,
        ): MotionState = when {
            systemScaleIsZero ->
                MotionState(MINIMAL, systemForced = true, canUserOverride = false)

            userOverride != null ->
                MotionState(userOverride, systemForced = false, canUserOverride = true)

            deviceTier == DeviceTier.LOW ->
                MotionState(REDUCED, systemForced = false, canUserOverride = true)

            else ->
                MotionState(FULL, systemForced = false, canUserOverride = true)
        }
    }
}

/**
 * 透明度 / 颜色过渡的实际时长（契约 §8.3 的「≤`micro`」硬上限在此统一收敛）。
 *
 * @param fullMs full 档使用的时长；reduced 档被钳到 `micro`；minimal 档归零（瞬时）。
 */
fun MotionLevel.fadeDurationMs(fullMs: Int): Int = when (this) {
    MotionLevel.FULL -> fullMs
    MotionLevel.REDUCED -> minOf(fullMs, MotionTokens.microMs)
    MotionLevel.MINIMAL -> 0
}

/** 设备档位（M4 低端机自动降档的输入）。 */
enum class DeviceTier { STANDARD, LOW }

/**
 * 判定结果（任务包 05 Q4A）：除 level 外，额外暴露两个 UI 决策位。
 *
 * - [systemForced]：true = 系统强制 minimal（§8.3 禁止给反向覆盖入口）。
 * - [canUserOverride]：false = 页面**不得**渲染三档开关。
 */
data class MotionState(
    val level: MotionLevel,
    val systemForced: Boolean,
    val canUserOverride: Boolean,
)

/** 零件 / 页面读它决定分支（契约 §7-A：`ProvidableCompositionLocal<MotionLevel>`）。 */
val LocalMotionLevel: ProvidableCompositionLocal<MotionLevel> = staticCompositionLocalOf { MotionLevel.FULL }

/** 需要读 `systemForced` / `canUserOverride` 时用（如 gallery 是否渲染三档开关）。 */
val LocalMotionState: ProvidableCompositionLocal<MotionState> =
    staticCompositionLocalOf { MotionState(MotionLevel.FULL, systemForced = false, canUserOverride = true) }

/**
 * 读系统「移除动画」（D27）。
 *
 * 冻结契约 §14.7 / D27 的原始写法是 `LocalMotionDurationScale.current == 0f`；在冻结的
 * Compose 1.12.1 中该 CompositionLocal 已被移除（改由 CoroutineContext 的
 * `MotionDurationScale` 承载）。此处改读其**唯一上游**：系统设置
 * `Settings.Global.ANIMATOR_DURATION_SCALE`（Compose 内部的 MotionDurationScaleImpl 也正是
 * 监听这个键）。语义等价，差异已登记为待裁决项（见 docs/06 报告）。
 */
@Composable
fun rememberSystemAnimationScaleIsZero(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

/** 低端机判定（M4）：`ActivityManager.isLowRamDevice`。 */
@Composable
fun rememberDeviceTier(): DeviceTier {
    val context = LocalContext.current
    return remember(context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (am != null && am.isLowRamDevice) DeviceTier.LOW else DeviceTier.STANDARD
    }
}

/**
 * 组合根 / 演示页用的判定入口：读系统设置 + 设备档位 + 用户覆盖，产出 [MotionState]。
 * 上层（第 10 步）应把它接进 `LocalMotionLevel` / `LocalMotionState`。
 */
@Composable
fun rememberMotionState(userOverride: MotionLevel? = null): MotionState {
    val systemZero = rememberSystemAnimationScaleIsZero()
    val tier = rememberDeviceTier()
    return remember(systemZero, userOverride, tier) {
        MotionLevel.resolve(systemZero, userOverride, tier)
    }
}
