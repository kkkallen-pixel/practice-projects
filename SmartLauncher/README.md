# SmartLauncher

一个基于 Kotlin + Jetpack 的原生 Android Launcher 示例，可设为默认桌面。支持遥控器 / 方向键焦点导航，带图标倒影，并能进入全部已安装应用列表。

## 功能

- 可作为默认桌面（`Main + HOME + DEFAULT`）。
- 演示模式：左上角开关，开启后固定展示 Netflix / YouTube / Google Play / Chrome 四个应用，不依赖设备是否安装。
- 真实模式：通过 `PackageManager` 读取已安装的 launcher 应用。
- 主页应用带圆角与倒影效果。
- 底部 5 个功能方块：Keystone、Miracast、SignalSource、MyApps、Settings。
  - MyApps 进入全部应用列表。
  - Settings 打开系统设置。
  - 其余为设备功能占位入口。
- 全部应用列表：网格展示所有 launcher 应用，方向键选择，OK 启动，BACK 返回。
- 输入以 D-pad / 遥控器为主，触摸为辅。

## 技术栈

| 项目 | 版本 |
| --- | --- |
| Kotlin | 1.9.20 |
| Android Gradle Plugin | 8.1.1 |
| Gradle | 8.2 |
| compileSdk / targetSdk | 34 |
| minSdk | 21 |
| 构建 JDK | JDK 17 |
| UI | AndroidX + ViewBinding |
| 异步 | Kotlin Coroutines |

## 目录结构

```
app/src/main/java/com/demo/smartlauncher/
├── LauncherActivity.kt       # 主页 Launcher
├── AllAppsActivity.kt        # 全部应用列表
├── data/AppInfo.kt           # 应用条目模型
├── repo/LauncherRepository.kt# PackageManager 查询
└── ui/
    ├── AppIconLoader.kt      # 图标 IO 加载 + 缓存
    ├── AllAppsAdapter.kt     # 全部应用网格适配器
    ├── DemoApp.kt            # 演示模式品牌应用
    ├── ReflectionIconView.kt # 圆角 + 倒影图标控件
    └── QuickTile.kt          # 底部方块模型
```

## 构建与运行

用 Android Studio 打开项目即可，首次会按 `gradle/wrapper/gradle-wrapper.properties` 下载 Gradle 8.2。

命令行构建需要 JDK 17 和 Android SDK：

```bash
gradle wrapper --gradle-version 8.2
gradlew :app:assembleDebug
```

> 项目未附带 `gradlew` 与 wrapper jar，建议优先用 Android Studio。

## 操作

- 上 / 下：在切换按钮、应用排、方块排之间移动焦点。
- 左 / 右：在行内移动焦点。
- OK：启动应用，或执行方块功能。
- 菜单键：进入全部应用。
- 全部应用列表中：OK 启动，BACK 返回。

## 说明

- 壁纸是占位图，替换 `app/src/main/res/drawable/wallpaper.png` 即可换图。
- Keystone / Miracast / SignalSource 为设备功能占位入口，需按设备实际能力接入。
- 演示模式品牌图标取自 `simple-icons`，为单色图形 + 品牌色。
