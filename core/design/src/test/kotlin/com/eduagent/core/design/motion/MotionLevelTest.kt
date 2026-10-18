package com.eduagent.core.design.motion

import com.eduagent.core.design.MotionTokensJson
import com.eduagent.core.design.TestPaths
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 必测用例 2：三档动效纪律（任务包 05 §4.1 / 契约 §8.3 / D27 / M4）。
 *
 * 判定被抽成纯函数 [MotionLevel.resolve]，故此处可**纯 JVM 全覆盖**：
 * 系统移除动画 > 用户覆盖 > 低端机降档 > 默认 full。
 */
class MotionLevelTest {

    @Test
    fun system_removal_animation_forces_minimal() {
        val state = MotionLevel.resolve(
            systemScaleIsZero = true,
            userOverride = null,
            deviceTier = DeviceTier.STANDARD,
        )
        assertEquals(MotionLevel.MINIMAL, state.level)
        assertTrue(state.systemForced)
    }

    @Test
    fun system_minimal_ignores_any_user_override_and_blocks_reverse_override() {
        listOf(null, MotionLevel.FULL, MotionLevel.REDUCED, MotionLevel.MINIMAL).forEach { override ->
            val state = MotionLevel.resolve(
                systemScaleIsZero = true,
                userOverride = override,
                deviceTier = DeviceTier.STANDARD,
            )
            assertEquals(MotionLevel.MINIMAL, state.level)
            assertFalse("系统 minimal 时必须封死反向覆盖入口", state.canUserOverride)
        }
    }

    @Test
    fun user_override_takes_effect_when_system_allows_animation() {
        MotionLevel.entries.forEach { override ->
            val state = MotionLevel.resolve(
                systemScaleIsZero = false,
                userOverride = override,
                deviceTier = DeviceTier.STANDARD,
            )
            assertEquals(override, state.level)
            assertFalse(state.systemForced)
            assertTrue(state.canUserOverride)
        }
    }

    @Test
    fun low_end_device_downgrades_to_reduced_when_user_has_no_override() {
        val state = MotionLevel.resolve(
            systemScaleIsZero = false,
            userOverride = null,
            deviceTier = DeviceTier.LOW,
        )
        assertEquals(MotionLevel.REDUCED, state.level)
        assertFalse(state.systemForced)
        assertTrue("低端机降档是软降档，用户仍可显式覆盖", state.canUserOverride)
    }

    @Test
    fun standard_device_defaults_to_full() {
        val state = MotionLevel.resolve(
            systemScaleIsZero = false,
            userOverride = null,
            deviceTier = DeviceTier.STANDARD,
        )
        assertEquals(MotionLevel.FULL, state.level)
        assertFalse(state.systemForced)
        assertTrue(state.canUserOverride)
    }

    @Test
    fun low_end_tier_does_not_override_user_choice() {
        val state = MotionLevel.resolve(
            systemScaleIsZero = false,
            userOverride = MotionLevel.FULL,
            deviceTier = DeviceTier.LOW,
        )
        assertEquals(MotionLevel.FULL, state.level)
    }

    @Test
    fun reduced_removes_translation_and_looping_but_keeps_short_fade() {
        assertTrue(MotionLevel.REDUCED.allowsFade)
        assertFalse(MotionLevel.REDUCED.allowsTranslation)
        assertFalse(MotionLevel.REDUCED.allowsLooping)
    }

    @Test
    fun minimal_is_fully_static_but_not_hidden() {
        assertFalse(MotionLevel.MINIMAL.allowsFade)
        assertFalse(MotionLevel.MINIMAL.allowsTranslation)
        assertFalse(MotionLevel.MINIMAL.allowsLooping)
    }

    @Test
    fun fade_duration_is_clamped_to_micro_in_reduced_and_zero_in_minimal() {
        assertEquals(600, MotionLevel.FULL.fadeDurationMs(600))
        assertEquals(120, MotionLevel.REDUCED.fadeDurationMs(600))
        assertEquals(120, MotionLevel.REDUCED.fadeDurationMs(120))
        assertEquals(0, MotionLevel.MINIMAL.fadeDurationMs(600))
    }

    @Test
    fun level_enum_matches_contract_json_order() {
        assertEquals(listOf("full", "reduced", "minimal"), MotionTokensJson.levelIds())
        assertEquals(
            listOf("FULL", "REDUCED", "MINIMAL"),
            MotionLevel.entries.map { it.name },
        )
    }

    /**
     * D27 的读取路径必须指向系统「移除动画」的真实键。
     *
     * 契约 §14.7 原文写的是 `LocalMotionDurationScale.current == 0f`；该 CompositionLocal 在冻结的
     * Compose 1.12.1 中已移除（见 docs/06 报告的待裁决项），故改读其唯一上游
     * `Settings.Global.ANIMATOR_DURATION_SCALE`。此断言把「读取路径存在且指向该键」固化进机检。
     */
    @Test
    fun system_removal_animation_read_path_targets_animator_duration_scale() {
        val source = TestPaths.moduleFile(
            "src/main/kotlin/com/eduagent/core/design/motion/MotionLevel.kt",
        ).readText()
        assertTrue(
            "MotionLevel.kt 必须从 Settings.Global.ANIMATOR_DURATION_SCALE 读系统移除动画",
            source.contains("Settings.Global.ANIMATOR_DURATION_SCALE"),
        )
        assertTrue(
            "MotionLevel.resolve 必须把 systemScaleIsZero 作为最高优先级输入",
            source.contains("systemScaleIsZero"),
        )
    }
}
