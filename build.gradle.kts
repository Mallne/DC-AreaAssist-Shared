@file:OptIn(ExperimentalWasmDsl::class)

import com.android.build.api.dsl.androidLibrary
import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.dependency.analysis)
}

allprojects {
    apply {
        plugin("com.autonomousapps.dependency-analysis")
    }
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}

group = "cloud.mallne.dicentra.areaassist"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvm()
    androidLibrary {
        namespace = project.group.toString()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
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
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.dc.polyfill)
                api(libs.dc.aviator.core)
                api(libs.dc.aviator.client.ktor)
                api(libs.dc.aviator.koas)
                api(libs.dc.aviator.adapter.xml)
                api(libs.dc.aviator.adapter.json)
                api(libs.mlln.units)
                api(libs.mlln.geokit.calculation)
                api(libs.mlln.geokit.geojson)
                api(libs.mlln.geokit.ogc)
                api(libs.mlln.geokit.interop)
                api(libs.mlln.geokit.coordinates)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                api(libs.ktor.http)
                api(libs.koin.annotations)
                api(libs.xml)
                api(libs.maplibre.spatialk)
            }
        }
    }
    jvmToolchain(21)
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspIosX64", libs.koin.ksp)
    add("kspIosArm64", libs.koin.ksp)
    add("kspIosSimulatorArm64", libs.koin.ksp)
}

mavenPublishing {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

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
                repositories {
                    maven {
                        url = uri("https://registry.mallne.cloud/repository/DiCentraArtefacts/")
                        credentials {
                            username = properties["dc.username"] as String?
                            password = properties["dc.password"] as String?
                        }
                    }
                }
            }
        }
    }

    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())
}
