package com.eduagent.core.model.part

/**
 * A2UI 零件信封（契约 01-契约.md §4 通用信封，v1）。
 *
 * ```json
 * { "v": 1, "id": "<回合内唯一>", "type": "<parts.json 的 type>", "payload": { ... } }
 * ```
 *
 * 契约依据：§4 十六个零件目录；§7 校验顺序（语法 → 白名单 type → 必需字段）。
 * 本层是**纯 Kotlin schema**（无 Android 依赖，契约 §2 `:core:model` 职责）；
 * `:ui:parts` 只消费、不另建 schema。
 */
data class PartEnvelope(
    /** 信封版本；当前冻结为 1。 */
    val v: Int,
    /** 回合内唯一 id。 */
    val id: String,
    /** 零件签名；取值必须是 [com.eduagent.core.model.PartTypes] 的 16 个之一。 */
    val type: String,
    /** 零件私有字段。 */
    val payload: PartPayload,
)

/**
 * 16 个零件 payload 的公共父类型。
 *
 * 说明：信封以它为静态类型承载；渲染分发由 `:ui:parts` 的 `PartRegistry` 按 [PartEnvelope.type] 完成，
 * 对 payload 做安全降型，失败走判废退化（不崩，契约 §7）。
 */
sealed interface PartPayload
