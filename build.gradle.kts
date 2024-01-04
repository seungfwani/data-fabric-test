plugins {
    id("java")
    id("org.springframework.boot") version "3.2.1"
    antlr
}
apply(plugin = "io.spring.dependency-management")

group = "pe.fwani"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.jsqlparser:jsqlparser:4.7")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    antlr("org.antlr:antlr:4.9.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}