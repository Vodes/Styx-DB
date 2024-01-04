plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    `java-library`
    `maven-publish`
}

group = "moe.styx"
version = "0.2"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api(libs.styx.types)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("build") {
            groupId = "moe.styx"
            artifactId = "styx-db"
            version = "0.2"

            from(components["java"])
        }
    }
}