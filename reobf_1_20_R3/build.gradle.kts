plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}