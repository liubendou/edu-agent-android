package com.eduagent.core.design.motion

/**
 * 五档吐字节奏 tau（契约 §8.5 / §13 D29，供第 7 步流式内核消费）。
 *
 * 语义：端侧细平滑时 `rate = clamp(backlog / tau, MIN, MAX)`——积压多则加速追赶，追平则减速。
 * 数值逐字对齐 `assets/contract/motion-tokens.json`，**禁止**改写。
 */
object StreamTau {
    /** 快速学习：干脆、节奏快。 */
    const val QUICK_SECONDS: Float = 0.18f

    /** 标准：从容。 */
    const val STANDARD_SECONDS: Float = 0.25f

    /** 深度：越高质量越不急，给思考留时间。 */
    const val DEEP_SECONDS: Float = 0.35f

    /** 复习：追求节奏与效率，不该有等待感。 */
    const val REVIEW_SECONDS: Float = 0.15f

    /** 提分：稳，强调计划感。 */
    const val BOOST_SECONDS: Float = 0.30f

    /** 按档名取 tau（档名逐字同契约 §5 的 mode 值）。 */
    fun forMode(mode: String): Float = when (mode.lowercase()) {
        "quick" -> QUICK_SECONDS
        "standard" -> STANDARD_SECONDS
        "deep" -> DEEP_SECONDS
        "review" -> REVIEW_SECONDS
        "boost" -> BOOST_SECONDS
        else -> STANDARD_SECONDS
    }
}
