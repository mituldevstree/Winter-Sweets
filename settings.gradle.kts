pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io") {
            val authToken: String by settings
            credentials { username = authToken }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") {
            val authToken: String by settings
            credentials { username = authToken }
        }
        maven(url = "https://jcenter.bintray.com")
        maven(url = "https://artifactory.appodeal.com/appodeal")
        maven(url = "https://artifacts.applovin.com/android")
        maven(url = "https://android-sdk.is.com/")
        maven(url = "https://sdk.tapjoy.com/")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven(url = "https://mvnrepository.com/artifact/com.inmobi.monetization/inmobi-ads")
        maven(url = "https://hyprmx.jfrog.io/artifactory/hyprmx")
        maven(url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://maven.google.com/")
        maven(url = "https://artifactory.bidmachine.io/bidmachine")
        maven(url = "https://bitbucket.org/adscend/androidsdk/raw/master/")
        maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
        maven(url = "https://artifactory.tools.tapresearch.io/artifactory/tapresearch-android-sdk/")
        maven(url = "https://releases.adjoe.io/maven")
        maven(url = "https://europe-west1-maven.pkg.dev/mychips-b31fe/mychips-android-sdk")
        maven(url = "https://dl.geemee.ai/geemee/")
    }
}

rootProject.name = "Winter-Sweets"
include(":app")
