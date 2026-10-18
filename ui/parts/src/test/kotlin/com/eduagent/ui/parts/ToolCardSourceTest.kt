package com.eduagent.ui.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.ToolCardPayload
import com.eduagent.core.model.part.ToolResult
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4.1 必测 · `ToolCardSourceTest`（红线 R-C1 / R-C2）。
 *
 * `tool_card` 的每条 `results[]` **必须**有 `source`；无来源项**不渲染**。
 */
@RunWith(RobolectricTestRunner::class)
class ToolCardSourceTest {

    @get:Rule
    val rule = createComposeRule()

    /** 无来源的负例条目（见 PartSamples）。 */
    private val noSourceTitle = "NO_SOURCE_ITEM"

    @Test
    fun sampleWithOneUnsourcedResultHidesThatResult() {
        rule.setContent {
            EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.TOOL_CARD)) }
        }
        // 有来源的渲染
        rule.onNodeWithText("Chloroplast - Wikipedia").assertIsDisplayed()
        // 无来源的不渲染
        rule.onNodeWithText(noSourceTitle).assertDoesNotExist()
    }

    @Test
    fun allUnsourcedResultsRenderNothingButTheNotice() {
        val envelope = PartEnvelope(
            v = 1,
            id = "tool-no-source",
            type = PartTypes.TOOL_CARD,
            payload = ToolCardPayload(
                query = "query without sources",
                results = listOf(
                    ToolResult("T1", "https://example.invalid/1", "s1", null),
                    ToolResult("T2", "https://example.invalid/2", "s2", "   "),
                ),
            ),
        )
        rule.setContent { EduTheme { PartHost(envelope) } }
        rule.onNodeWithText("query without sources").assertIsDisplayed()
        rule.onNodeWithText("T1").assertDoesNotExist()
        rule.onNodeWithText("T2").assertDoesNotExist()
        rule.onNodeWithText(stringOf(R.string.part_tool_no_source_notice)).assertIsDisplayed()
    }

    @Test
    fun sourcedResultsRenderWithSourceLabel() {
        val envelope = PartEnvelope(
            v = 1,
            id = "tool-sourced",
            type = PartTypes.TOOL_CARD,
            payload = ToolCardPayload(
                query = "query with sources",
                results = listOf(
                    ToolResult("Title One", "https://example.org/1", "snip", "Source One"),
                ),
            ),
        )
        rule.setContent { EduTheme { PartHost(envelope) } }
        rule.onNodeWithText("Title One").assertIsDisplayed()
        rule.onNodeWithText("Source One").assertIsDisplayed()
    }
}
