plugins {
    id("java")
    id("jacoco")
    id("maven-publish")
}

allprojects {

    group = "com.kaiqkt"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")

    dependencies {
        implementation("io.azam.ulidj:ulidj:1.0.4")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
        testImplementation("org.mockito:mockito-core:5.11.0")
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
        useJUnitPlatform()
    }

    tasks.jacocoTestReport{
        dependsOn(tasks.test)

        reports {
            html.required.set(true)
        }
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/kaiqkt/saga")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
                }
            }
        }
        publications {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
        }
    }
}