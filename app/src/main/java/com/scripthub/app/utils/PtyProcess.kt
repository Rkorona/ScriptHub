package com.scripthub.app.utils

import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * JNI 封装：通过 forkpty() 分配真正的伪终端（PTY）并启动子进程。
 *
 * 与 ProcessBuilder（管道模式）不同，PTY 模式下：
 *  - 子进程有真实的控制终端，/dev/tty 可正常打开
 *  - fzf、vim、nano、less、htop 等 TUI 程序不再报 "Failed to open /dev/tty"
 *  - 通过 PTY master fd 同时读写 stdin/stdout/stderr
 */
object PtyProcess {
    private const val TAG = "PtyProcess"

    init {
        System.loadLibrary("pty-helper")
    }

    /** 分配 PTY 并 fork-exec，返回 [masterFd, pid]，失败返回 null */
    @JvmStatic external fun forkExecPty(
        cmd: Array<String>,
        env: Array<String>,
        workDir: String
    ): IntArray?

    /** 调整 PTY 窗口大小 */
    @JvmStatic external fun resizePty(fd: Int, rows: Int, cols: Int)

    /** 关闭 master fd */
    @JvmStatic external fun closeFd(fd: Int)

    /** 向 pid 发送信号（2=SIGINT, 9=SIGKILL, 15=SIGTERM） */
    @JvmStatic external fun killPid(pid: Int, sig: Int): Int

    /* ─────────────────────────────────────────────────────── */

    data class Session(
        val pid: Int,
        val masterFd: Int,
        val inputStream: InputStream,
        val outputStream: OutputStream
    ) {
        fun sendSignal(sig: Int) = killPid(pid, sig)

        fun destroy() {
            // SIGKILL 先终止进程
            try { sendSignal(9) } catch (_: Exception) {}
            // inputStream 和 outputStream 共享同一个 masterFd 对应的 FileDescriptor，
            // 关闭 inputStream 时底层 fd 即被关闭，不再重复 close outputStream / closeFd，
            // 避免 double-close 导致关闭到其他 fd 的风险。
            try { inputStream.close() } catch (_: Exception) {}
        }
    }

    /**
     * 启动一个带 PTY 的子进程。
     *
     * @param cmd     完整命令行（包含可执行文件路径和参数）
     * @param envMap  环境变量映射
     * @param workDir 子进程工作目录
     */
    fun start(
        cmd: List<String>,
        envMap: Map<String, String>,
        workDir: String = "/"
    ): Session? {
        val envArray = envMap.map { (k, v) -> "$k=$v" }.toTypedArray()
        val result = forkExecPty(cmd.toTypedArray(), envArray, workDir)
        if (result == null || result.size < 2) {
            Log.e(TAG, "forkExecPty 返回 null，PTY 分配失败")
            return null
        }
        val masterFd = result[0]
        val pid = result[1]
        Log.d(TAG, "PTY 会话启动成功: pid=$pid masterFd=$masterFd")

        // 通过反射将原生 int fd 封装进 Java FileDescriptor
        val javaFd = FileDescriptor()
        return try {
            val field = FileDescriptor::class.java.getDeclaredField("descriptor")
            field.isAccessible = true
            field.setInt(javaFd, masterFd)
            Session(
                pid          = pid,
                masterFd     = masterFd,
                inputStream  = FileInputStream(javaFd),
                outputStream = FileOutputStream(javaFd)
            )
        } catch (e: Exception) {
            Log.e(TAG, "封装 FileDescriptor 失败: ${e.message}")
            closeFd(masterFd)
            null
        }
    }
}
