package com.docta.drpc.core.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Rpc(
    val serviceBaseHttpUrl: String
)
