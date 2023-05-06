val kotlinVersion: String by project

plugins {
    kotlin("jvm")
    application
}

group = "me.yailya"
version = "0.1"

repositories {
    mavenCentral()
}

configurations.create("include")

dependencies {
    "include"("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation(configurations["include"])
}

application {
    mainClass.set("me.yailya.cus.MainKt")
}

tasks.jar {
    manifest.attributes(
        "Main-Class" to "me.yailya.cus.MainKt"
    )

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(
        *configurations["include"].map {
            if (it.isDirectory) it else zipTree(it)
        }.toTypedArray()
    )
}
