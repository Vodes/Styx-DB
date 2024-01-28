import org.ajoberstar.grgit.Grgit

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    `java-library`
    `maven-publish`
    id("org.ajoberstar.grgit") version ("5.2.1")
}

group = "moe.styx"
version = "0.3"

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
            version = "0.3"

            from(components["java"])
        }
    }
}

tasks.register("buildExternalDeps") {
    val isWin = System.getProperty("os.name").contains("win", true)
    val projectDir = layout.projectDirectory.asFile.parentFile
    val outDir = File(projectDir, ".temp-deps/styx-types")
    doFirst {
        outDir.deleteRecursively()
        Grgit.clone {
            dir = outDir
            uri = "https://github.com/Vodes/Styx-Types.git"
        }
        val result = kotlin.runCatching {
            ProcessBuilder(listOf(if (isWin) "./gradlew.bat" else "./gradlew", "publishToMavenLocal"))
                .directory(outDir)
                .inheritIO()
                .start().waitFor()
        }.getOrNull() ?: -1
        if (result != 0) {
            outDir.deleteRecursively()
            throw StopExecutionException()
        }
    }
    doLast { outDir.deleteRecursively() }
}

