package com.eduagent.ui.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.QuizCardPayload
import com.eduagent.core.model.part.QuizKind
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4.1 必测 · `PartFieldAssertTest`。
 *
 * 每个零件在 debug 下断言必需字段齐全；**字段缺失走判废（不崩）**。
 * 断言面：`PartFieldCheck.missingFields` 是纯函数（无 Compose 依赖），可直接单测。
 */
@RunWith(RobolectricTestRunner::class)
class PartFieldAssertTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun everySampleEnvelopeHasAllRequiredFields() {
        val offenders = PartSamples.all.filter { envelope ->
            PartFieldCheck.missingFields(envelope).isNotEmpty()
        }
        assertEquals(emptyList<PartEnvelope>(), offenders)
    }

    @Test
    fun missingFieldsAreReportedPerPart() {
        // 每类零件各给一个「缺必需字段」的负例，断言缺失名被如实报出
        val cases: List<Pair<PartEnvelope, String>> = listOf(
            envelope(PartTypes.EXPLAIN_CARD, com.eduagent.core.model.part.ExplainCardPayload(" ", emptyList())) to "title",
            envelope(PartTypes.KEYPOINT_CARD, com.eduagent.core.model.part.KeypointCardPayload("t", emptyList())) to "points",
            envelope(
                PartTypes.COMPARE_CARD,
                com.eduagent.core.model.part.CompareCardPayload("t", listOf("only one column"), emptyList()),
            ) to "columns",
            envelope(
                PartTypes.QUIZ_CARD,
                QuizCardPayload("s", QuizKind.CHOICE, emptyList(), "e"),
            ) to "options",
            envelope(
                PartTypes.VERDICT_CARD,
                com.eduagent.core.model.part.VerdictCardPayload(
                    com.eduagent.core.model.part.VerdictResult.CORRECT,
                    " ",
                    "a",
                    "e",
                ),
            ) to "user_answer",
            envelope(
                PartTypes.ERROR_CAUSE_CARD,
                com.eduagent.core.model.part.ErrorCauseCardPayload("c", "ev", emptyList()),
            ) to "fix",
            envelope(PartTypes.MAP_CARD, com.eduagent.core.model.part.MapCardPayload("t", emptyList(), emptyList())) to "nodes",
            envelope(
                PartTypes.TIMELINE_CARD,
                com.eduagent.core.model.part.TimelineCardPayload("t", emptyList()),
            ) to "events",
            envelope(PartTypes.TABLE_CARD, com.eduagent.core.model.part.TableCardPayload("t", emptyList(), emptyList())) to "headers",
            envelope(
                PartTypes.PLAN_PANEL,
                com.eduagent.core.model.part.PlanPanelPayload(
                    com.eduagent.core.model.part.PlanHorizon.SHORT,
                    emptyList(),
                    emptyList(),
                ),
            ) to "goals",
            envelope(
                PartTypes.PROGRESS_CARD,
                com.eduagent.core.model.part.ProgressCardPayload("t", emptyList(), com.eduagent.core.model.part.ProgressMetric.MASTERY),
            ) to "series",
            envelope(
                PartTypes.REVIEW_CARD,
                com.eduagent.core.model.part.ReviewCardPayload(" ", "p", "d", 0),
            ) to "item_id",
            envelope(
                PartTypes.DIAGNOSE_CARD,
                com.eduagent.core.model.part.DiagnoseCardPayload("t", emptyList()),
            ) to "weak",
            envelope(
                PartTypes.PROFILE_CONFIRM_CARD,
                com.eduagent.core.model.part.ProfileConfirmCardPayload(emptyList()),
            ) to "entries",
            envelope(
                PartTypes.MATERIAL_CARD,
                com.eduagent.core.model.part.MaterialCardPayload(
                    com.eduagent.core.model.part.MaterialSourceKind.PHOTO,
                    " ",
                    emptyList(),
                    emptyList(),
                ),
            ) to "summary",
            envelope(
                PartTypes.TOOL_CARD,
                com.eduagent.core.model.part.ToolCardPayload("q", emptyList()),
            ) to "results",
        )

        assertEquals(16, cases.size)
        cases.forEach { (envelope, expectedField) ->
            val missing = PartFieldCheck.missingFields(envelope)
            assertTrue(
                "expected $expectedField in $missing for ${envelope.type}",
                missing.contains(expectedField),
            )
        }
    }

    @Test
    fun missingFieldDegradesToPlainTextWithoutCrash() {
        val broken = envelope(
            PartTypes.EXPLAIN_CARD,
            com.eduagent.core.model.part.ExplainCardPayload(title = " ", blocks = emptyList()),
        )
        rule.setContent { EduTheme { PartHost(broken) } }
        rule.onNodeWithText(stringOf(R.string.part_fallback_notice)).assertIsDisplayed()
    }

    private fun envelope(type: String, payload: com.eduagent.core.model.part.PartPayload) =
        PartEnvelope(v = 1, id = "field-$type", type = type, payload = payload)
}
