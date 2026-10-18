package com.eduagent.ui.parts.gallery

import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.BlockKind
import com.eduagent.core.model.part.CompareCardPayload
import com.eduagent.core.model.part.DiagnoseCardPayload
import com.eduagent.core.model.part.ErrorCauseCardPayload
import com.eduagent.core.model.part.ExplainBlock
import com.eduagent.core.model.part.ExplainCardPayload
import com.eduagent.core.model.part.KeyPoint
import com.eduagent.core.model.part.KeypointCardPayload
import com.eduagent.core.model.part.MapCardPayload
import com.eduagent.core.model.part.MapEdge
import com.eduagent.core.model.part.MapNode
import com.eduagent.core.model.part.MaterialCardPayload
import com.eduagent.core.model.part.MaterialSourceKind
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.PartPayload
import com.eduagent.core.model.part.PlanHorizon
import com.eduagent.core.model.part.PlanPanelPayload
import com.eduagent.core.model.part.PlanTask
import com.eduagent.core.model.part.ProfileConfirmCardPayload
import com.eduagent.core.model.part.ProfileEntry
import com.eduagent.core.model.part.ProgressCardPayload
import com.eduagent.core.model.part.ProgressMetric
import com.eduagent.core.model.part.QuizCardPayload
import com.eduagent.core.model.part.QuizKind
import com.eduagent.core.model.part.ReviewCardPayload
import com.eduagent.core.model.part.Sensitivity
import com.eduagent.core.model.part.SeriesPoint
import com.eduagent.core.model.part.TableCardPayload
import com.eduagent.core.model.part.TimelineCardPayload
import com.eduagent.core.model.part.TimelineEvent
import com.eduagent.core.model.part.ToolCardPayload
import com.eduagent.core.model.part.ToolResult
import com.eduagent.core.model.part.VerdictCardPayload
import com.eduagent.core.model.part.VerdictResult
import com.eduagent.core.model.part.WeakItem

/**
 * 陈列页与测试共用的 16 个零件样品（任务包 06 §3 gallery / §7-C）。
 *
 * 文本纪律：**本文件全部为 ASCII 示例文案**（仓库纪律：Kotlin 源码零硬编码中文，中文只进
 * `res/values/strings.xml`）。样品仅用于 debug 陈列页与渲染测试，不参与真实会话。
 *
 * 注：`tool_card` 样品**故意**含一条无 `source` 的结果，用于验证 R-C1/R-C2「无来源不渲染」；
 * `verdict_card` 样品故意是 `wrong`，用于验证 minimal 档「颜色 + 图标 + 文案」三载体。
 */
internal object PartSamples {

    /** 16 个样品信封，顺序同契约 §4。 */
    val all: List<PartEnvelope> = listOf(
        envelope(PartTypes.EXPLAIN_CARD, explain()),
        envelope(PartTypes.KEYPOINT_CARD, keypoint()),
        envelope(PartTypes.COMPARE_CARD, compare()),
        envelope(PartTypes.QUIZ_CARD, quiz()),
        envelope(PartTypes.VERDICT_CARD, verdict()),
        envelope(PartTypes.ERROR_CAUSE_CARD, errorCause()),
        envelope(PartTypes.MAP_CARD, map()),
        envelope(PartTypes.TIMELINE_CARD, timeline()),
        envelope(PartTypes.TABLE_CARD, table()),
        envelope(PartTypes.PLAN_PANEL, plan()),
        envelope(PartTypes.PROGRESS_CARD, progress()),
        envelope(PartTypes.REVIEW_CARD, review()),
        envelope(PartTypes.DIAGNOSE_CARD, diagnose()),
        envelope(PartTypes.PROFILE_CONFIRM_CARD, profile()),
        envelope(PartTypes.MATERIAL_CARD, material()),
        envelope(PartTypes.TOOL_CARD, tool()),
    )

    /** 按 `type` 取样品信封；未知 type 返回一个判废样例（供负例测试使用）。 */
    fun envelopeFor(type: String): PartEnvelope =
        all.firstOrNull { it.type == type } ?: envelope(type, explain())

    /** 一条**未知 type** 的负例信封（`foo_card`，供 `unknown_part` 埋点与降级测试使用）。 */
    fun unknown(type: String = "foo_card"): PartEnvelope = envelope(type, explain())

    private fun envelope(type: String, payload: PartPayload): PartEnvelope =
        PartEnvelope(v = 1, id = "sample-$type", type = type, payload = payload)

    // ------------------------------------------------------------ 16 个样品

    private fun explain() = ExplainCardPayload(
        title = "Photosynthesis",
        blocks = listOf(
            ExplainBlock(kind = BlockKind.TEXT, body = "Plants convert light energy into chemical energy."),
            ExplainBlock(kind = BlockKind.FORMULA, body = "6CO2 + 6H2O -> C6H12O6 + 6O2"),
            ExplainBlock(
                kind = BlockKind.STEPS,
                items = listOf("Absorb light by chlorophyll", "Fix carbon into sugar", "Release oxygen"),
            ),
        ),
    )

    private fun keypoint() = KeypointCardPayload(
        title = "Key points",
        points = listOf(
            KeyPoint(text = "Chlorophyll absorbs red and blue light", level = 1),
            KeyPoint(text = "Stomata exchange gases", level = 2),
            KeyPoint(text = "Rate depends on light and CO2", level = 3),
        ),
    )

    private fun compare() = CompareCardPayload(
        title = "Mitosis vs Meiosis",
        columns = listOf("Item", "Mitosis", "Meiosis"),
        rows = listOf(
            listOf("Daughter cells", "2", "4"),
            listOf("Ploidy", "2n", "n"),
            listOf("Crossing over", "No", "Yes"),
        ),
    )

    private fun quiz() = QuizCardPayload(
        stem = "Which organelle performs photosynthesis?",
        kind = QuizKind.CHOICE,
        options = listOf("Chloroplast", "Mitochondrion", "Ribosome"),
        explain = "Chloroplast carries chlorophyll and builds sugar from light.",
    )

    private fun verdict() = VerdictCardPayload(
        result = VerdictResult.WRONG,
        userAnswer = "Mitochondrion",
        answer = "Chloroplast",
        explain = "Mitochondrion releases energy by respiration, not photosynthesis.",
    )

    private fun errorCause() = ErrorCauseCardPayload(
        cause = "Mixed up respiration with photosynthesis",
        evidence = "Chose mitochondrion for a light-driven process",
        fix = listOf("Compare the energy direction of both processes", "Recall where chlorophyll sits"),
    )

    private fun map() = MapCardPayload(
        title = "Cell structures",
        nodes = listOf(
            MapNode(id = "n1", label = "Chloroplast"),
            MapNode(id = "n2", label = "Mitochondrion"),
            MapNode(id = "n3", label = "Nucleus"),
        ),
        edges = listOf(MapEdge(from = "n1", to = "n3"), MapEdge(from = "n2", to = "n3")),
    )

    private fun timeline() = TimelineCardPayload(
        title = "Protein synthesis",
        events = listOf(
            TimelineEvent(at = "1", text = "Transcription happens in the nucleus"),
            TimelineEvent(at = "2", text = "mRNA travels to the ribosome"),
            TimelineEvent(at = "3", text = "Translation builds the polypeptide"),
        ),
    )

    private fun table() = TableCardPayload(
        title = "SI units",
        headers = listOf("Quantity", "Unit", "Symbol"),
        rows = listOf(
            listOf("Energy", "Joule", "J"),
            listOf("Force", "Newton", "N"),
            listOf("Power", "Watt", "W"),
        ),
    )

    private fun plan() = PlanPanelPayload(
        horizon = PlanHorizon.SHORT,
        goals = listOf("Finish chapter 3", "Score 80+ on the unit quiz"),
        tasks = listOf(
            PlanTask(week = "W1", items = listOf("Read 3.1", "Do 10 items"), done = true),
            PlanTask(week = "W2", items = listOf("Review notes", "Take quiz"), done = false),
        ),
    )

    private fun progress() = ProgressCardPayload(
        title = "Mastery trend",
        series = listOf(SeriesPoint("W1", 0.30f), SeriesPoint("W2", 0.55f), SeriesPoint("W3", 0.82f)),
        metric = ProgressMetric.MASTERY,
    )

    private fun review() = ReviewCardPayload(
        itemId = "item-1",
        prompt = "Define photosynthesis in one sentence",
        dueAt = "2026-10-19",
        reps = 3,
    )

    private fun diagnose() = DiagnoseCardPayload(
        title = "Weak spots",
        weak = listOf(
            WeakItem(topic = "Photosynthesis", score = 0.40f, evidence = "3 of 5 items wrong"),
            WeakItem(topic = "Stomata", score = 0.62f, evidence = "Slow recall in review"),
        ),
    )

    private fun profile() = ProfileConfirmCardPayload(
        entries = listOf(
            ProfileEntry("Preference", "explanation_style", "step_by_step", Sensitivity.LOW),
            ProfileEntry("Goal", "exam_date", "2027-06-07", Sensitivity.HIGH),
        ),
    )

    private fun material() = MaterialCardPayload(
        sourceKind = MaterialSourceKind.PHOTO,
        summary = "Textbook page 42 about cell organelles",
        fields = listOf("Title: Cells", "Formula: 6CO2 + 6H2O -> C6H12O6 + 6O2"),
        uncertain = listOf("Handwritten note in the margin"),
    )

    private fun tool() = ToolCardPayload(
        query = "chloroplast function",
        results = listOf(
            ToolResult(
                title = "Chloroplast - Wikipedia",
                url = "https://en.wikipedia.org/wiki/Chloroplast",
                snippet = "Organelle that conducts photosynthesis in plant cells",
                source = "Wikipedia",
            ),
            // 负例：无 source —— 该条**不得渲染**（R-C1/R-C2）
            ToolResult(
                title = "NO_SOURCE_ITEM",
                url = "https://example.invalid/x",
                snippet = "This item must never be rendered",
                source = null,
            ),
        ),
    )
}
