package com.eduagent.core.design.motion

import androidx.compose.animation.core.SpringSpec
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 时长与弹簧 tokens（契约 §8.1 / §8.2，逐字对齐 `assets/contract/motion-tokens.json`）。
 *
 * 纪律（任务包 05 §6）：**禁止**改写数值、**禁止**用 cubic-bezier 近似。
 * 弹簧一律使用 Compose 原生 [SpringSpec]（stiffness / dampingRatio 物理参数）。
 *
 * 注：冻结契约 §7-D 写的是 `Spring(stiffness, dampingRatio)`；在冻结的 Compose 1.12.1 中
 * `androidx.compose.animation.core.Spring` 已是**常量对象**（StiffnessMedium 等），
 * 可构造的原生弹簧类型是 [SpringSpec]。此处改用 [SpringSpec]（原生参数、语义一致），
 * 差异已登记为待裁决项（见 docs/06 报告）。
 */
object MotionTokens {
    /** 120ms：按压、chips 选中、hover、颜色切换。 */
    val micro: Duration = 120.milliseconds

    /** 200ms：面板进出、toast、卡片入场。 */
    val base: Duration = 200.milliseconds

    /** 600ms：大范围布局变化。 */
    val slow: Duration = 600.milliseconds

    /** 800ms：描边类（对勾、掌握度环）。 */
    val draw: Duration = 800.milliseconds

    /** 弹簧：按钮、chips、pill。 */
    val snappy: SpringSpec<Float> = SpringSpec(stiffness = 380f, dampingRatio = 30f)

    /** 弹簧：页面进出、卡片。 */
    val default: SpringSpec<Float> = SpringSpec(stiffness = 260f, dampingRatio = 30f)

    /** 弹簧：toast、面板。 */
    val soft: SpringSpec<Float> = SpringSpec(stiffness = 260f, dampingRatio = 26f)

    /** 毫秒直读（供 `tween(durationMillis = ...)` 使用）。 */
    const val microMs: Int = 120
    const val baseMs: Int = 200
    const val slowMs: Int = 600
    const val drawMs: Int = 800
}
