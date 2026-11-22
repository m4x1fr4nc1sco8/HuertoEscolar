import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

// Datos de la firma (llave académica del curso). Si el archivo no existe, el proyecto igual compila.
val keystoreProperties = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "cl.duoc.huertoescolar"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.huertoescolar"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Exporta el APK release firmado con un nombre legible: HuertoEscolar-v1.0-build1.apk
val nombreApp = "HuertoEscolar"
val versionApp = android.defaultConfig.versionName ?: "1.0"
val buildApp = android.defaultConfig.versionCode ?: 1

tasks.register<Copy>("exportarApkRelease") {
    val nombreFinal = "$nombreApp-v$versionApp-build$buildApp.apk"
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
        rename("app-release.apk", nombreFinal)
    }
    // Carpeta en la raíz del proyecto (no dentro de build/) para que el APK quede en el repositorio
    into(rootProject.layout.projectDirectory.dir("apk-firmada"))
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.compose.material:material-icons-core")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

kover {
    reports {
        filters {
            includes {
                classes(
                    "cl.duoc.huertoescolar.domain.validation.*",
                    "cl.duoc.huertoescolar.domain.alertas.*",
                    "cl.duoc.huertoescolar.data.repository.*",
                    "cl.duoc.huertoescolar.data.session.SesionActual*",
                    "cl.duoc.huertoescolar.util.FechasKt*",
                    "cl.duoc.huertoescolar.ui.viewmodel.*ViewModel*",
                    "cl.duoc.huertoescolar.data.remote.MapeosKt*",
                    "cl.duoc.huertoescolar.data.remote.ErroresApiKt*"
                )
            }
        }
    }
}
