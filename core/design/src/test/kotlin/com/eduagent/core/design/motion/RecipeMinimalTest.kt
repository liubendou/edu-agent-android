package com.eduagent.core.design.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 必测用例 3：十条配方各 1 个 minimal 用例，断言「**信息不丢失**」（任务包 05 §4.1 / 契约 V2）。
 *
 * 判定方法（§4.2）：minimal 不是「什么都不发生」，而是「用静态样式表达同一状态」——
 * 动效消失后必须仍能用**文字 / 颜色 / 图标 / 形状 / 数值**分清「对」与「错」、「有」与「无」。
 */
class RecipeMinimalTest {

    @Test
    fun recipe_01_press_rebound_minimal_keeps_button_usable_state() {
        assertMinimal(MotionRecipe.PRESS_REBOUND, InfoCarrier.SHAPE)
    }

    @Test
    fun recipe_02_mode_indicator_slide_minimal_keeps_selected_state_by_color() {
        assertMinimal(MotionRecipe.MODE_INDICATOR_SLIDE, InfoCarrier.COLOR)
    }

    @Test
    fun recipe_03_chip_bounce_minimal_keeps_selected_state_by_fill() {
        assertMinimal(MotionRecipe.CHIP_BOUNCE, InfoCarrier.COLOR)
    }

    @Test
    fun recipe_04_submit_three_dot_minimal_keeps_in_progress_text() {
        assertMinimal(MotionRecipe.SUBMIT_THREE_DOT, InfoCarrier.TEXT)
    }

    @Test
    fun recipe_05_wrong_shake_minimal_keeps_text_color_and_icon() {
        assertMinimal(
            MotionRecipe.WRONG_SHAKE,
            InfoCarrier.TEXT,
            InfoCarrier.COLOR,
            InfoCarrier.ICON,
        )
    }

    @Test
    fun recipe_06_correct_stroke_minimal_keeps_filled_stroke_and_check() {
        assertMinimal(MotionRecipe.CORRECT_STROKE, InfoCarrier.COLOR, InfoCarrier.ICON)
    }

    @Test
    fun recipe_07_thinking_minimal_keeps_static_text() {
        assertMinimal(MotionRecipe.THINKING_WAIT, InfoCarrier.TEXT)
    }

    @Test
    fun recipe_08_top_toast_minimal_keeps_message_visible() {
        assertMinimal(MotionRecipe.TOP_TOAST, InfoCarrier.TEXT, InfoCarrier.SHAPE)
    }

    @Test
    fun recipe_09_review_breath_dot_minimal_keeps_static_solid_dot() {
        assertMinimal(MotionRecipe.REVIEW_BREATH_DOT, InfoCarrier.SHAPE)
    }

    @Test
    fun recipe_10_mastery_growth_minimal_keeps_target_value_visible() {
        assertMinimal(MotionRecipe.MASTERY_GROWTH, InfoCarrier.TEXT, InfoCarrier.VALUE)
    }

    @Test
    fun recipe_enum_matches_contract_order_and_count() {
        assertEquals(
            listOf(
                "PRESS_REBOUND",
                "MODE_INDICATOR_SLIDE",
                "CHIP_BOUNCE",
                "SUBMIT_THREE_DOT",
                "WRONG_SHAKE",
                "CORRECT_STROKE",
                "THINKING_WAIT",
                "TOP_TOAST",
                "REVIEW_BREATH_DOT",
                "MASTERY_GROWTH",
            ),
            MotionRecipe.entries.map { it.name },
        )
        assertEquals(10, Motion.all.size)
    }

    @Test
    fun only_meaningful_recipes_loop_and_none_of_them_loop_off_full_level() {
        val looping = Motion.all.filter { it.looping }.map { it.recipe }
        assertEquals(
            listOf(
                MotionRecipe.SUBMIT_THREE_DOT,
                MotionRecipe.THINKING_WAIT,
                MotionRecipe.REVIEW_BREATH_DOT,
            ),
            looping,
        )
    }

    @Test
    fun every_recipe_exposes_reusable_spec() {
        MotionRecipe.entries.forEach { recipe ->
            val spec = Motion.recipe(recipe)
            assertEquals(recipe, spec.recipe)
            assertTrue("${recipe.name} 必须给出非空 minimal 说明", spec.minimal.note.isNotBlank())
            assertTrue(
                "${recipe.name} 的时长必须是 0 或正整数毫秒",
                spec.durationMs >= 0,
            )
            if (!spec.looping) {
                assertFalse(
                    "${recipe.name} 非循环配方不应出现 0 时长",
                    spec.durationMs == 0,
                )
            }
        }
    }

    private fun assertMinimal(recipe: MotionRecipe, vararg carriers: InfoCarrier) {
        val spec = Motion.recipe(recipe)
        assertTrue("${recipe.name} 的 minimal 必须是静态渲染而不是隐藏", spec.minimal.rendersStatic)
        assertTrue("${recipe.name} 的 minimal 必须有信息载体", spec.minimal.carriers.isNotEmpty())
        carriers.forEach { carrier ->
            assertTrue(
                "${recipe.name} 的 minimal 缺少信息载体 $carrier",
                spec.minimal.carriers.contains(carrier),
            )
        }
        assertTrue("${recipe.name} 的 minimal 说明不得为空", spec.minimal.note.isNotBlank())
    }
}
