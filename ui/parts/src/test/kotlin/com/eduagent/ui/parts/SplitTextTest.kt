package com.eduagent.ui.parts

import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.BlockKind
import com.eduagent.core.model.part.CompareCardPayload
import com.eduagent.core.model.part.ExplainBlock
import com.eduagent.core.model.part.ExplainCardPayload
import com.eduagent.core.model.part.KeyPoint
import com.eduagent.core.model.part.KeypointCardPayload
import com.eduagent.core.model.part.PartTextSplitter
import com.eduagent.core.model.part.TableCardPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §4.1 必测 · `SplitTextTest`（契约 §4 硬规则）。
 *
 * `explain_card` / `keypoint_card` / `compare_card` / `table_card` 的正文 **>1200 字时必须拆分**
 * 成多张同型零件；拆分器见 `:core:model` 的 `PartTextSplitter`（纯 Kotlin，端云共用）。
 */
class SplitTextTest {

    private val limit = PartTextSplitter.TEXT_LIMIT

    /** 约 2480 字，明显越过 1200 上限。 */
    private val longBody: String = "Sentence about cells and energy. ".repeat(80)

    private val shortBody = "Short enough."

    init {
        assertTrue(longBody.length > limit)
    }

    @Test
    fun longTextIsSplitIntoMultipleChunksWithinLimit() {
        val chunks = PartTextSplitter.split(longBody)
        assertTrue(chunks.size > 1)
        chunks.forEach { chunk -> assertTrue("chunk too long: ${chunk.length}", chunk.length <= limit) }
        assertEquals(longBody, chunks.joinToString(""))
    }

    @Test
    fun shortTextStaysInOneChunk() {
        assertEquals(listOf(shortBody), PartTextSplitter.split(shortBody))
    }

    @Test
    fun emptyTextStaysInOneEmptyChunk() {
        assertEquals(listOf(""), PartTextSplitter.split(""))
    }

    @Test
    fun textWithoutSentenceBreaksIsStillSplit() {
        val solid = "x".repeat(limit * 2 + 5)
        val chunks = PartTextSplitter.split(solid)
        assertTrue(chunks.size >= 3)
        chunks.forEach { chunk -> assertTrue(chunk.length <= limit) }
        assertEquals(solid, chunks.joinToString(""))
    }

    @Test
    fun explainCardBodyOverLimitIsSplit() {
        val payload = ExplainCardPayload(
            title = "Long explanation",
            blocks = listOf(ExplainBlock(kind = BlockKind.TEXT, body = longBody)),
        )
        assertLongTextSplits(payload.blocks.first().body.orEmpty(), PartTypes.EXPLAIN_CARD)
    }

    @Test
    fun keypointCardBodyOverLimitIsSplit() {
        val payload = KeypointCardPayload(
            title = "Long keypoints",
            points = listOf(KeyPoint(text = longBody, level = 1)),
        )
        assertLongTextSplits(payload.points.first().text, PartTypes.KEYPOINT_CARD)
    }

    @Test
    fun compareCardBodyOverLimitIsSplit() {
        val payload = CompareCardPayload(
            title = "Long comparison",
            columns = listOf("A", "B"),
            rows = listOf(listOf(longBody, shortBody)),
        )
        assertLongTextSplits(payload.rows.first().first(), PartTypes.COMPARE_CARD)
    }

    @Test
    fun tableCardBodyOverLimitIsSplit() {
        val payload = TableCardPayload(
            title = "Long table",
            headers = listOf("H1", "H2"),
            rows = listOf(listOf(longBody, shortBody)),
        )
        assertLongTextSplits(payload.rows.first().first(), PartTypes.TABLE_CARD)
    }

    @Test
    fun nonPositiveLimitIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { PartTextSplitter.split(longBody, 0) }
    }

    private fun assertLongTextSplits(body: String, type: String) {
        val chunks = PartTextSplitter.split(body)
        assertTrue("$type should split into multiple parts", chunks.size > 1)
        chunks.forEach { chunk ->
            assertTrue("$type chunk exceeds limit", chunk.length <= limit)
        }
        assertEquals("$type text must not lose characters", body, chunks.joinToString(""))
    }
}
