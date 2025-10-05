rootProject.name = "shared"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

val polyfillDir = file("../../polyfill")
if (polyfillDir.exists()) {
    includeBuild(polyfillDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra:polyfill")).using(project(":"))
        }
    }
} else {
    println("[AREAASSIST_SHARED:polyfill] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}

val aviatorDir = file("../../aviator")
if (aviatorDir.exists()) {
    includeBuild(aviatorDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.aviator.plugin:interception")).using(project(":plugins:interception"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:translation-keys")).using(project(":plugins:translation-keys"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:weaver")).using(project(":plugins:weaver"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:synapse")).using(project(":plugins:synapse"))
            substitute(module("cloud.mallne.dicentra.aviator.client:ktor")).using(project(":clients:ktor"))
            substitute(module("cloud.mallne.dicentra.aviator:koas")).using(project(":koas"))
            substitute(module("cloud.mallne.dicentra.aviator:core")).using(project(":core"))
        }
    }
} else {
    println("[AREAASSIST_SHARED:aviator] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}

val unitsDir = file("../units")
if (unitsDir.exists()) {
    includeBuild(unitsDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne:units")).using(project(":"))
        }
    }
} else {
    println("[AREAASSIST_SHARED:units] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val geokitDir = file("../geokit")
if (geokitDir.exists()) {
    includeBuild(geokitDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.geokit:calculation")).using(project(":calculation"))
            substitute(module("cloud.mallne.geokit:geojson")).using(project(":geojson"))
        }
    }
} else {
    println("[AREAASSIST_SHARED:geokit] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}