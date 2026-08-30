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
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VideoCompressor"

include(":app")
include(":core:common")
include(":core:resources")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:video")
include(":feature:home")
include(":feature:editor")
include(":feature:history")
include(":feature:settings")
