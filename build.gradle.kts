group = "io.github.wldt"
version = "0.5.0"
description = "The core library to build White Label Digital Twins"
java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
    id("com.vanniktech.maven.publish") version "0.35.0"
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    mavenLocal()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    //testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    //testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}

tasks.named<Test>("test") {
    //useJUnitPlatform()
    enabled = false
}

java {
    //withJavadocJar() // Removed to avoid double signing
    withSourcesJar()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

// ✅ FIX ESATTO PER GRADLE 9.2.1 + vanniktech.maven.publish 0.35.0
afterEvaluate {
    val plainJavadocJarTask = tasks.findByName("plainJavadocJar")
    val metadataTask = tasks.findByName("generateMetadataFileForMavenPublication")

    if (plainJavadocJarTask != null && metadataTask != null) {
        metadataTask.dependsOn(plainJavadocJarTask)
    }
}

mavenPublishing {
    coordinates(group.toString(), name.toString(), version.toString())

    pom {
        name.set("WLDT Core")
        description.set("The WLDT Core Java Module to build Digital Twins")
        inceptionYear.set("2025")
        url.set("https://github.com/wldt/wldt-core-java")
        licenses {
            license {
                name.set("WLDT License")
                url.set("https://github.com/wldt/wldt-core-java/blob/main/LICENSE")
                distribution.set("https://github.com/wldt/wldt-core-java/blob/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("piconem")
                name.set("Marco Picone")
                url.set("https://github.com/piconem")
            }
            developer {
                id.set("samubura")
                name.set("Samuele Burattini")
                url.set("https://github.com/samubura")
            }
        }
        scm {
            url.set("https://github.com/wldt/wldt-core-java")
            connection.set("scm:git:git://github.com/wldt/wldt-core-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/wldt/wldt-core-java.git")
        }
    }
}