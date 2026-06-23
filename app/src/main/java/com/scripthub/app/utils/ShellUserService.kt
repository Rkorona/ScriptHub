package com.scripthub.app.utils

import com.scripthub.app.IShellService

class ShellUserService : IShellService.Stub() {

    override fun exec(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            "$exitCode\n$stdout"
        } catch (e: Exception) {
            "-1\n${e.message}"
        }
    }
}
