package com.demo.smartlauncher.data

import android.content.ComponentName

/**
 * 一个已安装的 launcher 应用条目。
 *
 * 注意：这里不缓存 [android.graphics.drawable.Drawable] 图标，
 * 图标由 UI 层通过 PackageManager 按需加载（本对象可被复用/比较）。
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val launchComponent: ComponentName
)
