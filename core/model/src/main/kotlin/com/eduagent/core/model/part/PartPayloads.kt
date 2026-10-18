package com.eduagent.core.model.part

/**
 * 16 个零件的 payload 类型（契约 01-契约.md §4 目录 v1）。
 *
 * 纪律（红线 R-T10）：**payload 内一律不得出现动效 / 时长 / 曲线字段**；动效写死在 `:core:design`。
 * 字段名与顺序以 `assets/contract/parts.json` 的 `payload_required` 为准（本文件为端侧镜像）。
 * 枚举的 [wire] 值为 JSON 线上取值（逐字取自契约 §4 / §7-E）。
 */

// ---------------------------------------------------------------- 1 explain_card

/** `explain_card.blocks[].kind`（§4-1）。 */
enum class BlockKind(val wire: String) {
    TEXT("text"),
    FORMULA("formula"),
    STEPS("steps"),
}

/** 块：`{kind:text|formula|steps, body|items}`（`body` 与 `items` 二选一，按 kind 取用）。 */
data class ExplainBlock(
    val kind: BlockKind,
    val body: String? = null,
    val items: List<String> = emptyList(),
)

data class ExplainCardPayload(
    val title: String,
    val blocks: List<ExplainBlock>,
) : PartPayload

// ---------------------------------------------------------------- 2 keypoint_card

/** `points[] = {text, level}`（§4-2）。 */
data class KeyPoint(val text: String, val level: Int)

data class KeypointCardPayload(
    val title: String,
    val points: List<KeyPoint>,
) : PartPayload

// ---------------------------------------------------------------- 3 compare_card

/** `columns[]` 2~3 列；`rows[][]` 与列对齐（§4-3）。 */
data class CompareCardPayload(
    val title: String,
    val columns: List<String>,
    val rows: List<List<String>>,
) : PartPayload

// ---------------------------------------------------------------- 4 quiz_card

/** `quiz_card.kind`（§4-4 / §7-E）。 */
enum class QuizKind(val wire: String) {
    CHOICE("choice"),
    BLANK("blank"),
    OPEN("open"),
}

/**
 * 题卡 payload。
 *
 * 硬规则（契约 §4）：`answer` 由**端侧保管**，判分前不下发给 UI —— 故本 payload **不含 `answer`**；
 * 用户作答后由 `verdict_card` 呈现 `answer` 与解析。
 */
data class QuizCardPayload(
    val stem: String,
    val kind: QuizKind,
    val options: List<String> = emptyList(),
    val explain: String,
) : PartPayload

// ---------------------------------------------------------------- 5 verdict_card

/** `verdict_card.result`（§4-5 / §7-E）。 */
enum class VerdictResult(val wire: String) {
    CORRECT("correct"),
    WRONG("wrong"),
}

data class VerdictCardPayload(
    val result: VerdictResult,
    val userAnswer: String,
    val answer: String,
    val explain: String,
) : PartPayload

// ---------------------------------------------------------------- 6 error_cause_card

data class ErrorCauseCardPayload(
    val cause: String,
    val evidence: String,
    val fix: List<String>,
) : PartPayload

// ---------------------------------------------------------------- 7 map_card

data class MapNode(val id: String, val label: String)

data class MapEdge(val from: String, val to: String)

data class MapCardPayload(
    val title: String,
    val nodes: List<MapNode>,
    val edges: List<MapEdge>,
) : PartPayload

// ---------------------------------------------------------------- 8 timeline_card

/** `events[] = {at, text}`（§4-8）。 */
data class TimelineEvent(val at: String, val text: String)

data class TimelineCardPayload(
    val title: String,
    val events: List<TimelineEvent>,
) : PartPayload

// ---------------------------------------------------------------- 9 table_card

data class TableCardPayload(
    val title: String,
    val headers: List<String>,
    val rows: List<List<String>>,
) : PartPayload

// ---------------------------------------------------------------- 10 plan_panel

/** `plan_panel.horizon`（§4-10 / D32：short ≤30 天，long = 无考试日或 >30 天）。 */
enum class PlanHorizon(val wire: String) {
    SHORT("short"),
    LONG("long"),
}

/** `tasks[] = {week, items[], done}`（§4-10）。 */
data class PlanTask(
    val week: String,
    val items: List<String>,
    val done: Boolean,
)

data class PlanPanelPayload(
    val horizon: PlanHorizon,
    val goals: List<String>,
    val tasks: List<PlanTask>,
) : PartPayload

// ---------------------------------------------------------------- 11 progress_card

/** `progress_card.metric`（§4-11 / §7-E）。 */
enum class ProgressMetric(val wire: String) {
    MASTERY("mastery"),
    PLAN("plan"),
    SCORE("score"),
}

/** `series[] = {label, value}`（§4-11）。 */
data class SeriesPoint(val label: String, val value: Float)

data class ProgressCardPayload(
    val title: String,
    val series: List<SeriesPoint>,
    val metric: ProgressMetric,
) : PartPayload

// ---------------------------------------------------------------- 12 review_card

data class ReviewCardPayload(
    val itemId: String,
    val prompt: String,
    val dueAt: String,
    val reps: Int,
) : PartPayload

// ---------------------------------------------------------------- 13 diagnose_card

/** `weak[] = {topic, score, evidence}`（§4-13）。 */
data class WeakItem(val topic: String, val score: Float, val evidence: String)

data class DiagnoseCardPayload(
    val title: String,
    val weak: List<WeakItem>,
) : PartPayload

// ---------------------------------------------------------------- 14 profile_confirm_card

/** `profile_entry.sensitivity`（契约 §10 数据契约：LOW / HIGH）。 */
enum class Sensitivity(val wire: String) {
    LOW("LOW"),
    HIGH("HIGH"),
}

/** `entries[] = {category, key, value, sensitivity}`（§4-14）。 */
data class ProfileEntry(
    val category: String,
    val key: String,
    val value: String,
    val sensitivity: Sensitivity,
)

/**
 * 画像确认卡 payload。
 *
 * 硬规则（§4-14 / R-E5）：渲染层**必须保留可改可删**；拒绝的条目不入库。可改可删的动作
 * 由 composable 的 `onEdit` / `onDelete` 回调承载，**不进 payload**（payload 不含任何交互/动效字段）。
 */
data class ProfileConfirmCardPayload(
    val entries: List<ProfileEntry>,
) : PartPayload

// ---------------------------------------------------------------- 15 material_card

/** `material_card.source_kind`（§4-15 / §7-E）。 */
enum class MaterialSourceKind(val wire: String) {
    PHOTO("photo"),
    SCAN("scan"),
    UPLOAD("upload"),
}

data class MaterialCardPayload(
    val sourceKind: MaterialSourceKind,
    val summary: String,
    val fields: List<String>,
    val uncertain: List<String>,
) : PartPayload

// ---------------------------------------------------------------- 16 tool_card

/** `results[] = {title, url, snippet, source}`；`source` 为空 → 该条**不得渲染**（R-C1/R-C2）。 */
data class ToolResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String?,
)

data class ToolCardPayload(
    val query: String,
    val results: List<ToolResult>,
) : PartPayload
