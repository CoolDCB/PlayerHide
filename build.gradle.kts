plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "me.dave"
version = "1.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") } // Spigot
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") } // PacketEvents
    maven { url = uri("https://repo.smrt-1.com/releases/") } // PlatyUtils
    maven { url = uri("https://repo.smrt-1.com/snapshots/") } // PlatyUtils
    maven { url = uri("https://maven.enginehub.org/repo/") } // WorldGuard
    maven { url = uri("https://jitpack.io") } // ChatColorHandler, EntityLib
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper.packetevents:spigot:2.2.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    implementation("com.github.Tofaa2.EntityLib:spigot:2.1.0-SNAPSHOT")
    implementation("me.dave:PlatyUtils:0.1.0.53")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("me.dave.chatcolorhandler", "me.dave.playerhide.libraries.chatcolor")
        relocate("me.dave.platyutils", "me.dave.playerhide.libraries.platyutils")
        relocate("com.github.Tofaa2", "me.dave.playerhide.libraries.tofaa2")

        minimize()

        val folder = System.getenv("pluginFolder_1-20")
        if (folder != null) destinationDirectory.set(file(folder))
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}