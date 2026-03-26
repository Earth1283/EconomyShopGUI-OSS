plugins {
    kotlin("jvm") version "2.3.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "io.github.Earth1283"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc-repo" }
    maven("https://jitpack.io") { name = "jitpack" }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") { name = "placeholderapi" }
    maven("https://nexus.hc.to/content/repositories/pub_releases/") { name = "vault-repo" }
    maven("https://repo.codemc.org/repository/maven-public/") { name = "codemc" }
    maven("https://repo.rosewooddev.io/repository/public/") { name = "rosewood" }
    maven("https://nexus.scarsz.me/content/groups/public/") { name = "discordsrv" }
    maven("https://maven.enginehub.org/repo/") { name = "enginehub" }
}

dependencies {
    // Paper API — provides Adventure/MiniMessage at runtime
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Economy integrations (soft-depend — do NOT shadow)
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.2.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
    // GemsEconomy: no stable public Maven artifact — loaded at runtime via
    // class guards in GemsEconomy.kt without a compile-time dependency.

    // Async & database — shadowed into plugin JAR
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")

    // Plugin metrics — shadowed into plugin JAR
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }

    shadowJar {
        archiveClassifier.set("")

        // Reproducible output — entries sorted by name so the JAR hash is
        // stable across machines, enabling remote build-cache hits.
        isReproducibleFileOrder = true
        isPreserveFileTimestamps = false

        // Relocate all shadowed libraries to avoid classpath conflicts
        relocate("org.bstats", "io.github.Earth1283.economyShopGUIOSS.libs.bstats")
        relocate("org.xerial.sqlite", "io.github.Earth1283.economyShopGUIOSS.libs.sqlite")
        relocate("org.jetbrains.exposed", "io.github.Earth1283.economyShopGUIOSS.libs.exposed")
        relocate("kotlinx.coroutines", "io.github.Earth1283.economyShopGUIOSS.libs.coroutines")

        // Remove unused classes to keep JAR size reasonable
        minimize {
            // Keep Exposed DAO and all driver classes (minimize would otherwise strip them)
            exclude(dependency("org.jetbrains.exposed:.*"))
            exclude(dependency("org.xerial:sqlite-jdbc"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)

    compilerOptions {
        // Treat platform-type nullability from Java APIs as strict Kotlin nulls.
        // Paper's APIs are well-annotated, so this catches real bugs at compile time.
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            // Emit more precise null-checks into bytecode (catches more NPEs at runtime)
            "-Xnullability-annotations=@org.jetbrains.annotations:strict",
        )
    }
}

// ── Kotlin compilation performance ────────────────────────────────────────────
// Configure all KotlinCompile tasks lazily (task-configuration avoidance).
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        // Add opt-in for APIs that use @RequiresOptIn (e.g. kotlinx.coroutines experimental)
        optIn.add("kotlin.RequiresOptIn")
    }
}
