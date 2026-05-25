plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "io.github.popeye0618.springforge"
version = "0.1.0-SNAPSHOT"
description = "Springforge"

allprojects {
	repositories {
		mavenCentral()
	}
}

subprojects {
	group = rootProject.group
	version = rootProject.version

	plugins.withType<JavaPlugin> {
		extensions.configure<JavaPluginExtension> {
			toolchain {
				languageVersion = JavaLanguageVersion.of(21)
			}
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
