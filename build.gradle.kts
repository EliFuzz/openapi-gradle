import Build_gradle.ApiConfig.GROUP_ID
import Build_gradle.ApiConfig.VERSION
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


group = "com.genten.contract"
version = "1.0.0-SNAPSHOT"

val springBootVersion = "3.2.4"

plugins {
    val springBootVersion = "3.2.4"
    val kotlinVersion = "1.9.23"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.4"

    id("org.openapi.generator") version "7.4.0"

    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.2")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.21")
    implementation("io.swagger.core.v3:swagger-models:2.2.21")
}

// Plugin
object ApiConfig {
    const val VERSION = "1.0.0-SNAPSHOT"
    const val GROUP_ID = "com.genten.contract"
}

tasks {
    val contractInputSpec = "$projectDir/api.yml"
    val contractConfigs = mapOf(
        "dateLibrary" to "java8",
        "delegatePattern" to "true",
        "interfaceOnly" to "true",
        "serviceImplementation" to "false",
        "serviceInterface" to "true",
        "useJakartaEe" to "true",
        "useTags" to "true",
        "notNullJacksonAnnotation" to "true",
        "useSpringBoot3" to "true"
    )

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.majorVersion
        }
    }

    // Generate
    register<GenerateTask>("generateClient") {
        groupId = GROUP_ID
        id = "client"
        version = VERSION
        generatorName = "kotlin"
        library = "jvm-okhttp4"
        inputSpec = contractInputSpec
        outputDir = "${layout.buildDirectory.get()}/openapi/client"
        packageName = GROUP_ID
        apiPackage = "${GROUP_ID}.client.api"
        modelPackage = "${GROUP_ID}.client.model"
        configOptions = contractConfigs
        verbose = false
        validateSpec = true
        skipOverwrite = false
        generateModelTests = false
        generateModelDocumentation = true
        generateApiTests = false
        generateApiDocumentation = true
        generateAliasAsModel = false
    }

    register<GenerateTask>("generateServer") {
        groupId = GROUP_ID
        id = "server"
        version = VERSION
        generatorName = "kotlin-spring"
        library = "spring-boot"
        inputSpec = contractInputSpec
        outputDir = "${layout.buildDirectory.get()}/openapi/server"
        packageName = GROUP_ID
        apiPackage = "${GROUP_ID}.server.api"
        modelPackage = "${GROUP_ID}.server.model"
        configOptions = contractConfigs
        verbose = false
        validateSpec = true
        skipOverwrite = false
        generateModelTests = false
        generateModelDocumentation = true
        generateApiTests = false
        generateApiDocumentation = true
        generateAliasAsModel = false
    }

    // Source
    sourceSets {
        val clientPath = "${layout.buildDirectory.get()}/openapi/client/src"
        val serverPath = "${layout.buildDirectory.get()}/openapi/server/src"

        create("sourceClient") {
            kotlin.srcDir(clientPath)
        }

        create("sourceServer") {
            kotlin.srcDir(serverPath)
        }

        main {
            kotlin.srcDirs(clientPath, serverPath)
        }
    }

    // Jar
    register<Jar>("jarClient") {
        archiveClassifier.set("client")
        from(sourceSets["sourceClient"].kotlin)
    }

    register<Jar>("jarServer") {
        archiveClassifier.set("server")
        from(sourceSets["sourceServer"].kotlin)
    }

    // Publish
    publishing {
        publications {
            create<MavenPublication>("publishClient") {
                groupId = GROUP_ID
                artifactId = "client"
                version = VERSION
                artifact(project.tasks.getByName<Jar>("jarClient"))
            }
            create<MavenPublication>("publishServer") {
                groupId = GROUP_ID
                artifactId = "server"
                version = VERSION
                artifact(project.tasks.getByName<Jar>("jarServer"))
            }
        }
    }

    // Dependencies
    mapOf(
        "compileKotlin" to listOf("generateClient", "generateServer"),
        "jarClient" to listOf("generateClient"),
        "jarServer" to listOf("generateServer"),
        "jar" to listOf("jarClient", "jarServer"),
        "publish" to listOf("publishClient", "publishServer"),
        "compileSourceClientKotlin" to listOf("generateClient"),
        "compileSourceServerKotlin" to listOf("generateServer"),
    ).forEach { (task, dependsOn) -> getByName(task).dependsOn(dependsOn) }
}
