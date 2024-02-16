plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.git.plugin)
    `java-library`
    `maven-publish`
}

group = "moe.styx"
version = "0.0.5"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.styx.moe/releases") }
    maven { url = uri("https://repo.styx.moe/snapshots") }
}

dependencies {
    api(libs.styx.common)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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