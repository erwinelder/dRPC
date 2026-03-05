package com.docta.drpc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.logging.Logger
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

            jar.project.logger.lifecycle(
                "[dRPC] Configuring fat jar task '${jar.path}' (type=${jar::class.java.name}). " +
                        "Will merge only: ${drpcServiceFiles.joinToString()}"
            )

            jar.duplicatesStrategy = DuplicatesStrategy.INCLUDE

            val shadowConfigured = jar.tryConfigureShadowOnlyDrpcServiceMerge(drpcServiceFiles)
            jar.project.logger.lifecycle(
                "[dRPC] Task '${jar.path}': shadow-transformer-configured=$shadowConfigured"
            )
            if (shadowConfigured) {
                jar.project.logger.lifecycle(
                    "[dRPC] Task '${jar.path}': using Shadow ServiceFileTransformer (restricted to dRPC descriptors)."
                )
                return@configureEach
            }

            jar.project.logger.lifecycle(
                "[dRPC] Task '${jar.path}': Shadow transformer not available; falling back to manual merge for dRPC descriptors."
            )

            configureManualDrpcServiceMerge(jar = jar)
        }
    }

    private fun Jar.tryConfigureShadowOnlyDrpcServiceMerge(servicePaths: Set<String>): Boolean {
        val logger: Logger = this.project.logger
        logger.info("[dRPC] '${this.path}': attempting Shadow-only service merge; paths=${servicePaths.joinToString()}")

        // Ensure task has "transform(Object)" method (ShadowJar does)
        val transformMethod = this::class.java.methods.firstOrNull { m ->
            m.name == "transform" && m.parameterCount == 1
        } ?: run {
            logger.info("[dRPC] '${this.path}': no transform(Object) method found; not a ShadowJar-like task")
            return false
        }
        logger.info("[dRPC] '${this.path}': found transform(Object) method; task looks ShadowJar-like")

        // Try to load ServiceFileTransformer class
        val transformerClassNames = listOf(
            "com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer",
            "com.gradleup.shadow.transformers.ServiceFileTransformer",
        )

        val transformerClass = transformerClassNames.firstNotNullOfOrNull { name ->
            runCatching { Class.forName(name) }.getOrNull()
        } ?: run {
            logger.info(
                "[dRPC] '${this.path}': ServiceFileTransformer class not found (tried: ${transformerClassNames.joinToString()})"
            )
            return false
        }
        logger.info("[dRPC] '${this.path}': using ServiceFileTransformer=${transformerClass.name}")

        val transformer = runCatching { transformerClass.getDeclaredConstructor().newInstance() }.getOrNull()
            ?: run {
                logger.info("[dRPC] '${this.path}': failed to instantiate ServiceFileTransformer")
                return false
            }

        // Set the paths on transformer (Shadow versions differ: setPaths(Collection), setPaths(Set), or getPaths()+mutate)
        val pathsSet = servicePaths.toSet()
        val pathsList = servicePaths.toList()

        val setPathsMethods = transformerClass.methods
            .filter { m -> m.name == "setPaths" && m.parameterCount == 1 }

        logger.info(
            "[dRPC] '${this.path}': setPaths(any) methods found=${setPathsMethods.size}; " +
                    "willTryInvokeWith=Set then List"
        )

        fun tryInvokeSetPaths(arg: Any): Boolean {
            for (m in setPathsMethods) {
                val ok = runCatching { m.invoke(transformer, arg) }.isSuccess
                if (ok) {
                    logger.info(
                        "[dRPC] '${this.path}': configured ServiceFileTransformer via ${m.name}(${m.parameterTypes[0].name})"
                    )
                    return true
                }
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

                if (mutated) {
                    logger.info("[dRPC] '${this.path}': configured ServiceFileTransformer by mutating getPaths() collection")
                    pathsSetOk = true
                } else {
                    logger.info("[dRPC] '${this.path}': getPaths() present but not mutable/compatible (type=${current?.javaClass?.name})")
                }
            } else {
                logger.info("[dRPC] '${this.path}': no getPaths()/paths() accessor found for mutation")
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

                if (pathsSetOk) {
                    logger.info("[dRPC] '${this.path}': configured ServiceFileTransformer via field(paths)")
                }
            }
        }

        if (!pathsSetOk) {
            logger.info("[dRPC] '${this.path}': failed to set ServiceFileTransformer paths via all strategies; aborting Shadow config")
            return false
        }

        logger.info("[dRPC] '${this.path}': configured ServiceFileTransformer paths successfully")

        val installed = runCatching { transformMethod.invoke(this, transformer) }.isSuccess
        logger.info("[dRPC] '${this.path}': installed ServiceFileTransformer into task: $installed")
        return installed
    }

    private fun Project.configureManualDrpcServiceMerge(jar: Jar) {
        val outDir = layout.buildDirectory
            .dir("generated/drpcServiceMerge/${jar.name}")
            .get()
            .asFile

        jar.project.logger.info("[dRPC] '${jar.path}': manual merge output dir: ${outDir.absolutePath}")

        jar.doFirst {
            jar.project.logger.lifecycle("[dRPC] '${jar.path}': running manual merge for dRPC ServiceLoader descriptors")

            outDir.deleteRecursively()
            outDir.mkdirs()

            val urls = resolveRuntimeClasspathUrls(project = project)
            jar.project.logger.lifecycle("[dRPC] '${jar.path}': classpath roots scanned (count=${urls.size})")
            urls.take(25).forEach { jar.project.logger.info("[dRPC] '${jar.path}': cp -> $it") }
            if (urls.size > 25) jar.project.logger.info("[dRPC] '${jar.path}': cp -> ... (${urls.size - 25} more)")

            for (servicePath in drpcServiceFiles) {
                jar.project.logger.lifecycle("[dRPC] '${jar.path}': merging $servicePath")
                val lines = collectServiceFileLines(logger = jar.project.logger, classpathUrls = urls, servicePath = servicePath)
                jar.project.logger.lifecycle("[dRPC] '${jar.path}': merged ${lines.size} unique provider lines for $servicePath")
                lines.take(50).forEach { jar.project.logger.info("[dRPC] '${jar.path}':   $it") }
                if (lines.size > 50) jar.project.logger.info("[dRPC] '${jar.path}':   ... (${lines.size - 50} more)")

                if (lines.isEmpty()) continue

                val targetFile = outDir.resolve(servicePath)
                targetFile.parentFile.mkdirs()
                targetFile.writeText(lines.joinToString("\n") + "\n", Charsets.UTF_8)
                jar.project.logger.info("[dRPC] '${jar.path}': wrote merged descriptor -> ${targetFile.absolutePath}")
            }
        }

        jar.from(outDir)
        jar.project.logger.lifecycle("[dRPC] '${jar.path}': will include merged dRPC descriptors from ${outDir.absolutePath}")
    }

    private fun resolveRuntimeClasspathUrls(project: Project): List<URL> {
        // Collect dependency jars/dirs from the most likely runtime classpath configurations.
        val configNames = listOf(
            "runtimeClasspath",
            "jvmRuntimeClasspath",
        )

        val cpFiles = configNames
            .asSequence()
            .mapNotNull { project.configurations.findByName(it) }
            .firstOrNull()
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

    private fun collectServiceFileLines(logger: Logger, classpathUrls: List<URL>, servicePath: String): List<String> {
        val seen = LinkedHashSet<String>()

        for (url in classpathUrls) {
            // Handle both directories and jars
            val file = runCatching { File(url.toURI()) }.getOrNull() ?: continue
            if (file.isDirectory) {
                val candidate = file.resolve(servicePath)
                if (candidate.isFile) {
                    logger.info("[dRPC] found $servicePath in dir: ${candidate.absolutePath}")
                    addLines(text = candidate.readText(Charsets.UTF_8), seen = seen)
                }
            } else if (file.isFile && file.extension == "jar") {
                JarFile(file).use { jar ->
                    val entry = jar.getJarEntry(servicePath) ?: return@use
                    logger.info("[dRPC] found $servicePath in jar: ${file.absolutePath}")
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