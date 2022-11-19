dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("libs")
  }
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
  }
}

rootProject.name = "handyauth"
include(":example-app")
include(":client")
