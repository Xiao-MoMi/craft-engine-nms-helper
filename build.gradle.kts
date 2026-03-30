plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.3.1"
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
        maven("https://repo.momirealms.net/snapshots")
        maven("https://repo.momirealms.net/releases/")
        maven("https://libraries.minecraft.net/")
    }

    dependencies {
        compileOnly("net.momirealms:craft-engine-core:26.4-SNAPSHOT")
        compileOnly("net.momirealms:craft-engine-bukkit:26.4-SNAPSHOT")
        compileOnly("net.momirealms:craft-engine-bukkit-proxy:26.4-SNAPSHOT")
        compileOnly("com.mojang:brigadier:1.0.18")
        compileOnly("net.momirealms:sparrow-reflection:0.21")
    }
}