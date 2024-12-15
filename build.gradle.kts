plugins {
	java
	idea
}

buildscript {
	extra["lwjglVersion"] = "3.3.5"
	extra["jomlVersion"] = "1.10.8"
	extra["gsonVersion"] = "2.11.0"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:${project.extra["gsonVersion"]}")
	implementation(platform("org.lwjgl:lwjgl-bom:${project.extra["lwjglVersion"]}"))

	implementation("org.lwjgl", "lwjgl")
	implementation("org.lwjgl", "lwjgl-glfw")
	implementation("org.lwjgl", "lwjgl-jemalloc")
	implementation("org.lwjgl", "lwjgl-openal")
	implementation("org.lwjgl", "lwjgl-stb")
	implementation("org.lwjgl", "lwjgl-vulkan")
	implementation("org.lwjgl", "lwjgl-vma")
	listOf("natives-linux", "natives-macos", "natives-macos-arm64", "natives-windows")
		.forEach { platform ->
			runtimeOnly("org.lwjgl", "lwjgl", classifier = platform)
			runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = platform)
			runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = platform)
			runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = platform)
			runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = platform)
			runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = platform)
		}
	listOf("natives-macos", "natives-macos-arm64").forEach { platform ->
		runtimeOnly("org.lwjgl", "lwjgl-vulkan", classifier = platform)
	}

	implementation("org.joml", "joml", project.extra["jomlVersion"] as String)
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

task("run") {
	doLast {
		providers.javaexec {
			classpath(tasks.jar)
		}
	}
}.dependsOn(tasks.jar)

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

tasks.wrapper {
	gradleVersion = "8.11.1"
	distributionType = Wrapper.DistributionType.ALL
}
