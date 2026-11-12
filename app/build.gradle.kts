// Ativa Android, Kotlin e o compilador das interfaces Compose.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Configura o aplicativo usando a sintaxe suportada pelo AGP 8.
android {
    namespace = "com.example.gerenciacao"
    compileSdk = 36

    // Preserva a identificação e a versão atual do aplicativo.
    defaultConfig {
        applicationId = "com.example.gerenciacao"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Mantém desativada a redução de código na versão de lançamento.
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Mantém o bytecode Java no mesmo nível usado anteriormente pelo projeto.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Habilita a construção das telas com Jetpack Compose.
    buildFeatures {
        compose = true
    }
}

// Alinha o bytecode Kotlin com o nível 11 configurado para o código Java.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Reúne as dependências de interface, execução e testes já utilizadas no aplicativo.
dependencies {
    // Usa a BOM para coordenar as versões das bibliotecas Compose.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Mantém as bibliotecas dos testes locais e dos testes executados no Android.
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Inclui as ferramentas de prévia e inspeção apenas nas compilações de desenvolvimento.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
