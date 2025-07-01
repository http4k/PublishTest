import groovy.namespace.QName
import groovy.util.Node
import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "org.http4k.test"

buildscript {
    repositories {
        mavenCentral()
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = false
            jvmTarget.set(JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = VERSION_21
        targetCompatibility = VERSION_21
    }
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.vanniktech.maven.publish")

    mavenPublishing {
        configure<PublishingExtension> {
            publishToMavenCentral(automaticRelease = true)

            coordinates("com.example.mylibrary", "mylibrary-runtime", "1.0.3-SNAPSHOT")
            pom {
                val archivesBaseName = tasks.jar.get().archiveBaseName.get()
                withXml {
                    asNode().appendNode("name", archivesBaseName)
                    asNode().appendNode("description", description)
                    asNode().appendNode("url", "https://http4k.dev")
                    asNode().appendNode("developers")
                        .appendNode("developer").appendNode("name", "David Denton").parent()
                        .appendNode("email", "david@http4k.org")
                    asNode().appendNode("scm").appendNode("url", "git@github.com:http4k/PublishTest.git")
                        .parent()
                        .appendNode("connection", "scm:git:git@github.com:http4k/PublishTest.git").parent()
                        .appendNode("developerConnection", "scm:git:git@github.com:http4k/PublishTest.git")
                    asNode().appendNode("licenses").appendNode("license")
                        .appendNode("name", "Apache License, Version 2.0")
                        .parent().appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.html")
                }

                // replace all runtime dependencies with provided
                withXml {
                    asNode()
                        .childrenCalled("dependencies")
                        .flatMap { it.childrenCalled("dependency") }
                        .flatMap { it.childrenCalled("scope") }
                        .forEach { if (it.text() == "runtime") it.setValue("provided") }
                }
            }
        }
    }
}

fun Node.childrenCalled(wanted: String) = children()
    .filterIsInstance<Node>()
    .filter {
        val name = it.name()
        (name is QName) && name.localPart == wanted
    }
