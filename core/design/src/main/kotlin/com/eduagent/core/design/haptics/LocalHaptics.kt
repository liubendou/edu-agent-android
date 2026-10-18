package com.eduagent.core.design.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

/**
 * 触觉契约（契约 §9 / §13 D28）。
 *
 * - 触觉**只用于结果确认**（对 / 错 / 上下文点击），不用于装饰；
 * - 页面禁止直接调用 `performHapticFeedback`（§14.5）——只能走 [LocalHaptics]；
 * - 一键可关：由上层读 `haptics_enabled`（DataStore）后注入 [Haptics] 实现（任务包 05 Q1A：
 *   本模块**不引入 datastore 依赖**，值由组合根注入）。
 */
enum class HapticKind {
    /** 结果正确 / 提交成功。 */
    CONFIRM,

    /** 结果错误 / 校验失败。 */
    REJECT,

    /** 上下文点击（模式切换、chip 选中）。 */
    CONTEXT_CLICK,
}

/** 触觉唯一入口。 */
interface Haptics {
    fun perform(kind: HapticKind)
}

/** 默认实现：什么都不做（未接入组合根时也不会崩）。 */
object NoOpHaptics : Haptics {
    override fun perform(kind: HapticKind) = Unit
}

/**
 * 开关门（纯 Kotlin，可纯 JVM 测试）：`haptics_enabled = false` 时一律不下发。
 * @param enabled 对应 DataStore 键 `haptics_enabled`（默认 true）。
 */
class GatedHaptics(
    private val delegate: Haptics,
    private val enabled: Boolean,
) : Haptics {
    override fun perform(kind: HapticKind) {
        if (enabled) delegate.perform(kind)
    }
}

/**
 * Android 落地实现：**唯一**允许调用 `performHapticFeedback` 的地方。
 *
 * `CONFIRM` / `REJECT` 常量自 API 30 起提供，minSdk 29 上退化为 `VIRTUAL_KEY` / `LONG_PRESS`，
 * 保证 29 上仍有轻反馈而不是空调用。
 */
class AndroidHaptics(
    private val view: View,
    private val enabled: Boolean,
) : Haptics {
    override fun perform(kind: HapticKind) {
        if (!enabled) return
        view.performHapticFeedback(kind.systemConstant(Build.VERSION.SDK_INT))
    }
}

/** 场景 -> 系统常量（逐字对应 [HapticKind] 三值）。 */
fun HapticKind.systemConstant(sdkInt: Int): Int = when (this) {
    HapticKind.CONFIRM ->
        if (sdkInt >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }

    HapticKind.REJECT ->
        if (sdkInt >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

    HapticKind.CONTEXT_CLICK -> HapticFeedbackConstants.CONTEXT_CLICK
}

/** 三场景的稳定名（供不能触碰 android.view 常量的纯 JVM 用例断言）。 */
fun HapticKind.systemConstantName(): String = when (this) {
    HapticKind.CONFIRM -> "CONFIRM"
    HapticKind.REJECT -> "REJECT"
    HapticKind.CONTEXT_CLICK -> "CONTEXT_CLICK"
}

/** 零件 / 页面调用 `LocalHaptics.current.perform(kind)`（契约 §7-A）。 */
val LocalHaptics: ProvidableCompositionLocal<Haptics> = staticCompositionLocalOf { NoOpHaptics }

/**
 * 组合根便捷入口：把开关值包成 [AndroidHaptics]。
 * 只有组合根（`:app`）应调用它并 `LocalHaptics provides rememberAndroidHaptics(enabled)`。
 */
@Composable
fun rememberAndroidHaptics(enabled: Boolean): Haptics {
    val view = LocalView.current
    return remember(view, enabled) { AndroidHaptics(view, enabled) }
}
