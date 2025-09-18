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