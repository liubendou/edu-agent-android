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
 * §4.1 必测 · `PartRenderTest`×16。
 *
 * 每个零件 1 个渲染测试：给定合法 payload 可组合成功、**无崩溃、无白屏**（K1.1）。
 *
 * 纪律：`mainClock.autoAdvance = false` —— 零件内可能复用 `:core:design` 的循环动效
 * （如复习卡呼吸点），关闭时钟自动推进可避免测试等待动画而挂起。
 */
@RunWith(RobolectricTestRunner::class)
class PartRenderTest {

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun stopClock() {
        rule.mainClock.autoAdvance = false
    }

    @Test
    fun explainCardRenders() = assertRendered(PartTypes.EXPLAIN_CARD)

    @Test
    fun keypointCardRenders() = assertRendered(PartTypes.KEYPOINT_CARD)

    @Test
    fun compareCardRenders() = assertRendered(PartTypes.COMPARE_CARD)

    @Test
    fun quizCardRenders() = assertRendered(PartTypes.QUIZ_CARD)

    @Test
    fun verdictCardRenders() = assertRendered(PartTypes.VERDICT_CARD)

    @Test
    fun errorCauseCardRenders() = assertRendered(PartTypes.ERROR_CAUSE_CARD)

    @Test
    fun mapCardRenders() = assertRendered(PartTypes.MAP_CARD)

    @Test
    fun timelineCardRenders() = assertRendered(PartTypes.TIMELINE_CARD)

    @Test
    fun tableCardRenders() = assertRendered(PartTypes.TABLE_CARD)

    @Test
    fun planPanelRenders() = assertRendered(PartTypes.PLAN_PANEL)

    @Test
    fun progressCardRenders() = assertRendered(PartTypes.PROGRESS_CARD)

    @Test
    fun reviewCardRenders() = assertRendered(PartTypes.REVIEW_CARD)

    @Test
    fun diagnoseCardRenders() = assertRendered(PartTypes.DIAGNOSE_CARD)

    @Test
    fun profileConfirmCardRenders() = assertRendered(PartTypes.PROFILE_CONFIRM_CARD)

    @Test
    fun materialCardRenders() = assertRendered(PartTypes.MATERIAL_CARD)

    @Test
    fun toolCardRenders() = assertRendered(PartTypes.TOOL_CARD)

    private fun assertRendered(type: String) {
        rule.setContent {
            EduTheme {
                CompositionLocalProvider(LocalMotionLevel provides MotionLevel.FULL) {
                    PartHost(PartSamples.envelopeFor(type))
                }
            }
        }
        rule.onNodeWithText(expectedTextOf(type)).assertIsDisplayed()
    }
}
