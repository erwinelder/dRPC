package com.docta.drpc.server

object DrpcInstallerHolder {

    @Volatile var installer: DrpcInstaller? = null

}