plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    `java-library`
    `maven-publish`
}

group = "moe.styx"
version = "0.4.6"

repositories {
    mavenCentral()
    maven("https://repo.styx.moe/releases")
}

dependencies {
    api(libs.styx.common)
    api(libs.hikaricp)

    api(libs.jetbrains.exposed.core)
    api(libs.jetbrains.exposed.dao)
    api(libs.jetbrains.exposed.jdbc)
    api(libs.jetbrains.exposed.json)

    testImplementation(libs.jdbc.postgre)
    testImplementation(libs.jdbc.mysql)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "Styx"
            url = if (version.toString().contains("-SNAPSHOT", true))
                uri("https://repo.styx.moe/snapshots")
            else
                uri("https://repo.styx.moe/releases")
            credentials {
                username = System.getenv("STYX_REPO_TOKEN")
                password = System.getenv("STYX_REPO_SECRET")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("build") {
            groupId = project.group.toString()
            artifactId = "styx-db"
            version = project.version.toString()

            from(components["java"])
        }
    }
}