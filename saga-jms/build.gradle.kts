plugins {
    `java-library`
}

dependencies {
    implementation(project(":saga-core"))
    implementation("javax.jms:javax.jms-api:2.0.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}