package com.eduagent.core.design.motion

import com.eduagent.core.design.MotionTokensJson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 必测用例 4：五档 tau（任务包 05 §4.1 / 契约 §8.5 / D29）。
 *
 * 五档值逐字：0.18 / 0.25 / 0.35 / 0.15 / 0.30，并与冻结 JSON 交叉验证。
 */
class StreamTauTest {

    @Test
    fun five_taus_match_frozen_values() {
        assertEquals(0.18f, StreamTau.QUICK_SECONDS, 0.0f)
        assertEquals(0.25f, StreamTau.STANDARD_SECONDS, 0.0f)
        assertEquals(0.35f, StreamTau.DEEP_SECONDS, 0.0f)
        assertEquals(0.15f, StreamTau.REVIEW_SECONDS, 0.0f)
        assertEquals(0.30f, StreamTau.BOOST_SECONDS, 0.0f)
    }

    @Test
    fun taus_match_contract_json() {
        assertEquals(MotionTokensJson.tau("quick"), StreamTau.QUICK_SECONDS, 0.0f)
        assertEquals(MotionTokensJson.tau("standard"), StreamTau.STANDARD_SECONDS, 0.0f)
        assertEquals(MotionTokensJson.tau("deep"), StreamTau.DEEP_SECONDS, 0.0f)
        assertEquals(MotionTokensJson.tau("review"), StreamTau.REVIEW_SECONDS, 0.0f)
        assertEquals(MotionTokensJson.tau("boost"), StreamTau.BOOST_SECONDS, 0.0f)
    }

    @Test
    fun mode_lookup_is_case_insensitive_and_covers_five_modes() {
        assertEquals(StreamTau.QUICK_SECONDS, StreamTau.forMode("quick"), 0.0f)
        assertEquals(StreamTau.STANDARD_SECONDS, StreamTau.forMode("Standard"), 0.0f)
        assertEquals(StreamTau.DEEP_SECONDS, StreamTau.forMode("DEEP"), 0.0f)
        assertEquals(StreamTau.REVIEW_SECONDS, StreamTau.forMode("review"), 0.0f)
        assertEquals(StreamTau.BOOST_SECONDS, StreamTau.forMode("boost"), 0.0f)
    }

    @Test
    fun unknown_mode_falls_back_to_standard() {
        assertEquals(StreamTau.STANDARD_SECONDS, StreamTau.forMode("plaintext"), 0.0f)
        assertEquals(StreamTau.STANDARD_SECONDS, StreamTau.forMode(""), 0.0f)
    }
}
