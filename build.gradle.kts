plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}


tasks.register("assembleRelease") {
    dependsOn(":app:assembleRelease")
}

tasks.register("clean") {
    dependsOn(":app:clean")
}

tasks.register("build") {
    dependsOn(":app:build")
}

tasks.register("installDebug") {
    dependsOn(":app:installDebug")
}

tasks.register("installRelease") {
    dependsOn(":app:installRelease")
}