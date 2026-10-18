package com.eduagent.ui.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.QuizCardPayload
import com.eduagent.core.model.part.QuizKind
import com.eduagent.core.model.part.VerdictCardPayload
import com.eduagent.core.model.part.VerdictResult
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4.1 必测 · `QuizVerdictPairTest`。
 *
 * 契约 §4 硬规则：`quiz_card` 与 `verdict_card` 必须**成对出现**；`answer` 由端侧保管，
 * **判分前不下发给 UI**。
 *
 * 实现口径（任务包 06 §7-C）：`QuizCardPayload` **物理不含** `answer` 字段 ——
 * 该规则由类型系统强制（本测试用「哨兵答案」验证 UI 树里绝无泄漏）。
 */
@RunWith(RobolectricTestRunner::class)
class QuizVerdictPairTest {

    @get:Rule
    val rule = createComposeRule()

    /** 一个只存在于 `answer` 的哨兵串：若出现在 UI 树中即为泄漏。 */
    private val answerSentinel = "ANSWER_LEAK_SENTINEL_9F3"

    @Test
    fun quizPayloadHasNoAnswerFieldByType() {
        // 编译期即证明：QuizCardPayload 的构造参数里没有 answer（下方构造只传 4 个字段）
        val payload = QuizCardPayload(
            stem = "Stem",
            kind = QuizKind.OPEN,
            options = emptyList(),
            explain = "Explain",
        )
        assertFalse(
            QuizCardPayload::class.java.declaredFields.any { field -> field.name == "answer" },
        )
        // verdict_card 才持有 answer（判分后呈现）
        val verdict = VerdictCardPayload(
            result = VerdictResult.CORRECT,
            userAnswer = payload.stem,
            answer = answerSentinel,
            explain = payload.explain,
        )
        assertFalse(verdict.answer.isBlank())
    }

    @Test
    fun quizCardDoesNotRenderAnswerBeforeJudging() {
        val envelope = PartEnvelope(
            v = 1,
            id = "quiz-under-test",
            type = PartTypes.QUIZ_CARD,
            payload = QuizCardPayload(
                stem = "Which option is correct?",
                kind = QuizKind.CHOICE,
                options = listOf("Option A", "Option B"),
                explain = "Plain explanation",
            ),
        )
        rule.setContent { EduTheme { PartHost(envelope) } }
        rule.onNodeWithText("Which option is correct?").assertIsDisplayed()
        rule.onNodeWithText(answerSentinel).assertDoesNotExist()
    }

    @Test
    fun verdictCardRendersAnswerAfterJudging() {
        val envelope = PartEnvelope(
            v = 1,
            id = "verdict-under-test",
            type = PartTypes.VERDICT_CARD,
            payload = VerdictCardPayload(
                result = VerdictResult.WRONG,
                userAnswer = "Option A",
                answer = "Option B",
                explain = "Because options are ordered",
            ),
        )
        rule.setContent { EduTheme { PartHost(envelope) } }
        rule.onNodeWithText("Option B").assertIsDisplayed()
    }

    @Test
    fun quizAndVerdictAppearPairwiseInTheFrozenCatalog() {
        // §5 standard/deep 的必需命中都含 quiz_card + verdict_card；注册表两者齐备
        assertTrue(PartRegistry.types.contains("quiz_card"))
        assertTrue(PartRegistry.types.contains("verdict_card"))
        assertTrue(PartSamples.all.any { it.type == PartTypes.QUIZ_CARD })
        assertTrue(PartSamples.all.any { it.type == PartTypes.VERDICT_CARD })
    }

    @Test
    fun quizCardRendersUnderPairComposition() {
        // 成对渲染（quiz 在前、verdict 在后，与 §5 emit_order 同序）可组合成功。
        // 用专属哨兵串，避免样品文案在两张卡里重复出现导致节点歧义。
        val quizStem = "PAIR_QUIZ_STEM_UNIQUE"
        val verdictAnswer = "PAIR_VERDICT_ANSWER_UNIQUE"
        val quiz = PartEnvelope(
            v = 1,
            id = "pair-quiz",
            type = PartTypes.QUIZ_CARD,
            payload = QuizCardPayload(
                stem = quizStem,
                kind = QuizKind.OPEN,
                options = emptyList(),
                explain = "PAIR_QUIZ_EXPLAIN",
            ),
        )
        val verdict = PartEnvelope(
            v = 1,
            id = "pair-verdict",
            type = PartTypes.VERDICT_CARD,
            payload = VerdictCardPayload(
                result = VerdictResult.CORRECT,
                userAnswer = "PAIR_USER_ANSWER",
                answer = verdictAnswer,
                explain = "PAIR_VERDICT_EXPLAIN",
            ),
        )
        rule.setContent {
            EduTheme {
                PartHost(quiz)
                PartHost(verdict)
            }
        }
        rule.onNodeWithText(quizStem).assertIsDisplayed()
        rule.onNodeWithText(verdictAnswer).assertIsDisplayed()
    }
}
