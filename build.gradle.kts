// Top-level build file where you can add configuration options common to all sub-projects/modules.
// NOTE: As of AGP 9.0, Kotlin support is built in — the kotlin.android and kotlin.plugin.compose
//       plugins no longer need to be declared here.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
}
