@file:OptIn(ExperimentalWasmDsl::class)

import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.dependency.analysis)
}

allprojects {
    apply {
        plugin("com.autonomousapps.dependency-analysis")
    }
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://registry.mallne.cloud/repository/DiCentraArtefacts/")
            credentials {
                username = providers.environmentVariable("NEXUS_USERNAME").getOrElse("")
                password = providers.environmentVariable("NEXUS_PASSWORD").getOrElse("")
            }
        }
    }
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}

group = "cloud.mallne.dicentra.areaassist"
version = project.findProperty("VERSION_NAME") ?: "1.0.0-SNAPSHOT"

kotlin {
    jvm()
    android {
        namespace = "${project.group}.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }
    js {
        nodejs()
        browser()
    }
    wasmJs {
        browser()
        nodejs()
        d8()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.dc.polyfill)
                api(libs.dc.aviator.core)
                api(libs.dc.aviator.client.ktor)
                api(libs.dc.aviator.koas)
                api(libs.dc.aviator.adapter.xml)
                api(libs.dc.aviator.adapter.json)
                api(libs.dc.weaver.core)
                api(libs.mlln.units)
                api(libs.mlln.geokit.calculation)
                api(libs.mlln.geokit.geojson)
                api(libs.mlln.geokit.ogc)
                api(libs.mlln.geokit.interop)
                api(libs.mlln.geokit.coordinates)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                api(libs.ktor.http)
                api(libs.ktor.openapi.schema)
                api(libs.xml)
                api(libs.maplibre.spatialk)
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.dc.aviator.plugin.synapse)
            }
        }
        jvmTest.dependencies {
            implementation(libs.junit.jupiter.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.mock)
            implementation(libs.dc.aviator.plugin.weaver)
            implementation(libs.dc.aviator.plugin.interception)
            runtimeOnly(libs.junit.jupiter.engine)
            runtimeOnly(libs.junit.vintage.engine)
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
    jvmToolchain(25)
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform { excludeTags("live-provider") }
}

val jvmTestCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")
tasks.register<Test>("liveApiContractTest") {
    description = "Opt-in live response-mapping checks for services from all built-in API documents."
    group = "verification"
    testClassesDirs = jvmTestCompilation.output.classesDirs
    classpath = jvmTestCompilation.output.allOutputs + jvmTestCompilation.runtimeDependencyFiles!!
    useJUnitPlatform { includeTags("live-provider") }
    outputs.upToDateWhen { false }
    maxParallelForks = 1
}


mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "DiCentraArtefacts"
                url = uri("https://registry.mallne.cloud/repository/DiCentraArtefacts/")
                credentials {
                    username = providers.environmentVariable("NEXUS_USERNAME").getOrElse("")
                    password = providers.environmentVariable("NEXUS_PASSWORD").getOrElse("")
                }
            }
        }
    }

    coordinates(group.toString(), project.name)
    pom {
        name = "DiCentra AreaAssist Shared"
        inceptionYear = "2025"
        developers {
            developer {
                name = "Mallne"
                url = "mallne.cloud"
            }
        }
    }
}
