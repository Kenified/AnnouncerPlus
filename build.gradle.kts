plugins {
  kotlin("jvm") version "1.8.0"
  alias(libs.plugins.indra)
  alias(libs.plugins.indraGit)
  alias(libs.plugins.indraLicenseHeader)
  alias(libs.plugins.runPaper)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.shadow)
  alias(libs.plugins.pluginYmlBukkit)
}

repositories {
  mavenCentral()
  sonatype.s01Snapshots()
  sonatype.ossSnapshots()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.jpenilla.xyz/snapshots/")
  maven("https://maven.fabricmc.net/")
  maven("https://repo.essentialsx.net/releases/")
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    content { includeGroup("me.clip") }
  }
  maven("https://jitpack.io") {
    content { includeGroupByRegex("com\\.github\\..*") }
  }
  maven ("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
  implementation(platform(kotlin("bom")))

  compileOnly("com.destroystokyo.paper", "paper-api", "1.13.2-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
  compileOnly("net.essentialsx", "EssentialsX", "2.19.7") {
    isTransitive = false
  }
  compileOnly("me.clip", "placeholderapi", "2.11.2")
  compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT")
  
  implementation(platform("net.kyori:adventure-bom:4.12.0"))
  implementation("net.kyori", "adventure-extra-kotlin")
  implementation("net.kyori", "adventure-serializer-configurate4")
  
  implementation(platform("cloud.commandframework:cloud-bom:1.8.0"))
  implementation("cloud.commandframework", "cloud-paper")
  implementation("cloud.commandframework", "cloud-kotlin-extensions")
  implementation("cloud.commandframework", "cloud-minecraft-extras")
  
  implementation(platform("org.spongepowered:configurate-bom:4.1.2"))
  implementation("org.spongepowered", "configurate-hocon")
  implementation("org.spongepowered", "configurate-extra-kotlin")
  
  implementation("io.insert-koin", "koin-core", "3.3.2")
  implementation("xyz.jpenilla", "legacy-plugin-base", "0.0.1+73-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "3.0.0")
  implementation("io.papermc", "paperlib", "1.0.8")
  
  implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
  
}

version = (version as String).decorateVersion()

java {
  disableAutoTargetJvm()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  jar {
    archiveClassifier.set("not-shadowed")
  }
  shadowJar {
    from(rootProject.file("license.txt")) {
      rename { "license_${rootProject.name.toLowerCase()}.txt" }
    }

    minimize()
    archiveClassifier.set(null as String?)
    archiveBaseName.set(project.name) // Use uppercase name for final jar

    val prefix = "${project.group}.${project.name.toLowerCase()}.lib"
    sequenceOf(
      "com.typesafe.config",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "xyz.jpenilla.pluginbase",
      "cloud.commandframework",
      "org.koin",
      "org.spongepowered.configurate",
      "org.bstats",
      "kotlin",
      "xyz.jpenilla.reflectionremapper",
      "net.fabricmc.mappingio"
    ).forEach { pkg ->
      relocate(pkg, "$prefix.$pkg")
    }

    dependencies {
      exclude(dependency("org.jetbrains:annotations"))
    }
  }
  assemble {
    dependsOn(shadowJar)
  }
  runServer {
    minecraftVersion("1.19.3")
    javaLauncher.set(
      project.javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
      }
    )
  }
  register("format") {
    group = "formatting"
    description = "Formats source code according to project style."
    dependsOn(licenseFormat, ktlintFormat)
  }
}

bukkit {
  main = "xyz.jpenilla.announcerplus.AnnouncerPlus"
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/AnnouncerPlus"
  authors = listOf("jmp")
  depend = listOf("Vault")
  softDepend = listOf("PlaceholderAPI", "Essentials", "ViaVersion")
}

fun String.decorateVersion(): String =
  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
