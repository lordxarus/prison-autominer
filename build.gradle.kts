group = "com.lordxarus"
version = "1.0.0"

plugins {
    `java-library`
  //     `kotlin-dsl`
    id("com.github.johnrengelman.shadow") version "4.0.4"
    maven
    kotlin("jvm") version "1.3.21"
}

java {                                      // (4)
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://maven.sk89q.com/repo/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("http://repo.dmulloy2.net/nexus/repository/public/")
    maven("http://repo.citizensnpcs.co/")
}

dependencies {
    implementation(fileTree("libs"))
    implementation("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    implementation("com.comphenix.protocol:ProtocolLib:4.4.0")
    implementation("net.citizensnpcs:citizensapi:2.0.16-SNAPSHOT")
    implementation("fr.minuskube.inv:smart-invs:1.2.6")
    implementation("com.sk89q:worldguard:6.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

