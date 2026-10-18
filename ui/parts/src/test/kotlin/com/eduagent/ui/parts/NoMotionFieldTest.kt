package com.eduagent.ui.parts

import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.ui.parts.gallery.PartSamples
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §4.1 必测 · `NoMotionFieldTest`（红线 R-T10）。
 *
 * 所有零件 payload **不含**任何动效 / 时长 / 曲线字段 —— 用反射遍历 16 个样品的
 * 字段名与字段类型（含嵌套列表元素）逐层核对。
 */
class NoMotionFieldTest {

    /** 禁止出现的字段名关键词（涵盖时长、缓动、弹簧、位移曲线等）。 */
    private val bannedNameTokens = listOf(
        "duration",
        "delay",
        "easing",
        "curve",
        "spring",
        "stiffness",
        "damping",
        "velocity",
        "animation",
        "motion",
        "interpolator",
        "timing",
        "fps",
        "frames",
    )

    /** 禁止出现的字段类型所属包（动效类型不得进入 schema）。 */
    private val bannedTypePackages = listOf(
        "androidx.compose.animation",
        "kotlin.time",
    )

    @Test
    fun noPartPayloadCarriesMotionFields() {
        val offenders = mutableListOf<String>()
        val visited = mutableSetOf<Class<*>>()
        PartSamples.all.forEach { envelope ->
            collect(envelope, envelope.type, offenders, visited)
        }
        assertEquals(emptyList<String>(), offenders)
    }

    @Test
    fun envelopeCarriesOnlyVersionIdTypePayload() {
        val names = PartEnvelope::class.java.declaredFields.map { field -> field.name }.sorted()
        assertEquals(listOf("id", "payload", "type", "v"), names)
    }

    @Test
    fun allSixteenPayloadClassesAreCovered() {
        val payloadClasses = PartSamples.all.map { envelope -> envelope.payload.javaClass.name }.toSet()
        assertEquals(16, payloadClasses.size)
    }

    @Test
    fun noPayloadFieldTypeComesFromAnAnimationPackage() {
        val offenders = mutableListOf<String>()
        val visited = mutableSetOf<Class<*>>()
        PartSamples.all.forEach { envelope ->
            walkTypes(envelope.payload.javaClass, envelope.type, offenders, visited)
        }
        assertEquals(emptyList<String>(), offenders)
    }

    /** 递归收集字段名（含 List / Array 元素与嵌套 data class）。 */
    private fun collect(
        value: Any?,
        path: String,
        offenders: MutableList<String>,
        visited: MutableSet<Class<*>>,
    ) {
        when (value) {
            null, is String, is Int, is Float, is Double, is Boolean -> Unit

            // 枚举成员自引用（`TEXT` 字段指回 `TEXT`）、数组等都是叶子
            is Enum<*> -> Unit

            is List<*> -> value.forEach { item -> collect(item, path, offenders, visited) }

            is Array<*> -> value.forEach { item -> collect(item, path, offenders, visited) }

            is PartEnvelope -> collect(value.payload, path, offenders, visited)

            else -> {
                val type = value.javaClass
                if (!visited.add(type)) return
                type.declaredFields.forEach { field ->
                    field.isAccessible = true
                    val name = field.name
                    val token = bannedNameTokens.firstOrNull { banned ->
                        name.contains(banned, ignoreCase = true)
                    }
                    if (token != null) {
                        offenders += "$path.$name matches '$token'"
                    }
                    collect(field.get(value), "$path.$name", offenders, visited)
                }
            }
        }
    }

    /** 递归核对字段声明类型不属于动效相关包。 */
    private fun walkTypes(
        type: Class<*>,
        path: String,
        offenders: MutableList<String>,
        visited: MutableSet<Class<*>>,
    ) {
        if (type.isPrimitive || type.name.startsWith("java.lang")) return
        if (type.isArray || type.isEnum) return
        if (bannedTypePackages.any { pkg -> type.name.startsWith(pkg) }) {
            offenders += "$path is declared as ${type.name}"
            return
        }
        if (!visited.add(type)) return
        type.declaredFields.forEach { field ->
            field.isAccessible = true
            val raw = field.type
            val element = if (List::class.java.isAssignableFrom(raw)) {
                raw.typeParameters.firstOrNull()?.let { parameter ->
                    (parameter as? java.lang.reflect.ParameterizedType)
                        ?.actualTypeArguments?.firstOrNull() as? Class<*>
                }
            } else {
                raw
            }
            if (element != null) walkTypes(element, "$path.${field.name}", offenders, visited)
        }
    }

    @Test
    fun bannedTokenScanIsNotSilentlyEmpty() {
        // 反例自检：若黑名单失效（例如写成空表），本测试会失败
        assertTrue(bannedNameTokens.isNotEmpty())
        assertTrue(bannedTypePackages.isNotEmpty())
    }
}
