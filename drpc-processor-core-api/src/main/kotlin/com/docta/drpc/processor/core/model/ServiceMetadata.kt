package com.docta.drpc.processor.core.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class ServiceMetadata(
    val packageName: String,
    val baseName: String,
    val serviceName: String,
    val serviceQualifiedName: String = "$packageName.$serviceName",
    val controllerName: String,
    val controllerQualifiedName: String = "$packageName.$controllerName",
    val binderName: String,
    val binderQualifiedName: String = "$packageName.$binderName"
) {
    companion object {

        fun fromService(service: KSClassDeclaration): ServiceMetadata {
            val packageName = service.packageName.asString()
            val serviceName = service.simpleName.asString()
            val serviceQualifiedName = service.qualifiedName?.asString()
                ?: error("Service class ($serviceName) must have a qualified name")
            val baseName = serviceName.removeSuffix("Service")
            val controllerName = "${baseName}RestController"
            val binderName = "${serviceName}Binder"

            return ServiceMetadata(
                packageName = packageName,
                baseName = baseName,
                serviceName = serviceName,
                serviceQualifiedName = serviceQualifiedName,
                controllerName = controllerName,
                binderName = binderName
            )
        }

    }
}
