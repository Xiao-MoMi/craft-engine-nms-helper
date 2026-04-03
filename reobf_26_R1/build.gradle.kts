//plugins {
//    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
//}

dependencies {
    compileOnly(files("libs/paper-26.1.jar"))
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("it.unimi.dsi:fastutil:8.5.18")
    compileOnly("com.google.guava:guava:33.5.0-jre")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-json-legacy-impl:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.26.1")
    compileOnly("io.netty:netty-all:4.2.7.Final")
    compileOnly("com.mojang:datafixerupper:9.0.19")
    compileOnly(project(":common"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
