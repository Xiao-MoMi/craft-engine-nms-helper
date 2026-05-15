plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION