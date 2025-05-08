

// Define version catalog
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven ("https://storage.zego.im/maven")
        maven ("https://www.jitpack.io")//
    }
    versionCatalogs {
        create("libs") {
            // Android plugins
            plugin("android.application", "com.android.application").version("8.2.0")
            
            // Firebase BOM
            version("firebase-bom", "32.7.4")
            
            // AndroidX libraries
            library("appcompat", "androidx.appcompat:appcompat:1.6.1")
            library("material", "com.google.android.material:material:1.11.0")
            library("activity", "androidx.activity:activity:1.8.2")
            library("constraintlayout", "androidx.constraintlayout:constraintlayout:2.1.4")
            library("gridlayout", "androidx.gridlayout:gridlayout:1.0.0")
            
            // Testing
            library("junit", "junit:junit:4.13.2")
            library("ext.junit", "androidx.test.ext:junit:1.1.5")
            library("espresso.core", "androidx.test.espresso:espresso-core:3.5.1")
        }
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven ("https://storage.zego.im/maven")
        maven ("https://www.jitpack.io")
        maven ("https://storage.zego.im/maven")  // <- Add this line.
        maven ("https://www.jitpack.io") // <- Add this line.
    }
}

rootProject.name = "sprintly_app_smd_finale"
include(":app")
