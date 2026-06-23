package com.scripthub.app.utils

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private const val REQUEST_CODE = 3721

    enum class State {
        UNAVAILABLE,
        CONNECTED_NO_PERMISSION,
        READY
    }

    private val _state = MutableStateFlow(State.UNAVAILABLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private val binderReceived = Shizuku.OnBinderReceivedListener { refreshState() }
    private val binderDead = Shizuku.OnBinderDeadListener { _state.value = State.UNAVAILABLE }
    private val permResult = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            _state.value = State.READY
        }
    }

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permResult)
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceived)
        Shizuku.removeBinderDeadListener(binderDead)
        Shizuku.removeRequestPermissionResultListener(permResult)
    }

    fun refreshState() {
        _state.value = when {
            !Shizuku.pingBinder() -> State.UNAVAILABLE
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> State.READY
            else -> State.CONNECTED_NO_PERMISSION
        }
    }

    fun requestPermission() {
        if (Shizuku.pingBinder()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    fun exec(command: String): ExecResult {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            ExecResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            ExecResult(-1, "", e.message ?: "执行失败")
        }
    }

    fun listDirectory(path: String): List<String> {
        val result = exec("ls -1a \"$path\" 2>/dev/null")
        return if (result.exitCode == 0) {
            result.stdout.lines().filter { it.isNotBlank() && it != "." && it != ".." }
        } else emptyList()
    }

    fun deleteFile(path: String): Boolean =
        exec("rm -rf \"$path\"").exitCode == 0

    fun copyFile(src: String, dst: String): Boolean =
        exec("cp -r \"$src\" \"$dst\"").exitCode == 0

    fun moveFile(src: String, dst: String): Boolean =
        exec("mv \"$src\" \"$dst\"").exitCode == 0

    fun fileExists(path: String): Boolean =
        exec("test -e \"$path\"").exitCode == 0

    data class ExecResult(val exitCode: Int, val stdout: String, val stderr: String)
}
