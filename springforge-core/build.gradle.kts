plugins {
	`java-library`
	kotlin("jvm")
	id("com.vanniktech.maven.publish")
}

dependencies {
	testImplementation(kotlin("test-junit5"))
	testImplementation("org.assertj:assertj-core:3.26.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

mavenPublishing {
	pom {
		name.set("Springforge Core")
		description.set("Core utilities and abstractions for the Springforge toolkit.")
	}
}
