package com.eduagent.tools.contract

import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 反例：删掉一个零件 type 后，校验必须报红。
 * 对应 04-任务包/01-契约冻结与版本锁定.md §4.2「反例测试」。
 */
class ContractCheckNegativeTest {

    @Test
    fun `删掉一个零件 type 后校验必须失败`() {
        val src = ContractPaths.contractDir
        val tmp = Files.createTempDirectory("contract-negative").toFile()
        try {
            src.listFiles()?.filter { it.isFile }?.forEach { it.copyTo(File(tmp, it.name), overwrite = true) }

            val original = File(tmp, "parts.json")
            val root = Json.parseToJsonElement(original.readText()).jsonObject
            val before = (root["parts"] as JsonArray).size
            val kept = (root["parts"] as JsonArray).filterNot {
                it.jsonObject["type"]?.jsonPrimitive?.content == "tool_card"
            }
            original.writeText(JsonObject(root + ("parts" to JsonArray(kept))).toString())
            println("[negative] 破坏注入：parts 条数 $before -> ${kept.size}（删除 tool_card）")

            val report = ContractValidator.validate(tmp, ContractPaths.versionsToml)
            report.violations.forEach { println("[negative] violation: $it") }
            println("[negative] ok=${report.ok} violations=${report.violations.size}")

            assertFalse("删掉一个零件 type 后，校验必须报红", report.ok)
            assertTrue(
                "应产生「零件条数/type 集合不一致」违规",
                report.violations.any {
                    it.rule == "parts.json" && (it.detail.contains("零件条数") || it.detail.contains("type 集合"))
                },
            )
        } finally {
            tmp.deleteRecursively()
        }
    }
}
