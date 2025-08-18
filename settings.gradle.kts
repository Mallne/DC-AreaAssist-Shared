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