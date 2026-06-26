package com.scripthub.app.utils

import android.content.Context

object EditorPreference {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_EDITOR = "selected_editor"

    const val MONACO = "monaco"
    const val SPCK   = "spck"

    fun getEditor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EDITOR, MONACO) ?: MONACO

    fun setEditor(context: Context, editor: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_EDITOR, editor).apply()
}
