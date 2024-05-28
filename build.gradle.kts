plugins {
	java
	idea
}

buildscript {
	extra["lwjglVersion"] = "3.3.0"
	extra["jomlVersion"] = "1.10.3"
	extra["gsonVersion"] = "2.8.9"
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
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-vulkan", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-macos")

	implementation("org.joml", "joml", project.extra["jomlVersion"] as String)
}

task<Jar>("deploy") {
	manifest {
		attributes(Pair("Main-Class", "astechzgo.luminescent.main.Main"))
	}

	from({
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		configurations.runtimeClasspath.get().map { if(it.isDirectory) it else zipTree(it) }
	})
	with(tasks["jar"] as CopySpec)
}.finalizedBy("copyToRoot")

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

// Copy to base directory
task<Copy>("copyToRoot") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(tasks.findByName("deploy"))
	into(projectDir)
	doNotTrackState("Final copy outside of build directory")
}

task("run") {
	doLast {
		javaexec {
			classpath("Luminescent.jar")
		}
	}
}.dependsOn("deploy")

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

tasks.wrapper {
	gradleVersion = "8.7"
}
