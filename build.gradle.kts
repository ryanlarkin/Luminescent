plugins {
	java
	idea
}

buildscript {
	extra["lwjglVersion"] = "3.4.1"
	extra["jomlVersion"] = "1.10.8"
	extra["gsonVersion"] = "2.13.2"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:${project.extra["gsonVersion"]}")
	implementation(platform("org.lwjgl:lwjgl-bom:${project.extra["lwjglVersion"]}"))

	implementation("org.lwjgl:lwjgl")
	implementation("org.lwjgl:lwjgl-glfw")
	implementation("org.lwjgl:lwjgl-jemalloc")
	implementation("org.lwjgl:lwjgl-openal")
	implementation("org.lwjgl:lwjgl-stb")
	implementation("org.lwjgl:lwjgl-vulkan")
	implementation("org.lwjgl:lwjgl-vma")

	listOf("natives-linux", "natives-macos", "natives-macos-arm64", "natives-windows")
		.forEach { platform ->
			runtimeOnly("org.lwjgl:lwjgl::$platform")
			runtimeOnly("org.lwjgl:lwjgl-glfw::$platform")
			runtimeOnly("org.lwjgl:lwjgl-jemalloc::$platform")
			runtimeOnly("org.lwjgl:lwjgl-openal::$platform")
			runtimeOnly("org.lwjgl:lwjgl-stb::$platform")
			runtimeOnly("org.lwjgl:lwjgl-vma::$platform")
		}
	listOf("natives-macos", "natives-macos-arm64").forEach { platform ->
		runtimeOnly("org.lwjgl:lwjgl-vulkan::$platform")
	}

	implementation("org.joml:joml:${project.extra["jomlVersion"]}")
}

tasks.jar {
	manifest {
		attributes(Pair("Main-Class", "astechzgo.luminescent.main.Main"))
	}

	from({
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		configurations.runtimeClasspath.get().map { if(it.isDirectory) it else zipTree(it) }
	})
}

// Don't add resources to JAR again
sourceSets {
	main {
		resources {
			exclude("*")
		}
	}
}

tasks.processResources {
	from("src/main/resources") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		into ("resources")
	}
}

apply(from = "shaders.gradle.kts")

tasks.register<JavaExec>("run") {
	group = "application"
	classpath = files(tasks.jar)
	mainClass = "astechzgo.luminescent.main.Main"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

tasks.wrapper {
	gradleVersion = "9.4.1"
	distributionType = Wrapper.DistributionType.ALL
}
