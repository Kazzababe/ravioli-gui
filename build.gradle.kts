plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

val paperVersion = "1.20.2-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    implementation("org.jetbrains:annotations:24.0.1")

    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:${paperVersion}")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        register<MavenPublication>("shadow") {
            project.shadow.component(this)

            artifact(tasks.reobfJar) {
                classifier = ""
            }
        }
    }
}