plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-rc2"
}

val projectVersion : String by project
val projectGroup : String by project

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.momirealms.net/releases/")
        maven("https://libraries.minecraft.net/")
    }

    dependencies {
        compileOnly("net.momirealms:craft-engine-core:0.0.62.3.1")
        compileOnly("net.momirealms:craft-engine-bukkit:0.0.62.3.1")
        compileOnly("com.mojang:brigadier:1.0.18")
    }
}