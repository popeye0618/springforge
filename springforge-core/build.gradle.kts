plugins {
	`java-library`
	kotlin("jvm")
}

dependencies {
	testImplementation(kotlin("test-junit5"))
	testImplementation("org.assertj:assertj-core:3.26.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
