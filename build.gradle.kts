plugins {
    id("java")
    id("org.springframework.boot") version "3.2.1"
    antlr
}
apply(plugin = "io.spring.dependency-management")

group = "pe.fwani"
version = "1.0-SNAPSHOT"

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "${buildDir}/generated/sources/antlr/main")
        }
    }
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.jsqlparser:jsqlparser:4.7")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    antlr("org.antlr:antlr4:4.11.1")
    implementation("org.antlr:antlr4-runtime:4.11.1")


    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf(
        "-visitor",
        "-long-messages",
        "-package", "pe.fwani.antlr"
    )
    outputDirectory = file("${buildDir}/generated/sources/antlr/main")
}


tasks.test {
    useJUnitPlatform()
}