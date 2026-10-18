package com.eduagent.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 第 2 步占位测试：验证 :core:model 骨架可编译、可测，并锁定本步两条红线常量
 * （R-T7 默认模型、R-T8 目录版本）。零件目录与 parts.json 的一致性断言在 :tools:contract-check。
 */
class CoreModelPlaceholderTest {

    @Test
    fun `目录版本常量为 1`() {
        assertEquals(1, CatalogVersion.CATALOG_VERSION)
    }

    @Test
    fun `默认模型必须是 Flash 不得是 Pro`() {
        assertEquals(ModelPreference.FLASH, DefaultModel)
    }

    @Test
    fun `PartTypes 恰好 16 个常量且无重复`() {
        assertEquals(16, PartTypes.ALL.size)
        assertEquals(16, PartTypes.ALL.toSet().size)
    }
}
