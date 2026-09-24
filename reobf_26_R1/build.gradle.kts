plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:26.1.2.build.+")
    compileOnly(project(":common"))
}
