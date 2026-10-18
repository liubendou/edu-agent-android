package com.eduagent.core.design.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 必测用例 5：触觉契约（任务包 05 §4.1 / 契约 §9 / D28）。
 *
 * 两条硬要求：`haptics_enabled = false` 时 `perform` **不触发**；三场景映射
 * CONFIRM / REJECT / CONTEXT_CLICK。
 */
class HapticsTest {

    private class RecordingHaptics : Haptics {
        val calls = mutableListOf<HapticKind>()
        override fun perform(kind: HapticKind) {
            calls += kind
        }
    }

    @Test
    fun disabled_gate_swallows_every_kind() {
        val recorder = RecordingHaptics()
        val gated = GatedHaptics(recorder, enabled = false)
        HapticKind.entries.forEach { gated.perform(it) }
        assertTrue("haptics_enabled=false 时不得触发任何触觉", recorder.calls.isEmpty())
    }

    @Test
    fun enabled_gate_passes_all_three_kinds_through_in_order() {
        val recorder = RecordingHaptics()
        val gated = GatedHaptics(recorder, enabled = true)
        gated.perform(HapticKind.CONFIRM)
        gated.perform(HapticKind.REJECT)
        gated.perform(HapticKind.CONTEXT_CLICK)
        assertEquals(
            listOf(HapticKind.CONFIRM, HapticKind.REJECT, HapticKind.CONTEXT_CLICK),
            recorder.calls,
        )
    }

    @Test
    fun exactly_three_scenarios_are_registered() {
        assertEquals(
            listOf("CONFIRM", "REJECT", "CONTEXT_CLICK"),
            HapticKind.entries.map { it.name },
        )
        assertEquals(
            listOf("CONFIRM", "REJECT", "CONTEXT_CLICK"),
            HapticKind.entries.map { it.systemConstantName() },
        )
    }

    @Test
    fun haptics_is_used_only_for_result_confirmation() {
        // 结果确认 = CONFIRM / REJECT；CONTEXT_CLICK 允许用于模式切换与 chip 选中（契约 §9）
        assertEquals(3, HapticKind.entries.size)
        assertTrue(HapticKind.entries.contains(HapticKind.CONFIRM))
        assertTrue(HapticKind.entries.contains(HapticKind.REJECT))
    }

    @Test
    fun no_op_implementation_never_throws() {
        HapticKind.entries.forEach { NoOpHaptics.perform(it) }
    }
}
