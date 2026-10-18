package com.eduagent.ui.parts

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionLevel
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4.1 必测 · `PartMinimalTest`×16。
 *
 * 每个零件 1 个 minimal 分支：在 `MotionLevel.MINIMAL` 下仍渲染出**信息终态**
 * （§4 各零件的「minimal 档静态表达」列；§8.3：颜色 + 图标 + 文字三重载体不丢信息）。
 */
@RunWith(RobolectricTestRunner::class)
class PartMinimalTest {

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun stopClock() {
        rule.mainClock.autoAdvance = false
    }

    @Test
    fun explainCardMinimalShowsFinalState() = assertMinimal(PartTypes.EXPLAIN_CARD)

    @Test
    fun keypointCardMinimalShowsFinalState() = assertMinimal(PartTypes.KEYPOINT_CARD)

    @Test
    fun compareCardMinimalShowsFinalState() = assertMinimal(PartTypes.COMPARE_CARD)

    @Test
    fun quizCardMinimalShowsFinalState() = assertMinimal(PartTypes.QUIZ_CARD)

    @Test
    fun verdictCardMinimalShowsFinalState() = assertMinimal(PartTypes.VERDICT_CARD)

    @Test
    fun errorCauseCardMinimalShowsFinalState() = assertMinimal(PartTypes.ERROR_CAUSE_CARD)

    @Test
    fun mapCardMinimalShowsFinalState() = assertMinimal(PartTypes.MAP_CARD)

    @Test
    fun timelineCardMinimalShowsFinalState() = assertMinimal(PartTypes.TIMELINE_CARD)

    @Test
    fun tableCardMinimalShowsFinalState() = assertMinimal(PartTypes.TABLE_CARD)

    @Test
    fun planPanelMinimalShowsFinalState() = assertMinimal(PartTypes.PLAN_PANEL)

    @Test
    fun progressCardMinimalShowsFinalState() = assertMinimal(PartTypes.PROGRESS_CARD)

    @Test
    fun reviewCardMinimalShowsFinalState() = assertMinimal(PartTypes.REVIEW_CARD)

    @Test
    fun diagnoseCardMinimalShowsFinalState() = assertMinimal(PartTypes.DIAGNOSE_CARD)

    @Test
    fun profileConfirmCardMinimalShowsFinalState() = assertMinimal(PartTypes.PROFILE_CONFIRM_CARD)

    @Test
    fun materialCardMinimalShowsFinalState() = assertMinimal(PartTypes.MATERIAL_CARD)

    @Test
    fun toolCardMinimalShowsFinalState() = assertMinimal(PartTypes.TOOL_CARD)

    @Test
    fun verdictCardMinimalKeepsIconAndTextNotColourOnly() {
        rule.setContent {
            EduTheme {
                CompositionLocalProvider(LocalMotionLevel provides MotionLevel.MINIMAL) {
                    PartHost(PartSamples.envelopeFor(PartTypes.VERDICT_CARD))
                }
            }
        }
        // 图标（带 contentDescription 的对错标记）+ 文案同时在场
        rule.onNodeWithText(stringOf(R.string.part_verdict_wrong)).assertIsDisplayed()
        rule.onNodeWithText("Mitochondrion").assertIsDisplayed()
    }

    private fun assertMinimal(type: String) {
        rule.setContent {
            EduTheme {
                CompositionLocalProvider(LocalMotionLevel provides MotionLevel.MINIMAL) {
                    PartHost(PartSamples.envelopeFor(type))
                }
            }
        }
        rule.onNodeWithText(expectedTextOf(type)).assertIsDisplayed()
    }
}
