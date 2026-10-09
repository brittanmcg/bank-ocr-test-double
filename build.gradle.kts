plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Target the current LTS release so the build runs on any JDK 25+.
tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

application {
    mainClass = "bankocr.Main"
    applicationName = "bank-ocr"
}

tasks.test {
    useJUnitPlatform()
}
