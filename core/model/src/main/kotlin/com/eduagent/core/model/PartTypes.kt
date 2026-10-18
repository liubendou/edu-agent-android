package com.eduagent.core.model

/**
 * A2UI 目录 v1 的 16 个零件签名（`type` 字段值）。
 *
 * 契约依据：01-契约.md §4 十六个零件目录；红线 R-T3（新增零件必须三处同改）。
 * 唯一事实源仍是 `assets/contract/parts.json`；本文件是端侧编译期镜像，**必须**与其逐字同序一致，
 * 由 `:tools:contract-check` 的 `PartTypesConsistencyTest` 在 `contractCheck` 中卡死。
 */
object PartTypes {
    const val EXPLAIN_CARD = "explain_card"
    const val KEYPOINT_CARD = "keypoint_card"
    const val COMPARE_CARD = "compare_card"
    const val QUIZ_CARD = "quiz_card"
    const val VERDICT_CARD = "verdict_card"
    const val ERROR_CAUSE_CARD = "error_cause_card"
    const val MAP_CARD = "map_card"
    const val TIMELINE_CARD = "timeline_card"
    const val TABLE_CARD = "table_card"
    const val PLAN_PANEL = "plan_panel"
    const val PROGRESS_CARD = "progress_card"
    const val REVIEW_CARD = "review_card"
    const val DIAGNOSE_CARD = "diagnose_card"
    const val PROFILE_CONFIRM_CARD = "profile_confirm_card"
    const val MATERIAL_CARD = "material_card"
    const val TOOL_CARD = "tool_card"

    /** 与 `parts.json` 的 `parts[].type` 同序（第 1~16 项）。 */
    val ALL: List<String> = listOf(
        EXPLAIN_CARD,
        KEYPOINT_CARD,
        COMPARE_CARD,
        QUIZ_CARD,
        VERDICT_CARD,
        ERROR_CAUSE_CARD,
        MAP_CARD,
        TIMELINE_CARD,
        TABLE_CARD,
        PLAN_PANEL,
        PROGRESS_CARD,
        REVIEW_CARD,
        DIAGNOSE_CARD,
        PROFILE_CONFIRM_CARD,
        MATERIAL_CARD,
        TOOL_CARD,
    )
}
