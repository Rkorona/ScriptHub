// app_template/app/src/main/java/com/example/myapplication/viewmodel/ConfigViewModel.kt
package com.scripthub.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scripthub.app.data.AppDatabase
import com.scripthub.app.data.DependencyEntity
import com.scripthub.app.data.EnvVarEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val envDao = db.envVarDao()
    private val depDao = db.dependencyDao()

    // 观察数据流（无任何初始假数据置入）
    val envVarsList: StateFlow<List<EnvVarEntity>> = envDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val depsList: StateFlow<List<DependencyEntity>> = depDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- 环境变量操作 ---
    fun addOrUpdateEnv(env: EnvVarEntity) {
        viewModelScope.launch { envDao.insert(env) }
    }
    
    fun toggleEnvState(env: EnvVarEntity, isEnabled: Boolean) {
        viewModelScope.launch { envDao.update(env.copy(isEnabled = isEnabled)) }
    }

    fun deleteEnv(env: EnvVarEntity) {
        viewModelScope.launch { envDao.delete(env) }
    }

    // --- 依赖操作 ---
    fun addOrUpdateDependency(dep: DependencyEntity) {
        viewModelScope.launch { depDao.insert(dep) }
    }
    
    fun deleteDependency(dep: DependencyEntity) {
        viewModelScope.launch { depDao.delete(dep) }
    }
}