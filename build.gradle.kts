group = "com.lordxarus"
version = "1.1.0"

var kotlinVersion = "1.3.21"

plugins {
    `java-library`
    `kotlin-dsl`
    kotlin("jvm") version "1.3.21"

    maven
    

}

java {
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

    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.3.21")

    implementation("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Main-Class"] = "com.lordxarus.autominer.AutoMiner"
    }

    val include = arrayOf(
            "kotlin-runtime-$kotlinVersion.jar",
            "kotlin-stdlib-$kotlinVersion.jar",
            "smart-invs-1.2.6.jar")

    
    from(configurations.runtimeClasspath.get()
            .filter { include.contains(it.name) }
            .map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}