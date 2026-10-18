package com.eduagent.tools.contract

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 契约校验器（第 1 步机检实现）。
 * 契约依据：01-契约.md §3~§6、§8、§11、§12、§13；04-任务包/01-契约冻结与版本锁定.md §4.1。
 *
 * 只读 assets/contract 目录下的 *.json 与 gradle/libs.versions.toml，不做任何写操作。
 */
class ContractViolation(val rule: String, val detail: String) {
    override fun toString(): String = "[$rule] $detail"
}

class ContractReport(val evidence: List<String>, val violations: List<ContractViolation>) {
    val ok: Boolean get() = violations.isEmpty()

    fun render(): String = buildString {
        appendLine("---- 契约校验证据 ----")
        evidence.forEach { appendLine("  · $it") }
        if (ok) {
            appendLine("结论：PASS（0 项违规）")
        } else {
            appendLine("结论：FAIL（${violations.size} 项违规）")
            violations.forEach { appendLine("  × $it") }
        }
    }
}

object ContractValidator {

    /** 契约 §7 交接接口 6：端侧目录版本号 */
    const val CATALOG_VERSION = 1

    /** 契约 §4：16 个零件 type（逐字，含大小写与下划线） */
    val EXPECTED_PART_TYPES: List<String> = listOf(
        "explain_card", "keypoint_card", "compare_card", "quiz_card", "verdict_card",
        "error_cause_card", "map_card", "timeline_card", "table_card", "plan_panel",
        "progress_card", "review_card", "diagnose_card", "profile_confirm_card",
        "material_card", "tool_card",
    )

    /** 契约 §5：5 个 mode */
    val EXPECTED_MODES: List<String> = listOf("quick", "standard", "deep", "review", "boost")

    /** 契约 §6：8 段枚举顺序 + 断点位置 */
    val EXPECTED_SEGMENTS: List<String> =
        listOf("SYSTEM", "PSY_INDEX", "CATALOG", "SKILL", "PROFILE", "TASK", "HISTORY", "RETRIEVAL")
    const val BREAKPOINT_AFTER = 3

    /** 契约 §11：红线分组与其条目数（用于「无空缺」连续性校验） */
    val REDLINE_GROUPS: Map<String, Int> = linkedMapOf(
        "R-P" to 7, "R-T" to 10, "R-C" to 7, "R-E" to 6, "R-D" to 4, "D" to 9, "M" to 6,
    )
    val REQUIRED_REDLINE_IDS: List<String> = listOf("R-P7", "R-T10", "D9")
    private val REDLINE_ID_REGEX = Regex("^(R-[PTCRED]|D|M)(\\d+)$")
    private val ENFORCEMENT_VALUES = setOf("machine", "human")

    /** 契约 §12：验收索引必须出现的 ID */
    val REQUIRED_ACCEPTANCE_IDS: List<String> =
        listOf("V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "S1.8", "K1.5", "X1.1", "P3.1")
    private val LEVEL_VALUES = setOf("P0", "P1", "P2")

    /** 契约 §13：D1~D33 */
    const val DECISION_COUNT = 33

    /** 契约 §8.1 / §8.2 / §8.5 */
    val EXPECTED_DURATIONS: Map<String, Int> =
        linkedMapOf("micro" to 120, "base" to 200, "slow" to 600, "draw" to 800)
    val EXPECTED_SPRINGS: Map<String, Pair<Int, Int>> = linkedMapOf(
        "snappy" to (380 to 30), "default" to (260 to 30), "soft" to (260 to 26),
    )
    val EXPECTED_TAU: Map<String, Double> = linkedMapOf(
        "quick" to 0.18, "standard" to 0.25, "deep" to 0.35, "review" to 0.15, "boost" to 0.30,
    )
    const val RECIPE_COUNT = 10

    /** 00-总纲 §二：三类库 6 个坐标（版本逐字） */
    private val REQUIRED_LIB_RULES: List<Pair<String, Regex>> = listOf(
        "material3-adaptive 1.3.0" to
            Regex("""material3Adaptive\s*=\s*"1\.3\.0"[\s\S]*?androidx\.compose\.material3\.adaptive:adaptive"""),
        "vico compose-m3 3.3.1" to
            Regex("""vico\s*=\s*"3\.3\.1"[\s\S]*?com\.patrykandpatrick\.vico:compose-m3"""),
        "compose-shimmer 1.5.0" to
            Regex("""composeShimmer\s*=\s*"1\.5\.0"[\s\S]*?com\.valentinilk\.shimmer:compose-shimmer"""),
        "constraintlayout-compose 1.1.2" to
            Regex("""constraintlayoutCompose\s*=\s*"1\.1\.2"[\s\S]*?androidx\.constraintlayout:constraintlayout-compose"""),
        "lottie-compose 6.6.6" to
            Regex("""lottie\s*=\s*"6\.6\.6"[\s\S]*?com\.airbnb\.android:lottie-compose"""),
        "compose-animation（BOM 管，不写 version）" to
            Regex("""androidx-compose-animation\s*=\s*\{\s*module\s*=\s*"androidx\.compose\.animation:animation"\s*\}"""),
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun validate(contractDir: File, versionsToml: File): ContractReport {
        val violations = mutableListOf<ContractViolation>()
        val evidence = mutableListOf<String>()

        val parts = readObject(File(contractDir, "parts.json"), violations) ?: return ContractReport(evidence, violations)
        val matrix = readObject(File(contractDir, "signature-matrix.json"), violations) ?: return ContractReport(evidence, violations)
        val segments = readObject(File(contractDir, "context-segments.json"), violations) ?: return ContractReport(evidence, violations)
        val redlines = readObject(File(contractDir, "redlines.json"), violations) ?: return ContractReport(evidence, violations)
        val acceptance = readObject(File(contractDir, "acceptance.json"), violations) ?: return ContractReport(evidence, violations)
        val decisions = readObject(File(contractDir, "decisions.json"), violations) ?: return ContractReport(evidence, violations)
        val motion = readObject(File(contractDir, "motion-tokens.json"), violations) ?: return ContractReport(evidence, violations)

        // ---- 1. parts.json -------------------------------------------------
        val partTypeSet = checkParts(parts, violations, evidence)

        // ---- 2. signature-matrix.json --------------------------------------
        checkSignatureMatrix(matrix, partTypeSet, violations, evidence)

        // ---- 3. context-segments.json --------------------------------------
        checkContextSegments(segments, violations, evidence)

        // ---- 4. redlines.json ----------------------------------------------
        checkRedlines(redlines, violations, evidence)

        // ---- 5. acceptance.json --------------------------------------------
        checkAcceptance(acceptance, violations, evidence)

        // ---- 6. decisions.json ---------------------------------------------
        checkDecisions(decisions, violations, evidence)

        // ---- 7. motion-tokens.json -----------------------------------------
        checkMotionTokens(motion, violations, evidence)

        // ---- 8. libs.versions.toml -----------------------------------------
        checkVersionCatalog(versionsToml, violations, evidence)

        return ContractReport(evidence, violations)
    }

    // ------------------------------------------------------------------ parts

    private fun checkParts(parts: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>): Set<String> {
        val list = parts["parts"] as? JsonArray
        if (list == null) {
            v += ContractViolation("parts.json", "缺少 parts 数组")
            return emptySet()
        }
        val types = list.mapNotNull { it.jsonObject["type"]?.jsonPrimitive?.contentOrNull() }
        e += "parts.json：条数=${types.size}（期望 ${EXPECTED_PART_TYPES.size}）"
        if (types.size != EXPECTED_PART_TYPES.size) {
            v += ContractViolation("parts.json", "零件条数=${types.size}，期望 ${EXPECTED_PART_TYPES.size}")
        }
        if (types != EXPECTED_PART_TYPES) {
            val diff = compareOrdered(EXPECTED_PART_TYPES, types)
            v += ContractViolation("parts.json", "type 集合与契约 §4 不一致：$diff")
        }
        list.forEachIndexed { i, element ->
            val obj = element.jsonObject
            val type = obj["type"]?.jsonPrimitive?.contentOrNull() ?: "?(#$i)"
            val required = (obj["payload_required"] as? JsonArray)?.size ?: 0
            if (required == 0) v += ContractViolation("parts.json", "$type 的 payload_required 为空")
        }
        val catalog = parts["catalog_version"]?.jsonPrimitive?.intOrNull()
        e += "parts.json：catalog_version=$catalog（期望 $CATALOG_VERSION）"
        if (catalog != CATALOG_VERSION) {
            v += ContractViolation("parts.json", "catalog_version=$catalog，期望 $CATALOG_VERSION")
        }
        return types.toSet()
    }

    // ------------------------------------------------------- signature matrix

    private fun checkSignatureMatrix(
        matrix: JsonObject,
        partTypes: Set<String>,
        v: MutableList<ContractViolation>,
        e: MutableList<String>,
    ) {
        val modes = (matrix["modes"] as? JsonArray) ?: run {
            v += ContractViolation("signature-matrix.json", "缺少 modes 数组")
            return
        }
        val modeIds = modes.mapNotNull { it.jsonObject["mode"]?.jsonPrimitive?.contentOrNull() }
        e += "signature-matrix.json：档数=${modeIds.size}、required 坏引用查完（共 ${modes.size} 档）"
        if (modeIds.toSet() != EXPECTED_MODES.toSet()) {
            v += ContractViolation("signature-matrix.json", "五档不齐全：$modeIds")
        }
        modes.forEach { element ->
            val obj = element.jsonObject
            val mode = obj["mode"]?.jsonPrimitive?.contentOrNull() ?: "?"
            val required = (obj["required"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            val optional = (obj["optional"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            (required + optional).forEach { sig ->
                if (sig !in partTypes) {
                    v += ContractViolation("signature-matrix.json", "$mode 引用了 parts.json 中不存在的签名：$sig")
                }
            }
            if (required.isEmpty()) {
                v += ContractViolation("signature-matrix.json", "$mode 的 required 为空")
            }
            val anyOf = (obj["required_any_of"] as? JsonArray)?.size ?: 0
            if (mode == "boost" && anyOf != 2) {
                v += ContractViolation("signature-matrix.json", "boost 档 required_any_of 长度=$anyOf，期望 2")
            }
            if (mode != "boost" && anyOf != 0) {
                v += ContractViolation("signature-matrix.json", "$mode 档不应有 required_any_of（长度=$anyOf）")
            }
        }
    }

    // ------------------------------------------------------- context segments

    private fun checkContextSegments(segments: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>) {
        val list = (segments["segments"] as? JsonArray) ?: run {
            v += ContractViolation("context-segments.json", "缺少 segments 数组")
            return
        }
        val enums = list.mapNotNull { it.jsonObject["code_enum"]?.jsonPrimitive?.contentOrNull() }
        e += "context-segments.json：段数=${enums.size}、顺序=${enums.joinToString("→")}"
        if (enums != EXPECTED_SEGMENTS) {
            v += ContractViolation("context-segments.json", "八段顺序不符：$enums")
        }
        val breakpoint = segments["breakpoint_after"]?.jsonPrimitive?.intOrNull()
        e += "context-segments.json：breakpoint_after=$breakpoint（期望 $BREAKPOINT_AFTER）"
        if (breakpoint != BREAKPOINT_AFTER) {
            v += ContractViolation("context-segments.json", "breakpoint_after=$breakpoint，期望 $BREAKPOINT_AFTER")
        }
        list.take(BREAKPOINT_AFTER).forEachIndexed { i, element ->
            val prefix = element.jsonObject["is_prefix"]?.jsonPrimitive?.contentOrNull()?.toBoolean()
            if (prefix != true) {
                v += ContractViolation("context-segments.json", "第 ${i + 1} 段（${enums.getOrNull(i)}）is_prefix 非 true")
            }
        }
        list.drop(BREAKPOINT_AFTER).forEachIndexed { i, element ->
            val prefix = element.jsonObject["is_prefix"]?.jsonPrimitive?.contentOrNull()?.toBoolean()
            if (prefix == true) {
                v += ContractViolation("context-segments.json", "断点后第 ${i + 1} 段不应标 is_prefix=true")
            }
        }
    }

    // ---------------------------------------------------------------- redlines

    private fun checkRedlines(redlines: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>) {
        val list = (redlines["redlines"] as? JsonArray) ?: run {
            v += ContractViolation("redlines.json", "缺少 redlines 数组")
            return
        }
        val ids = list.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull() }
        e += "redlines.json：条数=${ids.size}、ID 去重后=${ids.toSet().size}"
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            v += ContractViolation("redlines.json", "ID 重复：$it")
        }
        REQUIRED_REDLINE_IDS.forEach { required ->
            if (required !in ids) v += ContractViolation("redlines.json", "缺少必须存在的红线：$required")
        }
        // 无空缺：每个分组内序号必须是 1..N 连续
        val grouped = mutableMapOf<String, MutableList<Int>>()
        ids.forEach { id ->
            val match = REDLINE_ID_REGEX.matchEntire(id)
            if (match == null) {
                v += ContractViolation("redlines.json", "ID 命名不符 R-<P|T|C|R|E|D><n> / D<n> / M<n>：$id")
            } else {
                grouped.getOrPut(match.groupValues[1]) { mutableListOf() } += match.groupValues[2].toInt()
            }
        }
        REDLINE_GROUPS.forEach { (prefix, expectedCount) ->
            val nums = grouped[prefix] ?: emptyList()
            val expected = (1..expectedCount).toList()
            e += "  - $prefix：${nums.size} 条（期望 $expectedCount）"
            if (nums.sorted() != expected) {
                v += ContractViolation("redlines.json", "$prefix 组序号不连续或有空缺：${nums.sorted()}")
            }
        }
        list.forEach { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull() ?: "?"
            val enforcement = obj["enforcement"]?.jsonPrimitive?.contentOrNull()
            if (enforcement !in ENFORCEMENT_VALUES) {
                v += ContractViolation("redlines.json", "$id 的 enforcement=$enforcement 不在 {machine,human}")
            }
            val ownerStep = obj["owner_step"]?.jsonPrimitive?.intOrNull()
            if (ownerStep == null || ownerStep !in 1..10) {
                v += ContractViolation("redlines.json", "$id 的 owner_step=$ownerStep 不在 1..10")
            }
        }
        e += "redlines.json：49 条红线均已标注 enforcement 与 owner_step(1..10)"
    }

    // -------------------------------------------------------------- acceptance

    private fun checkAcceptance(acceptance: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>) {
        val list = (acceptance["items"] as? JsonArray) ?: run {
            v += ContractViolation("acceptance.json", "缺少 items 数组")
            return
        }
        val ids = list.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull() }
        val byLevel = list.mapNotNull { it.jsonObject["level"]?.jsonPrimitive?.contentOrNull() }
            .groupingBy { it }.eachCount()
        e += "acceptance.json：条数=${ids.size}、级别分布=$byLevel"
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            v += ContractViolation("acceptance.json", "ID 重复：$it")
        }
        REQUIRED_ACCEPTANCE_IDS.forEach { required ->
            if (required !in ids) v += ContractViolation("acceptance.json", "缺少必须存在的验收项：$required")
        }
        list.forEach { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull() ?: "?"
            val level = obj["level"]?.jsonPrimitive?.contentOrNull()
            if (level !in LEVEL_VALUES) {
                v += ContractViolation("acceptance.json", "$id 的 level=$level 不在 {P0,P1,P2}")
            }
        }
    }

    // --------------------------------------------------------------- decisions

    private fun checkDecisions(decisions: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>) {
        val list = (decisions["decisions"] as? JsonArray) ?: run {
            v += ContractViolation("decisions.json", "缺少 decisions 数组")
            return
        }
        val ids = list.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull() }
        e += "decisions.json：条数=${ids.size}（期望 $DECISION_COUNT）"
        val expected = (1..DECISION_COUNT).map { "D$it" }
        if (ids.sortedBy { it.removePrefix("D").toIntOrNull() ?: -1 } != expected) {
            v += ContractViolation("decisions.json", "D1~D$DECISION_COUNT 不连续：$ids")
        }
        list.forEach { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.contentOrNull() ?: "?"
            val step = obj["landing_step"]?.jsonPrimitive?.intOrNull()
            if (step == null || step !in 1..10) {
                v += ContractViolation("decisions.json", "$id 的 landing_step=$step 不在 1..10")
            }
            if (obj["landing_module"]?.jsonPrimitive?.contentOrNull().isNullOrBlank()) {
                v += ContractViolation("decisions.json", "$id 缺少 landing_module")
            }
        }
    }

    // ------------------------------------------------------------ motion tokens

    private fun checkMotionTokens(motion: JsonObject, v: MutableList<ContractViolation>, e: MutableList<String>) {
        val durations = motion["durations"]?.jsonObject
        if (durations == null) {
            v += ContractViolation("motion-tokens.json", "缺少 durations")
        } else {
            EXPECTED_DURATIONS.forEach { (k, expected) ->
                val actual = durations[k]?.jsonPrimitive?.intOrNull()
                if (actual != expected) {
                    v += ContractViolation("motion-tokens.json", "durations.$k=$actual，期望 $expected")
                }
            }
            e += "motion-tokens.json：durations=${EXPECTED_DURATIONS.keys.joinToString("/") { k -> "$k=${durations[k]?.jsonPrimitive?.intOrNull()}" }}"
        }
        val springs = motion["springs"]?.jsonObject
        if (springs == null) {
            v += ContractViolation("motion-tokens.json", "缺少 springs")
        } else {
            EXPECTED_SPRINGS.forEach { (k, pair) ->
                val spring = springs[k]?.jsonObject
                val stiffness = spring?.get("stiffness")?.jsonPrimitive?.intOrNull()
                val damping = spring?.get("dampingRatio")?.jsonPrimitive?.intOrNull()
                if (stiffness != pair.first || damping != pair.second) {
                    v += ContractViolation(
                        "motion-tokens.json",
                        "springs.$k=($stiffness,$damping)，期望(${pair.first},${pair.second})",
                    )
                }
            }
            e += "motion-tokens.json：springs=snappy(380,30)/default(260,30)/soft(260,26)"
        }
        val tau = motion["tau"]?.jsonObject
        if (tau == null) {
            v += ContractViolation("motion-tokens.json", "缺少 tau")
        } else {
            EXPECTED_TAU.forEach { (k, expected) ->
                val actual = tau[k]?.jsonPrimitive?.contentOrNull()?.toDoubleOrNull()
                if (actual != expected) {
                    v += ContractViolation("motion-tokens.json", "tau.$k=$actual，期望 $expected")
                }
            }
            e += "motion-tokens.json：tau=${EXPECTED_TAU.keys.joinToString("/") { k -> "$k=${tau[k]?.jsonPrimitive?.contentOrNull()}" }}"
        }
        val recipes = (motion["recipes"] as? JsonArray)
        if (recipes == null) {
            v += ContractViolation("motion-tokens.json", "缺少 recipes")
        } else {
            e += "motion-tokens.json：recipes=${recipes.size} 条（期望 $RECIPE_COUNT）"
            if (recipes.size != RECIPE_COUNT) {
                v += ContractViolation("motion-tokens.json", "recipes 长度=${recipes.size}，期望 $RECIPE_COUNT")
            }
            recipes.forEach { element ->
                val obj = element.jsonObject
                val no = obj["no"]?.jsonPrimitive?.contentOrNull() ?: "?"
                if (obj["minimal_fallback"]?.jsonPrimitive?.contentOrNull().isNullOrBlank()) {
                    v += ContractViolation("motion-tokens.json", "配方 #$no 缺少 minimal_fallback")
                }
            }
        }
    }

    // ----------------------------------------------------------- version catalog

    private fun checkVersionCatalog(toml: File, v: MutableList<ContractViolation>, e: MutableList<String>) {
        if (!toml.isFile) {
            v += ContractViolation("libs.versions.toml", "文件不存在：${toml.path}")
            return
        }
        val text = toml.readText()
        REQUIRED_LIB_RULES.forEach { (label, regex) ->
            if (!regex.containsMatchIn(text)) {
                v += ContractViolation("libs.versions.toml", "缺少或版本不符：$label")
            }
        }
        val hit = REQUIRED_LIB_RULES.count { it.second.containsMatchIn(text) }
        e += "libs.versions.toml：三类库坐标命中 $hit/${REQUIRED_LIB_RULES.size}"
    }

    // ----------------------------------------------------------------- helpers

    private fun readObject(file: File, v: MutableList<ContractViolation>): JsonObject? {
        if (!file.isFile) {
            v += ContractViolation(file.name, "文件不存在：${file.path}")
            return null
        }
        return runCatching { json.parseToJsonElement(file.readText()).jsonObject }
            .getOrElse {
                v += ContractViolation(file.name, "不是合法 JSON 对象：${it.message}")
                null
            }
    }

    private fun compareOrdered(expected: List<String>, actual: List<String>): String {
        val missing = expected.filterNot { it in actual }
        val extra = actual.filterNot { it in expected }
        return buildString {
            if (missing.isNotEmpty()) append("缺失=${missing} ")
            if (extra.isNotEmpty()) append("多出=${extra} ")
            if (missing.isEmpty() && extra.isEmpty()) append("集合相同但顺序不同：`$actual`")
        }
    }

    private fun JsonElement?.contentOrNull(): String? = (this as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content
    private fun JsonElement?.intOrNull(): Int? = contentOrNull()?.toIntOrNull()
}

/** 资产路径解析：优先用 Gradle 传入的 system property，其次按仓库相对路径兜底。 */
object ContractPaths {
    val contractDir: File
        get() = System.getProperty("contract.dir")?.let(::File) ?: File("../../assets/contract")

    val versionsToml: File
        get() = System.getProperty("versions.toml")?.let(::File) ?: File("../../gradle/libs.versions.toml")
}
