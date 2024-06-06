plugins {
    alias(libs.plugins.library.common)
    alias(libs.plugins.library.protobuf)
    alias(libs.plugins.refine)
}

android {
    namespace = "com.xayah.core.rootservice"

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        aidl = true
    }
}

dependencies {
    compileOnly(project(":core:hiddenapi"))
    implementation(project(":core:systemapi"))
    implementation(project(":core:util"))

    implementation(libs.kotlinx.coroutines.core.jvm)

    // libsu
    implementation(libs.libsu.service)

    // Gson
    implementation(libs.gson)
}