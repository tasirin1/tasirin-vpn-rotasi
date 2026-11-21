plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "id.tasirin.vpnrotasi"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.tasirin.vpnrotasi"
        // minSdk 26 untuk aplikasi; fitur koneksi IKEv2 (VpnManager) butuh
        // API 30+ dan dicek saat jalan — di bawah itu aplikasi hanya
        // mengelola daftar server dan pengaturan rotasi.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            val storeFileProp = project.findProperty("storeFile") as String?
            if (storeFileProp != null) {
                storeFile = file(storeFileProp)
                storePassword = project.findProperty("storePassword") as String?
                keyAlias = project.findProperty("keyAlias") as String?
                keyPassword = project.findProperty("keyPassword") as String?
                // Keystore proyek berformat PKCS12 (dibuat dengan OpenSSL,
                // tersimpan sebagai secret GitHub — tidak pernah di repo).
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Tanda tangan hanya bila properti keystore diberikan (CI rilis).
            // Tanpa itu assembleRelease menghasilkan APK tanpa tanda tangan.
            if (project.findProperty("storeFile") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    testImplementation("junit:junit:4.13.2")
}
