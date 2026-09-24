plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.21.10-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION