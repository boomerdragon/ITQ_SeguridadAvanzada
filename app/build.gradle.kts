plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.itq.seguridadavanzadamovilapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.itq.seguridadavanzadamovilapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Biblioteca Jetpack Security para Cifrado Local (EncryptedSharedPreferences & EncryptedFile)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Cliente HTTP Volley para servicios web RESTful seguros
    implementation("com.android.volley:volley:1.2.1")

    // Componentes de la Interfaz Gráfica (AndroidX & Material Design)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Jetpack Lifecycle (MVVM: ViewModel & LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Pruebas unitarias e instrumentadas
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}