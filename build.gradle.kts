plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

group = "io.github.popeye0618.springforge"
version = "0.1.0"
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

	plugins.withId("com.vanniktech.maven.publish") {
		configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
			publishToMavenCentral(automaticRelease = true)
			signAllPublications()

			pom {
				url.set("https://github.com/popeye0618/springforge")
				inceptionYear.set("2026")

				licenses {
					license {
						name.set("MIT License")
						url.set("https://opensource.org/licenses/MIT")
						distribution.set("https://opensource.org/licenses/MIT")
					}
				}

				developers {
					developer {
						id.set("popeye0618")
						name.set("Seunghwan Chun")
						email.set("popeye0618@gmail.com")
						url.set("https://github.com/popeye0618")
					}
				}

				scm {
					url.set("https://github.com/popeye0618/springforge")
					connection.set("scm:git:https://github.com/popeye0618/springforge.git")
					developerConnection.set("scm:git:ssh://git@github.com/popeye0618/springforge.git")
				}
			}
		}
	}
}
