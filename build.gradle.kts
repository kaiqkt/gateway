import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainPkgAndClass = "com.kaiqkt.gateway.ApplicationKt"

plugins {
	application
	id("org.springframework.boot") version "2.7.7"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "com.kaiqkt.gateway"
version = "1.0.0"

repositories {

	mavenCentral()
}

extra["springCloudVersion"] = "2021.0.5"

configurations.implementation {
	exclude("org.springframework.boot", "spring-boot-starter-logging")
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	//spring
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")

	//fuel
	implementation("com.github.kittinunf.fuel:fuel:2.3.1")

	//jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	//logging
	implementation("org.slf4j:slf4j-api")
	implementation("org.slf4j:slf4j-simple")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

application {
	mainClass.set(mainPkgAndClass)
}

detekt {
	source = files("src/main/java", "src/main/kotlin")
	config = files("detekt/detekt.yml")
}

tasks.withType<CreateStartScripts> { mainClass.set(mainPkgAndClass) }

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	manifest {
		attributes("Main-Class" to mainPkgAndClass)
		attributes("Package-Version" to archiveVersion)
	}

	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	from(sourceSets.main.get().output)
}

tasks.withType<KotlinCompile> {
	java.sourceCompatibility = JavaVersion.VERSION_11
	java.targetCompatibility = JavaVersion.VERSION_11

	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
