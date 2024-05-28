import org.lwjgl.util.shaderc.Shaderc
import org.gradle.api.InvalidUserCodeException

buildscript {
    // From LWJGL website
    val lwjglNatives = Pair(
        System.getProperty("os.name")!!,
        System.getProperty("os.arch")!!
    ).let { (name, arch) ->
        when {
            arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
                if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                    "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
                else if (arch.startsWith("ppc"))
                    "natives-linux-ppc64le"
                else if (arch.startsWith("riscv"))
                    "natives-linux-riscv64"
                else
                    "natives-linux"

            arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
                "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"

            arrayOf("Windows").any { name.startsWith(it) } ->
                if (arch.contains("64"))
                    "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
                else
                    "natives-windows-x86"

            else ->
                throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
        }
    }

    dependencies {
        classpath(platform("org.lwjgl:lwjgl-bom:${project.extra["lwjglVersion"]}"))
        classpath("org.lwjgl", "lwjgl")
        classpath("org.lwjgl", "lwjgl", classifier = lwjglNatives)
        classpath("org.lwjgl", "lwjgl-shaderc")
        classpath("org.lwjgl", "lwjgl-shaderc", classifier = lwjglNatives)
    }

    repositories {
        mavenCentral()
    }
}

tasks {
    getByName<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }
        eachFile {
            if (!isDirectory && name.length > 5 && relativePath.startsWith("resources/shaders") && relativePath.endsWith(
                    ".glsl",
                    ignoreCase = true
                )
            ) {
                val newPath = path.substring(0, path.length - 4) + "spv"
                val program = file.readText()
                exclude()
                val out = File(destinationDir, newPath)

                val compiler = Shaderc.shaderc_compiler_initialize()
                val options = Shaderc.shaderc_compile_options_initialize()
                Shaderc.shaderc_compile_options_set_warnings_as_errors(options)
                val result = Shaderc.shaderc_compile_into_spv(
                    compiler,
                    program,
                    Shaderc.shaderc_glsl_infer_from_source,
                    name,
                    "main",
                    options
                )

                if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
                    throw InvalidUserCodeException(Shaderc.shaderc_result_get_error_message(result) ?: "unknown error")
                }

                val output = Shaderc.shaderc_result_get_bytes(result)

                out.parentFile.mkdirs()
                if (out.exists()) out.delete()
                out.createNewFile()
                val fc = out.outputStream().channel
                fc.write(output)
                fc.close()

                Shaderc.shaderc_result_release(result)
                Shaderc.shaderc_compile_options_release(options)
                Shaderc.shaderc_compiler_release(compiler)
            }
        }
    }
}
