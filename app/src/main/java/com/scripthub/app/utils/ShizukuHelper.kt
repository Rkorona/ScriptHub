package com.scripthub.app.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.scripthub.app.IShellService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private const val REQUEST_CODE = 3721
    private const val APP_PACKAGE  = "com.scripthub.app"

    enum class State { UNAVAILABLE, CONNECTED_NO_PERMISSION, READY }

    private val _state = MutableStateFlow(State.UNAVAILABLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private var shellService: IShellService? = null

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(APP_PACKAGE, ShellUserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("shell_service")
        .version(1)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            shellService = IShellService.Stub.asInterface(binder)
            _state.value = State.READY
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            _state.value = if (Shizuku.pingBinder()) State.CONNECTED_NO_PERMISSION else State.UNAVAILABLE
        }
    }

    private val binderReceived = Shizuku.OnBinderReceivedListener { refreshState() }

    private val binderDead = Shizuku.OnBinderDeadListener {
        shellService = null
        _state.value = State.UNAVAILABLE
    }

    private val permResult = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) bindService()
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
        try {
            if (shellService != null) {
                Shizuku.unbindUserService(userServiceArgs, serviceConnection, false)
            }
        } catch (_: Exception) {}
    }

    fun refreshState() {
        when {
            !Shizuku.pingBinder() -> _state.value = State.UNAVAILABLE
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> bindService()
            else -> _state.value = State.CONNECTED_NO_PERMISSION
        }
    }

    fun requestPermission() {
        if (Shizuku.pingBinder()) Shizuku.requestPermission(REQUEST_CODE)
    }

    private fun bindService() {
        try {
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (_: Exception) {
            _state.value = State.CONNECTED_NO_PERMISSION
        }
    }

    fun exec(command: String): ExecResult {
        val service = shellService ?: return ExecResult(-1, "", "Shizuku 服务未连接")
        return try {
            val raw = service.exec(command)
            val newlineIdx = raw.indexOf('\n')
            val exitCode = if (newlineIdx > 0) raw.substring(0, newlineIdx).trim().toIntOrNull() ?: -1 else -1
            val stdout   = if (newlineIdx > 0) raw.substring(newlineIdx + 1) else ""
            ExecResult(exitCode, stdout, "")
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

    fun deleteFile(path: String): Boolean  = exec("rm -rf \"$path\"").exitCode == 0
    fun copyFile(src: String, dst: String): Boolean = exec("cp -r \"$src\" \"$dst\"").exitCode == 0
    fun moveFile(src: String, dst: String): Boolean = exec("mv \"$src\" \"$dst\"").exitCode == 0
    fun fileExists(path: String): Boolean  = exec("test -e \"$path\"").exitCode == 0

    data class ExecResult(val exitCode: Int, val stdout: String, val stderr: String)
}
