package com.scripthub.app.utils

import android.content.Context
import android.os.Environment

object WorkdirPreference {
    private const val PREF_NAME = "workdir_prefs"
    private const val KEY_WORKDIR = "workdir"

    fun defaultWorkdir(): String =
        Environment.getExternalStorageDirectory().absolutePath + "/QLPanel"

    fun getWorkdir(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WORKDIR, defaultWorkdir()) ?: defaultWorkdir()

    fun setWorkdir(context: Context, path: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_WORKDIR, path.trimEnd('/')).apply()
    }

    fun getScriptsDir(context: Context): String = "${getWorkdir(context)}/scripts"
}
