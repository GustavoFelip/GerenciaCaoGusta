// Disponibiliza os plugins que serão aplicados pelo módulo do aplicativo.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
