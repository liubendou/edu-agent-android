package com.eduagent.core.model

/**
 * 模型档位。
 *
 * 契约依据：红线 R-T7 / 决策 D23 —— 默认模型必须是 Flash（便宜、快），
 * **任何人不得**把默认改成 Pro（Pro 只在 §7 判废后自动升级一次时使用，且升级后锁定至会话结束）。
 */
enum class ModelPreference {
    FLASH,
    PRO,
}

/** 默认模型（R-T7：默认 Flash）。 */
val DefaultModel: ModelPreference = ModelPreference.FLASH
