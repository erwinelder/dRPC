package com.docta.drpc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import java.net.URL

class DrpcShadowMergePlugin : Plugin<Project> {

    private val drpcServiceFiles = setOf(
        "META-INF/services/com.docta.drpc.server.DrpcBinderRegistryProvider",
        "META-INF/services/com.docta.drpc.client.DrpcClientFactoryRegistryProvider",
    )

    override fun apply(target: Project) = with(target) {
        val jarTaskNames = setOf("shadowJar", "fatJar", "uberJar")

        tasks.withType(Jar::class.java).configureEach { jar ->
            if (jar.name !in jarTaskNames) return@configureEach

            jar.duplicatesStrategy = DuplicatesStrategy.INCLUDE

            val shadowConfigured = jar.tryConfigureShadowOnlyDrpcServiceMerge(drpcServiceFiles)
            if (shadowConfigured) return@configureEach

            configureManualDrpcServiceMerge(jar = jar)
        }
    }

    private fun Jar.tryConfigureShadowOnlyDrpcServiceMerge(servicePaths: Set<String>): Boolean {
        // Ensure task has "transform(Object)" method (ShadowJar does)
        val transformMethod = this::class.java.methods.firstOrNull { m ->
            m.name == "transform" && m.parameterCount == 1
        } ?: return false

        // Try to load ServiceFileTransformer class
        val transformerClassNames = listOf(
            "com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer",
            "com.gradleup.shadow.transformers.ServiceFileTransformer",
        )

        val transformerClass = transformerClassNames.firstNotNullOfOrNull { name ->
            runCatching { Class.forName(name) }.getOrNull()
        } ?: return false

        val transformer = runCatching { transformerClass.getDeclaredConstructor().newInstance() }.getOrNull()
            ?: return false

        // Set the paths on transformer
        val setPathsMethod = transformerClass.methods.firstOrNull { m ->
            m.name == "setPaths" &&
                    m.parameterCount == 1 &&
                    java.util.Set::class.java.isAssignableFrom(m.parameterTypes[0])
        }

        val pathsSetOk = when {
            setPathsMethod != null -> runCatching {
                setPathsMethod.invoke(transformer, servicePaths)
            }.isSuccess

            else -> {
                // Try writing field / Kotlin property backing field if present
                val field = transformerClass.declaredFields.firstOrNull { it.name == "paths" }
                if (field != null) {
                    runCatching {
                        field.isAccessible = true
                        field.set(transformer, servicePaths)
                    }.isSuccess
                } else false
            }
        }

        if (!pathsSetOk) return false

        // Install transformer into the task
        return runCatching { transformMethod.invoke(this, transformer) }.isSuccess
    }

    private fun Project.configureManualDrpcServiceMerge(jar: Jar) {
        val outDir = layout.buildDirectory
            .dir("generated/drpcServiceMerge/${jar.name}")
            .get()
            .asFile

        jar.doFirst {
            outDir.deleteRecursively()
            outDir.mkdirs()

            val urls = resolveRuntimeClasspathUrls(project = project)
            for (servicePath in drpcServiceFiles) {
                val lines = collectServiceFileLines(classpathUrls = urls, servicePath = servicePath)
                if (lines.isEmpty()) continue

                val targetFile = outDir.resolve(servicePath)
                targetFile.parentFile.mkdirs()
                targetFile.writeText(lines.joinToString("\n") + "\n", Charsets.UTF_8)
            }
        }

        jar.from(outDir)
    }

    private fun resolveRuntimeClasspathUrls(project: Project): List<URL> {
        // JVM projects: main runtimeClasspath is a good source for service descriptors.
        val cp = project.configurations.findByName("runtimeClasspath")?.resolve().orEmpty()
        return cp.map { it.toURI().toURL() }
    }

    private fun collectServiceFileLines(classpathUrls: List<URL>, servicePath: String): List<String> {
        val seen = LinkedHashSet<String>()

        for (url in classpathUrls) {
            // Handle both directories and jars
            val file = runCatching { java.io.File(url.toURI()) }.getOrNull() ?: continue
            if (file.isDirectory) {
                val candidate = file.resolve(servicePath)
                if (candidate.isFile) addLines(text = candidate.readText(Charsets.UTF_8), seen = seen)
            } else if (file.isFile && file.extension == "jar") {
                java.util.jar.JarFile(file).use { jar ->
                    val entry = jar.getJarEntry(servicePath) ?: return@use
                    jar.getInputStream(entry).use { input ->
                        val text = input.readBytes().toString(Charsets.UTF_8)
                        addLines(text = text, seen = seen)
                    }
                }
            }
        }
        return seen.toList()
    }

    private fun addLines(text: String, seen: LinkedHashSet<String>) {
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .forEach { seen.add(it) }
    }

}