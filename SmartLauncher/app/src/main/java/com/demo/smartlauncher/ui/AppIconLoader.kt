package com.demo.smartlauncher.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 应用图标加载器：在 IO 线程读取图标并用 [LruCache] 做内存缓存。
 * 避免在 RecyclerView 绑定阶段触发主线程磁盘/包查询抖动。
 */
class AppIconLoader(context: Context) {

    private val packageManager = context.packageManager
    private val cache = object : LruCache<String, Drawable>(64) {}

    suspend fun load(packageName: String): Drawable? = withContext(Dispatchers.IO) {
        cache.get(packageName)?.let { return@withContext it }
        val drawable = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
        drawable?.let { cache.put(packageName, it) }
        drawable
    }
}
