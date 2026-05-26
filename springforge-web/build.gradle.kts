plugins {
	`java-library`
	kotlin("jvm")
	kotlin("plugin.spring")
	id("io.spring.dependency-management")
	id("com.vanniktech.maven.publish")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.14")
	}
}

dependencies {
	api(project(":springforge-core"))

	compileOnly("org.springframework.boot:spring-boot-autoconfigure")
	compileOnly("org.springframework:spring-web")
	compileOnly("jakarta.validation:jakarta.validation-api")
	compileOnly("jakarta.servlet:jakarta.servlet-api")

	testImplementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	testImplementation(kotlin("test-junit5"))

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

mavenPublishing {
	pom {
		name.set("Springforge Web")
		description.set("Spring Web integrations and auto-configurations for the Springforge toolkit.")
	}
}
