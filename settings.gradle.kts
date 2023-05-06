rootProject.name = "cus"

pluginManagement {
    repositories {
        mavenCentral()
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}