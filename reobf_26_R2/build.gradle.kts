plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:26.2.build.110-stable")
    compileOnly(project(":common"))
    compileOnly(project(":stubs"))
}
