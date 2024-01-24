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
            srcDirs( "${buildDir}/generated/sources/antlr/main")
        }
    }
}
repositories {
    mavenCentral()
}

configurations {
    all {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.jsqlparser:jsqlparser:4.7")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    antlr("org.antlr:antlr4:4.11.1")
    implementation("org.antlr:antlr4-runtime:4.11.1")
    implementation("org.json:json:20231013")

    implementation("org.apache.spark:spark-core_2.13:3.5.0")
    implementation("org.apache.spark:spark-sql_2.13:3.5.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    outputDirectory = file("${buildDir}/generated/sources/antlr/main/pe/fwani/antlr")
}


tasks.test {
    useJUnitPlatform()
}