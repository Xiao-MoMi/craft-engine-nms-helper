plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}
