package com.eduagent.core.model

/**
 * 端侧 A2UI 目录版本号（红线 R-T8）。
 *
 * 契约依据：01-契约.md §6 —— CATALOG 段必须携带 `catalog_version`，且与端侧编译期常量一致
 * （第 9 步断言）。升级端侧目录必须改本常量，**禁止**旧版本号继续使用。
 */
object CatalogVersion {
    const val CATALOG_VERSION: Int = 1
}
