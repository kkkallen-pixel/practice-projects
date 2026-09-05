package com.demo.smartlauncher.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * 主页底部的系统功能方块。
 *
 * @param labelRes  方块显示名称
 * @param iconRes   方块图标（矢量资源）
 * @param action    点击/OK 后要执行的动作
 */
data class QuickTile(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val action: Action
) {
    enum class Action {
        /** 进入“全部已安装应用列表”。 */
        OPEN_ALL_APPS,

        /** 打开系统设置。 */
        OPEN_SETTINGS,

        /** 设备专属功能（keystone / Miracast / 信号源），demo 中为占位入口。 */
        DEVICE_FEATURE
    }
}
