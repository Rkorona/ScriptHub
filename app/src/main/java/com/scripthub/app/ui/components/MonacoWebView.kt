package com.scripthub.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView

/**
 * Monaco 通过隐藏 textarea 接收输入；标准 WebView 默认不声明自己是文本编辑器，
 * 导致 Android 系统软键盘无法弹出。重写 onCheckIsTextEditor 修复此问题。
 */
@SuppressLint("ViewConstructor")
class MonacoWebView(context: Context) : WebView(context) {
    override fun onCheckIsTextEditor(): Boolean = true
}
