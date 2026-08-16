// Build script tingkat proyek. Versi diselaraskan dgn proyek Compose lain di mesin
// ini (AGP 8.10.1 + Kotlin 2.0.21 + compose-bom 2024.09.00) yang sudah terbukti.
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
