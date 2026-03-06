package com.docta.drpc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.net.URL
import java.util.Collection
import java.util.jar.JarFile

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

        // Set the paths on transformer (Shadow versions differ: setPaths(Collection), setPaths(Set), or getPaths()+mutate)
        val pathsSet = servicePaths.toSet()
        val pathsList = servicePaths.toList()

        val setPathsMethods = transformerClass.methods
            .filter { m -> m.name == "setPaths" && m.parameterCount == 1 }

        fun tryInvokeSetPaths(arg: Any): Boolean {
            for (m in setPathsMethods) {
                val ok = runCatching { m.invoke(transformer, arg) }.isSuccess
                if (ok) return true
            }
            return false
        }

        var pathsSetOk: Boolean

        // Strategy A: invoke any setPaths(X) with Set or List
        pathsSetOk = tryInvokeSetPaths(pathsSet) || tryInvokeSetPaths(pathsList)

        // Strategy B: getPaths()+mutate if available
        if (!pathsSetOk) {
            val getPathsMethod = transformerClass.methods.firstOrNull { m ->
                (m.name == "getPaths" || m.name == "paths") && m.parameterCount == 0
            }

            if (getPathsMethod != null) {
                val current = runCatching { getPathsMethod.invoke(transformer) }.getOrNull()
                val mutated = when (current) {
                    is MutableCollection<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        (current as MutableCollection<String>).addAll(pathsSet)
                    }
                    is Collection<*> -> {
                        // Might be mutable even if typed as Collection
                        runCatching {
                            @Suppress("UNCHECKED_CAST")
                            (current as Collection<String>).addAll(pathsSet)
                        }.getOrDefault(false)
                    }
                    else -> false
                }

                if (mutated) pathsSetOk = true
            }
        }

        // Strategy C: last resort – try writing field named 'paths'
        if (!pathsSetOk) {
            val field = transformerClass.declaredFields.firstOrNull { it.name == "paths" }
            if (field != null) {
                pathsSetOk = runCatching {
                    field.isAccessible = true
                    field.set(transformer, pathsSet)
                }.isSuccess
            }
        }

        if (!pathsSetOk) return false

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

        // Keep only the merged dRPC service descriptors from `outDir`.
        val outDirPrefix = outDir.absolutePath + File.separator
        jar.eachFile { details ->
            val relPath = details.relativePath.pathString.replace('\\', '/')
            if (relPath in drpcServiceFiles) {
                val src = details.file.absolutePath
                val fromMergedDir = src.startsWith(outDirPrefix)
                if (!fromMergedDir) details.exclude()
            }
        }

        jar.from(outDir)
    }

    private fun resolveRuntimeClasspathUrls(project: Project): List<URL> {
        // Collect dependency jars/dirs from the most likely runtime classpath configurations.
        val configNames = listOf(
            "runtimeClasspath",
            "jvmRuntimeClasspath",
        )

        val cpFiles = configNames
            .firstNotNullOfOrNull { project.configurations.findByName(it) }
            ?.resolve()
            .orEmpty()

        val urls = mutableListOf<URL>()
        urls += cpFiles.map { it.toURI().toURL() }

        // Also include this module's own outputs so we can see generated META-INF/services descriptors.
        val extraDirs = listOf(
            project.layout.buildDirectory.dir("resources/main").get().asFile,
            project.layout.buildDirectory.dir("generated/ksp/main/resources").get().asFile,
            project.layout.buildDirectory.dir("generated/ksp/main/kotlin").get().asFile,
            project.layout.buildDirectory.dir("classes/kotlin/main").get().asFile,
        )

        extraDirs
            .filter { it.exists() }
            .forEach { urls += it.toURI().toURL() }

        return urls.distinct()
    }

    private fun collectServiceFileLines(classpathUrls: List<URL>, servicePath: String): List<String> {
        val seen = LinkedHashSet<String>()

        for (url in classpathUrls) {
            // Handle both directories and jars
            val file = runCatching { File(url.toURI()) }.getOrNull() ?: continue
            if (file.isDirectory) {
                val candidate = file.resolve(servicePath)
                if (candidate.isFile) {
                    addLines(text = candidate.readText(Charsets.UTF_8), seen = seen)
                }
            } else if (file.isFile && file.extension == "jar") {
                JarFile(file).use { jar ->
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