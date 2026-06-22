package com.scripthub.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scripthub.app.data.AppDatabase
import com.scripthub.app.data.DependencyEntity
import com.scripthub.app.data.EnvVarEntity
import com.scripthub.app.ui.screens.DepStatus
import com.scripthub.app.ui.screens.DepType
import com.scripthub.app.utils.DistroPreference
import com.scripthub.app.utils.ProotManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val envDao = db.envVarDao()
    private val depDao = db.dependencyDao()

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

    /**
     * 真正执行依赖安装：先写入 Installing 状态，然后在 proot 容器中运行安装命令，
     * 最终将结果（Installed / Failed）更新回数据库。
     */
    fun installDependency(dep: DependencyEntity) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val installing = dep.copy(status = DepStatus.Installing)
            depDao.insert(installing)

            withContext(Dispatchers.IO) {
                try {
                    val distro = DistroPreference.getDistro(context)

                    if (!ProotManager.isDistroInstalled(context, distro)) {
                        depDao.update(installing.copy(status = DepStatus.Failed))
                        return@withContext
                    }

                    val cmd = buildInstallCommand(dep)
                    Log.d("DepInstall", "执行安装命令: $cmd")

                    val process = ProotManager.buildProotProcess(context, distro, cmd)
                        .redirectErrorStream(true)
                        .start()

                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    val exitCode = process.waitFor()

                    Log.d("DepInstall", "安装输出:\n$output\n退出码: $exitCode")

                    val finalStatus = if (exitCode == 0) DepStatus.Installed else DepStatus.Failed
                    depDao.update(installing.copy(status = finalStatus))

                } catch (e: Exception) {
                    Log.e("DepInstall", "安装异常", e)
                    depDao.update(installing.copy(status = DepStatus.Failed))
                }
            }
        }
    }

    private fun buildInstallCommand(dep: DependencyEntity): String {
        val ver = dep.version.trim()
        val hasVersion = ver.isNotEmpty() && ver != "latest"
        return when (dep.type) {
            DepType.NodeJS -> {
                val pkg = if (hasVersion) "${dep.name}@$ver" else dep.name
                "npm install -g $pkg"
            }
            DepType.Python3 -> {
                val pkg = if (hasVersion) "${dep.name}==$ver" else dep.name
                "pip3 install --break-system-packages $pkg"
            }
            DepType.Linux -> {
                "DEBIAN_FRONTEND=noninteractive apt-get install -y ${dep.name}"
            }
        }
    }
}
