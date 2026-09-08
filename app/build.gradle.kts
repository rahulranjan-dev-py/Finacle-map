plugins {
    id("com.android.application")
}

android {
    namespace = "com.finacledesk.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.finacledesk.app"
        minSdk = 24
        targetSdk = 35
        // CI builds auto-increment so a newer APK always installs over an older one.
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0." + (System.getenv("GITHUB_RUN_NUMBER") ?: "0")
    }

    signingConfigs {
        create("shared") {
            storeFile = rootProject.file(project.property("FINACLEDESK_STORE_FILE") as String)
            storePassword = project.property("FINACLEDESK_STORE_PASSWORD") as String
            keyAlias = project.property("FINACLEDESK_KEY_ALIAS") as String
            keyPassword = project.property("FINACLEDESK_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
        }
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
