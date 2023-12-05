plugins {
    `java-library`
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("io.spring.dependency-management") version "1.1.3"
    id("maven-publish")
}
group = "vip.xunmo"
version = "0.8.51"

repositories {
    mavenLocal()
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()

}

dependencyManagement {
    imports {
        mavenBom("org.noear:solon-parent:2.6.0")
    }
}

dependencies {

    api("org.babyfish.jimmer:jimmer-sql:0.8.51")
    api("org.babyfish.jimmer:jimmer-sql-kotlin:0.8.51")
    api("org.babyfish.jimmer:jimmer-client:0.8.51")

    testAnnotationProcessor("org.babyfish.jimmer:jimmer-apt:0.8.51")
    kspTest("org.babyfish.jimmer:jimmer-ksp:0.8.51")

    api("org.noear:solon")
    api("org.noear:solon-lib")
    api("org.noear:solon.data")
    api("org.noear:solon.web.cors")
    api("org.noear:solon.boot.jlhttp")
    api("org.noear:solon.serialization.jackson")
    api("org.noear:solon.scheduling.simple")
    api("org.noear:logback-solon-plugin")
    api("org.noear:solon.cache.redisson")
    api("cn.hutool:hutool-all:5.8.20")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testApi("com.zaxxer:HikariCP:4.0.3")
    testApi("org.noear:solon-test")

    compileOnly("com.github.ben-manes.caffeine:caffeine:2.9.1")

    testApi("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testApi("com.h2database:h2:2.1.212")
}

kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

//tasks.getByName<Test>("test") {
//    useJUnitPlatform()
//}

tasks.withType<Javadoc> {
    enabled = false
    options.encoding = "UTF-8"
}

//本地发布
publishing {

    // 定义发布什么
    publications {
        create<MavenPublication>("maven") {
            groupId = groupId
            artifactId = artifactId
            version = version

            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    /*
     * it must be compiled with parameters
     * when using @ConstructorBinding in Spring Native Image
     */
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Ajimmer.dto.dirs=src/test/dto")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

ksp {
    arg("jimmer.source.excludes", "org.babyfish.jimmer.spring.java")
    arg("jimmer.dto.dirs", "src/test/dto")
}
