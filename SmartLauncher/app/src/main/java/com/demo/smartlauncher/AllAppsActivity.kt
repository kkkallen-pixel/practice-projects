package com.demo.smartlauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.demo.smartlauncher.data.AppInfo
import com.demo.smartlauncher.databinding.ActivityAllAppsBinding
import com.demo.smartlauncher.repo.LauncherRepository
import com.demo.smartlauncher.ui.AllAppsAdapter
import com.demo.smartlauncher.ui.AppIconLoader
import kotlinx.coroutines.launch

/**
 * 全部已安装应用列表（App Drawer）。
 * 网格展示所有 launcher 应用，方向键聚焦，OK 启动，BACK 返回主页。
 */
class AllAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllAppsBinding
    private val repository by lazy { LauncherRepository(packageManager) }
    private val iconLoader by lazy { AppIconLoader(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.title.text = getString(R.string.all_apps_title)
        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = repository.loadAllApps(this@AllAppsActivity)
            binding.count.text = getString(R.string.all_apps_count, apps.size)
            binding.grid.layoutManager = GridLayoutManager(this@AllAppsActivity, GRID_COLUMNS)
            binding.grid.adapter = AllAppsAdapter(
                items = apps,
                scope = lifecycleScope,
                iconLoader = iconLoader,
                onLaunch = ::launchApp
            )
        }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setComponent(app.launchComponent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // 演示环境无需额外处理
        }
    }

    companion object {
        /** 网格列数，横屏下的“全部应用”视图。 */
        private const val GRID_COLUMNS = 4
    }
}
