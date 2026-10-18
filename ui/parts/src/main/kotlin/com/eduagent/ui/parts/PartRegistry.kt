package com.eduagent.ui.parts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.CompareCardPayload
import com.eduagent.core.model.part.DiagnoseCardPayload
import com.eduagent.core.model.part.ErrorCauseCardPayload
import com.eduagent.core.model.part.ExplainCardPayload
import com.eduagent.core.model.part.KeypointCardPayload
import com.eduagent.core.model.part.MapCardPayload
import com.eduagent.core.model.part.MaterialCardPayload
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.PlanPanelPayload
import com.eduagent.core.model.part.ProfileConfirmCardPayload
import com.eduagent.core.model.part.ProgressCardPayload
import com.eduagent.core.model.part.QuizCardPayload
import com.eduagent.core.model.part.QuizKind
import com.eduagent.core.model.part.ReviewCardPayload
import com.eduagent.core.model.part.TableCardPayload
import com.eduagent.core.model.part.TimelineCardPayload
import com.eduagent.core.model.part.ToolCardPayload
import com.eduagent.core.model.part.VerdictCardPayload
import com.eduagent.ui.parts.fallback.PlainTextPart
import com.eduagent.ui.parts.part.CompareCardPart
import com.eduagent.ui.parts.part.DiagnoseCardPart
import com.eduagent.ui.parts.part.ErrorCauseCardPart
import com.eduagent.ui.parts.part.ExplainCardPart
import com.eduagent.ui.parts.part.KeypointCardPart
import com.eduagent.ui.parts.part.MapCardPart
import com.eduagent.ui.parts.part.MaterialCardPart
import com.eduagent.ui.parts.part.PlanPanelPart
import com.eduagent.ui.parts.part.ProfileConfirmCardPart
import com.eduagent.ui.parts.part.ProgressCardPart
import com.eduagent.ui.parts.part.QuizCardPart
import com.eduagent.ui.parts.part.ReviewCardPart
import com.eduagent.ui.parts.part.TableCardPart
import com.eduagent.ui.parts.part.TimelineCardPart
import com.eduagent.ui.parts.part.ToolCardPart
import com.eduagent.ui.parts.part.VerdictCardPart

/**
 * 未知 type 埋点接收方（契约 §7-F：键 `unknown_part`）。
 *
 * 第 6 步只定义**接口与默认空实现**；真实上报链路由第 9/10 步在 `:core:network` / `:app` 装配，
 * 经 [LocalPartAnalytics] 注入，零件层不直接依赖网络模块（§2 方向铁律）。
 */
fun interface PartAnalytics {
    fun onUnknownPart(type: String)

    companion object {
        /** 默认空实现（未装配上报链路时静默，绝不抛异常）。 */
        val NoOp: PartAnalytics = PartAnalytics { }
    }
}

/** 埋点接收方的组合局部（默认 [PartAnalytics.NoOp]）。 */
val LocalPartAnalytics = staticCompositionLocalOf { PartAnalytics.NoOp }

/**
 * `type` → Composable 注册表（契约 §7-B 逐字接口；任务包 06 §3）。
 *
 * 纪律：
 * 1. 键集合恒等于 `:core:model` 的 [PartTypes.ALL]（与 `assets/contract/parts.json` 同序，16 条）；
 * 2. **未知 type 不崩、不白屏**：走 `unknown_part` 埋点一次 + 降级纯文本（§5.2、§7）；
 * 3. payload 类型不符（安全降型失败）同样走降级，绝不抛异常（§7 判废）；
 * 4. 本步只注册**已冻结的 16 个**；新增零件须先走 R-T3 三处同改，禁止夹带。
 */
object PartRegistry {

    /** 16 个 `type`，顺序同契约 §4（第 9 步 validator 与第 10 步渲染调用共用）。 */
    val types: List<String> = PartTypes.ALL

    /** 未知 type 的埋点键（§5.2、§7-F）。 */
    const val UNKNOWN_PART_KEY: String = "unknown_part"

    /**
     * 取该 `type` 的渲染器（契约 §7-B）。
     *
     * @return `@Composable (PartEnvelope) -> Unit`；**未知 type 返回降级渲染器**（纯文本，不抛异常）。
     */
    fun composableFor(type: String): @Composable (PartEnvelope) -> Unit = when (type) {
        PartTypes.EXPLAIN_CARD -> { envelope ->
            (envelope.payload as? ExplainCardPayload)?.let { ExplainCardPart(it) } ?: DegradedPart()
        }

        PartTypes.KEYPOINT_CARD -> { envelope ->
            (envelope.payload as? KeypointCardPayload)?.let { KeypointCardPart(it) } ?: DegradedPart()
        }

        PartTypes.COMPARE_CARD -> { envelope ->
            (envelope.payload as? CompareCardPayload)?.let { CompareCardPart(it) } ?: DegradedPart()
        }

        PartTypes.QUIZ_CARD -> { envelope ->
            (envelope.payload as? QuizCardPayload)?.let { QuizCardPart(it) } ?: DegradedPart()
        }

        PartTypes.VERDICT_CARD -> { envelope ->
            (envelope.payload as? VerdictCardPayload)?.let { VerdictCardPart(it) } ?: DegradedPart()
        }

        PartTypes.ERROR_CAUSE_CARD -> { envelope ->
            (envelope.payload as? ErrorCauseCardPayload)?.let { ErrorCauseCardPart(it) } ?: DegradedPart()
        }

        PartTypes.MAP_CARD -> { envelope ->
            (envelope.payload as? MapCardPayload)?.let { MapCardPart(it) } ?: DegradedPart()
        }

        PartTypes.TIMELINE_CARD -> { envelope ->
            (envelope.payload as? TimelineCardPayload)?.let { TimelineCardPart(it) } ?: DegradedPart()
        }

        PartTypes.TABLE_CARD -> { envelope ->
            (envelope.payload as? TableCardPayload)?.let { TableCardPart(it) } ?: DegradedPart()
        }

        PartTypes.PLAN_PANEL -> { envelope ->
            (envelope.payload as? PlanPanelPayload)?.let { PlanPanelPart(it) } ?: DegradedPart()
        }

        PartTypes.PROGRESS_CARD -> { envelope ->
            (envelope.payload as? ProgressCardPayload)?.let { ProgressCardPart(it) } ?: DegradedPart()
        }

        PartTypes.REVIEW_CARD -> { envelope ->
            (envelope.payload as? ReviewCardPayload)?.let { ReviewCardPart(it) } ?: DegradedPart()
        }

        PartTypes.DIAGNOSE_CARD -> { envelope ->
            (envelope.payload as? DiagnoseCardPayload)?.let { DiagnoseCardPart(it) } ?: DegradedPart()
        }

        PartTypes.PROFILE_CONFIRM_CARD -> { envelope ->
            (envelope.payload as? ProfileConfirmCardPayload)?.let { ProfileConfirmCardPart(it) } ?: DegradedPart()
        }

        PartTypes.MATERIAL_CARD -> { envelope ->
            (envelope.payload as? MaterialCardPayload)?.let { MaterialCardPart(it) } ?: DegradedPart()
        }

        PartTypes.TOOL_CARD -> { envelope ->
            (envelope.payload as? ToolCardPayload)?.let { ToolCardPart(it) } ?: DegradedPart()
        }

        else -> { envelope -> UnknownPart(envelope.type) }
    }
}

/**
 * 渲染入口（契约 §7-B 逐字接口）：`:app` / `:feature:main` 只调它，不直接调具体零件。
 *
 * 顺序（与契约 §7 校验顺序的端侧渲染部分对齐）：
 * 1. `type` 白名单（`PartRegistry.types`）——未知走 `unknown_part` 埋点 + 降级；
 * 2. 必需字段齐全性（[PartFieldCheck]）——缺失走判废降级（不崩）；
 * 3. 分发到具体零件。
 */
@Composable
fun PartHost(envelope: PartEnvelope, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        val missing = remember(envelope) { PartFieldCheck.missingFields(envelope) }
        if (missing.isEmpty() && envelope.type in PartRegistry.types) {
            PartRegistry.composableFor(envelope.type)(envelope)
        } else if (envelope.type !in PartRegistry.types) {
            UnknownPart(envelope.type)
        } else {
            DegradedPart()
        }
    }
}

/** 未知 type 降级渲染器：上报一次 `unknown_part` + 纯文本（§5.2、§7）。 */
@Composable
private fun UnknownPart(type: String) {
    val analytics = LocalPartAnalytics.current
    LaunchedEffect(type) { analytics.onUnknownPart(type) }
    DegradedPart()
}

/** 判废退化渲染器：纯文本段落，不做红色报错框（§7 二级兜底）。 */
@Composable
private fun DegradedPart() {
    PlainTextPart(text = stringResource(R.string.part_fallback_notice))
}

/**
 * 必需字段断言（§4.1 `PartFieldAssertTest`）。
 *
 * 端侧在 debug 下断言必需字段齐全；**字段缺失走判废（不崩）**——由 [PartHost] 落到 [DegradedPart]。
 * 纯函数（无 Compose 依赖），便于单测直接调用；空字符串视为缺失，必需列表为空视为缺失。
 */
object PartFieldCheck {

    /** @return 缺失的必需字段名（齐全时为空列表）。 */
    fun missingFields(envelope: PartEnvelope): List<String> = when (val payload = envelope.payload) {
        is ExplainCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.blocks.isEmpty()) add("blocks")
        }

        is KeypointCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.points.isEmpty()) add("points")
        }

        is CompareCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.columns.size !in 2..3) add("columns")
            if (payload.rows.isEmpty()) add("rows")
        }

        is QuizCardPayload -> buildList {
            if (payload.stem.isBlank()) add("stem")
            if (payload.explain.isBlank()) add("explain")
            if (payload.kind == QuizKind.CHOICE && payload.options.isEmpty()) add("options")
        }

        is VerdictCardPayload -> buildList {
            if (payload.userAnswer.isBlank()) add("user_answer")
            if (payload.answer.isBlank()) add("answer")
            if (payload.explain.isBlank()) add("explain")
        }

        is ErrorCauseCardPayload -> buildList {
            if (payload.cause.isBlank()) add("cause")
            if (payload.evidence.isBlank()) add("evidence")
            if (payload.fix.isEmpty()) add("fix")
        }

        is MapCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.nodes.isEmpty()) add("nodes")
            // edges 允许为空（单节点图），但不可为 null（类型即保证）
        }

        is TimelineCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.events.isEmpty()) add("events")
        }

        is TableCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.headers.isEmpty()) add("headers")
            if (payload.rows.isEmpty()) add("rows")
        }

        is PlanPanelPayload -> buildList {
            if (payload.goals.isEmpty()) add("goals")
            if (payload.tasks.isEmpty()) add("tasks")
        }

        is ProgressCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.series.isEmpty()) add("series")
        }

        is ReviewCardPayload -> buildList {
            if (payload.itemId.isBlank()) add("item_id")
            if (payload.prompt.isBlank()) add("prompt")
            if (payload.dueAt.isBlank()) add("due_at")
        }

        is DiagnoseCardPayload -> buildList {
            if (payload.title.isBlank()) add("title")
            if (payload.weak.isEmpty()) add("weak")
        }

        is ProfileConfirmCardPayload -> buildList {
            if (payload.entries.isEmpty()) add("entries")
        }

        is MaterialCardPayload -> buildList {
            if (payload.summary.isBlank()) add("summary")
        }

        is ToolCardPayload -> buildList {
            if (payload.query.isBlank()) add("query")
            if (payload.results.isEmpty()) add("results")
        }
    }
}
