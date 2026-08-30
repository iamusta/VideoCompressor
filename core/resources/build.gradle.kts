plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.videocompress.core.resources"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
}
