#include <jni.h>
#include <pty.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <signal.h>
#include <errno.h>
#include <android/log.h>

#define TAG "PtyHelper"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

/*
 * forkExecPty: 分配 PTY 并 fork-exec 目标进程。
 *
 * 返回 int[2] = {masterFd, pid}。
 * masterFd 是 PTY master 侧文件描述符；子进程的 stdin/stdout/stderr 均连接到 PTY slave。
 * 这使得子进程内的程序可以打开 /dev/tty（它就是 PTY slave），解决
 * "Failed to open /dev/tty" 错误。
 */
JNIEXPORT jintArray JNICALL
Java_com_scripthub_app_utils_PtyProcess_forkExecPty(
    JNIEnv *env, jclass clazz,
    jobjectArray cmdArray,
    jobjectArray envArray,
    jstring jWorkDir
) {
    /* ── 转换命令行参数 ── */
    int cmdLen = (*env)->GetArrayLength(env, cmdArray);
    char **cmd = malloc((cmdLen + 1) * sizeof(char *));
    if (!cmd) return NULL;
    for (int i = 0; i < cmdLen; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, cmdArray, i);
        const char *utf = (*env)->GetStringUTFChars(env, s, NULL);
        cmd[i] = strdup(utf);
        (*env)->ReleaseStringUTFChars(env, s, utf);
    }
    cmd[cmdLen] = NULL;

    /* ── 转换环境变量 ── */
    int envLen = (*env)->GetArrayLength(env, envArray);
    char **envp = malloc((envLen + 1) * sizeof(char *));
    if (!envp) { free(cmd); return NULL; }
    for (int i = 0; i < envLen; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, envArray, i);
        const char *utf = (*env)->GetStringUTFChars(env, s, NULL);
        envp[i] = strdup(utf);
        (*env)->ReleaseStringUTFChars(env, s, utf);
    }
    envp[envLen] = NULL;

    const char *workDir = (*env)->GetStringUTFChars(env, jWorkDir, NULL);

    /* ── 终端参数：无回显，规范模式，输出 ONLCR ── */
    struct termios t;
    memset(&t, 0, sizeof(t));
    t.c_iflag = ICRNL | IXON;
    t.c_oflag = OPOST | ONLCR;
    t.c_cflag = B38400 | CS8 | CREAD;
    /* ICANON + ISIG + IEXTEN，但 **不** 加 ECHO / ECHOE / ECHOK */
    t.c_lflag = ICANON | ISIG | IEXTEN;
    t.c_cc[VMIN]   = 1;
    t.c_cc[VTIME]  = 0;
    t.c_cc[VINTR]  = 3;    /* Ctrl+C  → SIGINT  */
    t.c_cc[VERASE] = 127;  /* Backspace         */
    t.c_cc[VEOF]   = 4;    /* Ctrl+D            */
    t.c_cc[VKILL]  = 21;   /* Ctrl+U            */
    t.c_cc[VSUSP]  = 26;   /* Ctrl+Z  → SIGTSTP */
    t.c_cc[VSTART] = 17;   /* Ctrl+Q            */
    t.c_cc[VSTOP]  = 19;   /* Ctrl+S            */

    struct winsize ws = { .ws_row = 40, .ws_col = 120 };

    int masterFd = -1;
    pid_t pid = forkpty(&masterFd, NULL, &t, &ws);

    if (pid < 0) {
        LOGE("forkpty failed: %s (errno=%d)", strerror(errno), errno);
        (*env)->ReleaseStringUTFChars(env, jWorkDir, workDir);
        return NULL;
    }

    if (pid == 0) {
        /* 子进程：切换工作目录后 execve */
        if (workDir && workDir[0]) chdir(workDir);
        execve(cmd[0], cmd, envp);
        LOGE("execve(%s) failed: %s", cmd[0], strerror(errno));
        _exit(127);
    }

    /* 父进程 */
    (*env)->ReleaseStringUTFChars(env, jWorkDir, workDir);
    LOGD("forkpty ok: masterFd=%d pid=%d", masterFd, pid);

    jintArray result = (*env)->NewIntArray(env, 2);
    jint res[2] = { masterFd, (jint)pid };
    (*env)->SetIntArrayRegion(env, result, 0, 2, res);
    return result;
}

/* 调整 PTY 窗口大小（用于未来 UI 尺寸同步） */
JNIEXPORT void JNICALL
Java_com_scripthub_app_utils_PtyProcess_resizePty(
    JNIEnv *env, jclass clazz, jint fd, jint rows, jint cols
) {
    struct winsize ws = { .ws_row = (unsigned short)rows,
                          .ws_col = (unsigned short)cols };
    ioctl((int)fd, TIOCSWINSZ, &ws);
}

/* 关闭 master fd */
JNIEXPORT void JNICALL
Java_com_scripthub_app_utils_PtyProcess_closeFd(
    JNIEnv *env, jclass clazz, jint fd
) {
    close((int)fd);
}

/* 向进程发送信号（kill / SIGINT 等） */
JNIEXPORT jint JNICALL
Java_com_scripthub_app_utils_PtyProcess_killPid(
    JNIEnv *env, jclass clazz, jint pid, jint sig
) {
    return (jint)kill((pid_t)pid, (int)sig);
}
