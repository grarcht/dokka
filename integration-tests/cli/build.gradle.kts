import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.github.johnrengelman.shadow")
}

val dokka_version: String by project
evaluationDependsOn(":runners:cli")
evaluationDependsOn(":plugins:base")

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("test-junit"))

    val coroutines_version: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

}

/* Create a fat base plugin jar for cli tests */
val basePluginShadow: Configuration by configurations.creating
dependencies {
    basePluginShadow(project(":plugins:base"))
}
val basePluginShadowJar = tasks.create("basePluginShadowJar", ShadowJar::class) {
    configurations = listOf(basePluginShadow)
    archiveFileName.set("fat-base-plugin-$dokka_version.jar")
    archiveClassifier.set("")
}

tasks.integrationTest {
    val cliJar = tasks.getByPath(":runners:cli:shadowJar") as ShadowJar
    environment("CLI_JAR_PATH", cliJar.archiveFile.get())
    environment("BASE_PLUGIN_JAR_PATH", basePluginShadowJar.archiveFile.get())
    dependsOn(cliJar)
    dependsOn(basePluginShadowJar)
}
