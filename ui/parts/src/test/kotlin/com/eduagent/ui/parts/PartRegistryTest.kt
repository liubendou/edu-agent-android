package com.eduagent.ui.parts

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4.1 必测 · `PartRegistryTest`。
 *
 * 断言三条（任务包 06 §4.1）：
 * 1. 16 个 type **全部**命中注册表；
 * 2. 未知 type（`foo_card`）**不抛异常**且触发 `unknown_part` 埋点**一次**；
 * 3. 未知 type 降级为纯文本（不白屏）。
 */
@RunWith(RobolectricTestRunner::class)
class PartRegistryTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `registry lists exactly the 16 frozen types in contract order`() {
        assertEquals(
            listOf(
                "explain_card",
                "keypoint_card",
                "compare_card",
                "quiz_card",
                "verdict_card",
                "error_cause_card",
                "map_card",
                "timeline_card",
                "table_card",
                "plan_panel",
                "progress_card",
                "review_card",
                "diagnose_card",
                "profile_confirm_card",
                "material_card",
                "tool_card",
            ),
            PartRegistry.types,
        )
    }

    @Test
    fun `all 16 frozen types hit the registry without unknown_part`() {
        val unknown = mutableListOf<String>()
        rule.setContent {
            EduTheme {
                CompositionLocalProvider(LocalPartAnalytics provides PartAnalytics { unknown += it }) {
                    Column { PartSamples.all.forEach { envelope -> PartHost(envelope) } }
                }
            }
        }
        rule.onRoot().assertExists()
        assertEquals(emptyList<String>(), unknown)
        assertEquals(16, PartSamples.all.size)
    }

    @Test
    fun `unknown type does not throw and emits unknown_part exactly once`() {
        val unknown = mutableListOf<String>()
        rule.setContent {
            EduTheme {
                CompositionLocalProvider(LocalPartAnalytics provides PartAnalytics { unknown += it }) {
                    PartHost(PartSamples.unknown("foo_card"))
                }
            }
        }
        rule.onRoot().assertExists()
        assertEquals(listOf("foo_card"), unknown)
    }

    @Test
    fun `unknown type degrades to plain text without crashing`() {
        rule.setContent { EduTheme { PartHost(PartSamples.unknown("foo_card")) } }
        rule.onNodeWithText(stringOf(R.string.part_fallback_notice)).assertExists()
    }

    @Test
    fun `missing required field degrades to plain text without crashing`() {
        val broken = PartSamples.envelopeFor(PartTypes.KEYPOINT_CARD).let { envelope ->
            com.eduagent.core.model.part.PartEnvelope(
                v = envelope.v,
                id = envelope.id,
                type = envelope.type,
                payload = com.eduagent.core.model.part.KeypointCardPayload(title = " ", points = emptyList()),
            )
        }
        rule.setContent { EduTheme { PartHost(broken) } }
        rule.onNodeWithText(stringOf(R.string.part_fallback_notice)).assertExists()
    }

    @Test
    fun `composableFor returns a renderer for every frozen type`() {
        // 16 个 type 都能取到渲染器（未知 type 也会返回降级渲染器，故此处只断言集合完备）
        assertTrue(PartRegistry.types.all { PartRegistry.composableFor(it) != null })
        assertEquals("unknown_part", PartRegistry.UNKNOWN_PART_KEY)
    }
}
