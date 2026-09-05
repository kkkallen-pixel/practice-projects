package com.demo.smartlauncher.repo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.demo.smartlauncher.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 负责通过 [PackageManager] 查询可启动的 launcher 应用。
 *
 * 所有耗时的查询都在 IO 线程执行，避免阻塞主线程；
 * 主页与“全部应用”列表均基于该仓库的数据。
 */
class LauncherRepository(private val packageManager: PackageManager) {

    companion object {
        /**
         * 主页优先展示的“代表性应用”包名（模拟效果图中的应用）。
         * 未安装的会按顺序跳过，不足时用其他已安装应用补齐。
         */
        private val FAVORITE_PACKAGES = listOf(
            "com.android.chrome",
            "com.google.android.youtube",
            "com.android.vending",
            "com.netflix.mediaclient",
            "com.google.android.gm"
        )
    }

    /** 读取全部可启动的 launcher 应用（排除自身），按名称排序。 */
    suspend fun loadAllApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        queryLauncherApps(context).sortedBy { it.label.lowercase() }
    }

    /**
     * 读取主页展示的代表性应用。
     * 优先取已安装的 [FAVORITE_PACKAGES]，再以其他已安装应用补齐到 [count] 个。
     */
    suspend fun loadFavoriteApps(context: Context, count: Int): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val all = queryLauncherApps(context)
            val byPackage = all.associateBy { it.packageName }
            val favorites = FAVORITE_PACKAGES.mapNotNull { byPackage[it] }
            val rest = all.filterNot { it.packageName in FAVORITE_PACKAGES }
            (favorites + rest).take(count)
        }

    private fun queryLauncherApps(context: Context): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val selfPackage = context.packageName
        return packageManager
            .queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                // 排除 Launcher 自身，避免重复出现
                if (activityInfo.packageName == selfPackage) return@mapNotNull null
                AppInfo(
                    label = activityInfo.loadLabel(packageManager).toString(),
                    packageName = activityInfo.packageName,
                    launchComponent = ComponentName(
                        activityInfo.packageName,
                        activityInfo.name
                    )
                )
            }
    }
}
