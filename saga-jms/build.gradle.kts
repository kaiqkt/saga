plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":saga-core"))
    implementation("javax.jms:javax.jms-api:2.0.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
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
        create<MavenPublication>("jms") {
            from(components["java"])
            groupId = "com.kaiqkt"
            artifactId = "saga-jms"
            version = "1.0.0"
        }
    }
}
