package com.eduagent.tools.contract

import com.eduagent.core.model.CatalogVersion
import com.eduagent.core.model.PartTypes
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 第 2 步「三处一致性」第 1、2 处机器校验（红线 R-T3 / R-T8 的基础设施）：
 *
 *   处 2（端侧常量）  assets/contract/parts.json 的 16 个 `type`  ≡  :core:model 的 PartTypes 常量集合
 *   处 3（目录版本）  assets/contract/parts.json 的 `catalog_version`  ≡  CatalogVersion.CATALOG_VERSION
 *
 * 第 1 处（云侧 CATALOG 段）由第 9 步接入（契约 §6 字节稳定铁律第 3 条）。
 *
 * 关键约束：本测试**必须能失败**——临时删掉 PartTypes 的任一常量，`contractCheck` 即应报红。
 */
class PartTypesConsistencyTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun partsJson() = File(ContractPaths.contractDir, "parts.json")
        .let { file ->
            assertTrue("parts.json 不存在：${file.absolutePath}", file.isFile)
            json.parseToJsonElement(file.readText()).jsonObject
        }

    @Test
    fun `parts_json 的 16 个 type 与 PartTypes 常量逐一一致`() {
        val jsonTypes = (partsJson()["parts"] as JsonArray)
            .map { it.jsonObject["type"]!!.jsonPrimitive.content }
        val kotlinTypes = PartTypes.ALL

        println("[三处一致性·处2/端侧常量] parts.json   = $jsonTypes")
        println("[三处一致性·处2/端侧常量] PartTypes.kt = $kotlinTypes")

        assertEquals("parts.json 的零件数应为 16", 16, jsonTypes.size)
        assertEquals("PartTypes 常量数应为 16", 16, kotlinTypes.size)
        assertEquals(
            "parts.json 与 :core:model 的 PartTypes 不一致（红线 R-T3：新增零件必须三处同改）",
            jsonTypes,
            kotlinTypes,
        )

        val onlyInJson = jsonTypes.toSet() - kotlinTypes.toSet()
        val onlyInKotlin = kotlinTypes.toSet() - jsonTypes.toSet()
        assertTrue("仅存在于 parts.json 的 type：$onlyInJson", onlyInJson.isEmpty())
        assertTrue("仅存在于 PartTypes 的常量：$onlyInKotlin", onlyInKotlin.isEmpty())
    }

    @Test
    fun `parts_json 的 catalog_version 与 CatalogVersion 常量一致`() {
        val jsonVersion = partsJson()["catalog_version"]!!.jsonPrimitive.intOrNull
        println(
            "[三处一致性·R-T8] parts.json.catalog_version=$jsonVersion " +
                "CatalogVersion.CATALOG_VERSION=${CatalogVersion.CATALOG_VERSION}",
        )
        assertEquals(
            "catalog_version 必须等于 com.eduagent.core.model.CatalogVersion.CATALOG_VERSION（红线 R-T8）",
            CatalogVersion.CATALOG_VERSION,
            jsonVersion,
        )
    }

    @Test
    fun `PartTypes 常量为小写 snake_case 且无重复`() {
        val types = PartTypes.ALL
        assertEquals("常量不得重复", types.size, types.toSet().size)
        types.forEach { type ->
            assertTrue("type 必须是小写 snake_case：$type", Regex("^[a-z][a-z0-9_]*$").matches(type))
        }
    }
}
