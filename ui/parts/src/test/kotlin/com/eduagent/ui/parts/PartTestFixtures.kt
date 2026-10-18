package com.eduagent.ui.parts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.eduagent.core.model.PartTypes

/**
 * 测试夹具（任务包 06 §4.1 / §14.7）。
 *
 * 文本纪律：断言用的期望文案一律**取自样品里的 ASCII 字段**或**从 `strings.xml` 读取**，
 * 测试源码内不出现硬编码中文（与宿主源码同一纪律）。
 */

/** 每个零件用于断言「已渲染」的 ASCII 文案（取自 [com.eduagent.ui.parts.gallery.PartSamples]）。 */
internal val PART_EXPECTED_TEXT: Map<String, String> = mapOf(
    PartTypes.EXPLAIN_CARD to "Photosynthesis",
    PartTypes.KEYPOINT_CARD to "Key points",
    PartTypes.COMPARE_CARD to "Mitosis vs Meiosis",
    PartTypes.QUIZ_CARD to "Which organelle performs photosynthesis?",
    PartTypes.VERDICT_CARD to "Mitochondrion",
    PartTypes.ERROR_CAUSE_CARD to "Mixed up respiration with photosynthesis",
    PartTypes.MAP_CARD to "Cell structures",
    PartTypes.TIMELINE_CARD to "Protein synthesis",
    PartTypes.TABLE_CARD to "SI units",
    PartTypes.PLAN_PANEL to "Finish chapter 3",
    PartTypes.PROGRESS_CARD to "Mastery trend",
    PartTypes.REVIEW_CARD to "Define photosynthesis in one sentence",
    PartTypes.DIAGNOSE_CARD to "Weak spots",
    PartTypes.PROFILE_CONFIRM_CARD to "step_by_step",
    PartTypes.MATERIAL_CARD to "Textbook page 42 about cell organelles",
    PartTypes.TOOL_CARD to "chloroplast function",
)

/** 取该零件在陈列样品中可断言的文案。 */
internal fun expectedTextOf(type: String): String = PART_EXPECTED_TEXT.getValue(type)

/** 从资源读文案（避免在测试源码硬编码中文）。 */
internal fun stringOf(resId: Int): String =
    ApplicationProvider.getApplicationContext<Context>().getString(resId)
