plugins {
    id("com.android.application")
}

// The release keystore is NOT committed to the repository. CI reconstructs
// it from the FINACLEDESK_KEYSTORE_B64 secret before building; the password
// arrives via the FINACLEDESK_KEYSTORE_PASSWORD env var. A local build
// without the keystore produces an unsigned release APK.
val releaseKeystore = rootProject.file("signing/finacledesk-release.jks")
val keystorePassword: String = System.getenv("FINACLEDESK_KEYSTORE_PASSWORD") ?: ""

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
        if (releaseKeystore.exists()) {
            create("shared") {
                storeFile = releaseKeystore
                storePassword = keystorePassword
                keyAlias = "finacledesk"
                keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (releaseKeystore.exists()) signingConfigs.getByName("shared") else null
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
