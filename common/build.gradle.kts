tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.119.Final")
    compileOnly("com.mojang:datafixerupper:6.0.8")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}