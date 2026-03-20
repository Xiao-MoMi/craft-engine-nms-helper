import java.net.URI

val projectVersion : String by project
val projectGroup : String by project

dependencies {
    implementation(project(":common"))
    implementation(project(mapOf("path" to ":reobf_1_21_R7", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R6", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R5", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R4", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R3", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R2", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_21_R1", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R4", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R3", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R2", "configuration" to "reobf")))
    implementation(project(mapOf("path" to ":reobf_1_20_R1", "configuration" to "reobf")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

artifacts {
    archives(tasks.shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveFileName = "${rootProject.name}-${projectVersion}.jar"
        destinationDirectory.set(file("$rootDir/target"))
    }
}

publishing {
    repositories {
        maven {
            name = "XiaoMoMi"
            url = URI("https://repo.momirealms.net/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.momirealms"
            artifactId = "craft-engine-nms-helper"
            version = projectVersion
            from(components["shadow"])
        }
    }
}