plugins {
    java

    id("com.diffplug.spotless") version "6.3.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.jbduncan"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.flogger:flogger:0.7.4")
    // implementation("com.google.flogger:flogger-system-backend:0.7.4")
    implementation("com.google.flogger:flogger-slf4j-backend:0.7.4")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("org.hsqldb:hsqldb:2.5.1")
    implementation("org.jdbi:jdbi3-core:3.27.2")
    constraints {
        implementation("com.github.ben-manes.caffeine:caffeine") {
            version {
                strictly("2.9.3")
            }
        }
    }

    runtimeOnly("org.slf4j:slf4j-simple:1.7.36")

    compileOnly("com.google.auto.value:auto-value-annotations:1.9")

    annotationProcessor("com.google.auto.value:auto-value:1.9")

    testCompileOnly("com.google.auto.value:auto-value-annotations:1.9")

    testAnnotationProcessor("com.google.auto.value:auto-value:1.9")

    testImplementation("com.google.jimfs:jimfs:1.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:4.3.1")
    testImplementation("org.mockito:mockito-inline:4.3.1")
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "me.jbduncan.creditsuisse.interviewchallenge.App"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.7")
    }
}
