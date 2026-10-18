package com.eduagent.core.design.motion

import com.eduagent.core.design.MotionTokensJson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 必测用例 1：动效 tokens（任务包 05 §4.1）。
 *
 * 断言 4 时长（ms）与 3 弹簧，**以原生 `SpringSpec(stiffness, dampingRatio)` 参数比对**，
 * 并交叉验证 `assets/contract/motion-tokens.json` 的冻结值（禁 build cache 也得一致）。
 */
class MotionTokensTest {

    @Test
    fun durations_match_frozen_values_in_milliseconds() {
        assertEquals(120, MotionTokens.microMs)
        assertEquals(200, MotionTokens.baseMs)
        assertEquals(600, MotionTokens.slowMs)
        assertEquals(800, MotionTokens.drawMs)
    }

    @Test
    fun duration_tokens_expose_same_values_as_milliseconds() {
        assertEquals(MotionTokens.microMs, MotionTokens.micro.inWholeMilliseconds.toInt())
        assertEquals(MotionTokens.baseMs, MotionTokens.base.inWholeMilliseconds.toInt())
        assertEquals(MotionTokens.slowMs, MotionTokens.slow.inWholeMilliseconds.toInt())
        assertEquals(MotionTokens.drawMs, MotionTokens.draw.inWholeMilliseconds.toInt())
    }

    @Test
    fun springs_match_frozen_native_parameters() {
        assertEquals(380f, MotionTokens.snappy.stiffness, 0.0f)
        assertEquals(30f, MotionTokens.snappy.dampingRatio, 0.0f)

        assertEquals(260f, MotionTokens.default.stiffness, 0.0f)
        assertEquals(30f, MotionTokens.default.dampingRatio, 0.0f)

        assertEquals(260f, MotionTokens.soft.stiffness, 0.0f)
        assertEquals(26f, MotionTokens.soft.dampingRatio, 0.0f)
    }

    @Test
    fun durations_match_contract_json() {
        assertEquals(MotionTokensJson.duration("micro"), MotionTokens.microMs)
        assertEquals(MotionTokensJson.duration("base"), MotionTokens.baseMs)
        assertEquals(MotionTokensJson.duration("slow"), MotionTokens.slowMs)
        assertEquals(MotionTokensJson.duration("draw"), MotionTokens.drawMs)
    }

    @Test
    fun springs_match_contract_json() {
        assertSpringMatchesJson("snappy", MotionTokens.snappy)
        assertSpringMatchesJson("default", MotionTokens.default)
        assertSpringMatchesJson("soft", MotionTokens.soft)
    }

    private fun assertSpringMatchesJson(name: String, spec: androidx.compose.animation.core.SpringSpec<Float>) {
        val (stiffness, dampingRatio) = MotionTokensJson.spring(name)
        assertEquals("$name stiffness", stiffness, spec.stiffness, 0.0f)
        assertEquals("$name dampingRatio", dampingRatio, spec.dampingRatio, 0.0f)
    }
}
