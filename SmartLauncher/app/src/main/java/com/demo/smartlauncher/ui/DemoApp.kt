package com.demo.smartlauncher.ui

import androidx.annotation.DrawableRes

/**
 * 演示模式下固定展示的应用条目。
 * 与真实 [com.demo.smartlauncher.data.AppInfo] 不同，它自带占位图标资源，
 * 不依赖设备上是否真正安装对应应用。
 */
data class DemoApp(
    val label: String,
    val packageName: String,
    @DrawableRes val iconRes: Int
)
