package com.demo.smartlauncher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.demo.smartlauncher.data.AppInfo
import com.demo.smartlauncher.databinding.ActivityLauncherBinding
import com.demo.smartlauncher.databinding.ItemAppBinding
import com.demo.smartlauncher.databinding.ItemTileBinding
import com.demo.smartlauncher.repo.LauncherRepository
import com.demo.smartlauncher.ui.DemoApp
import com.demo.smartlauncher.ui.QuickTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 主页 Launcher：
 *
 *  - 顶部：时间/日期（右上角）+ 演示模式切换按钮（左上角）。
 *  - 中间：一排应用，带圆角 + 倒影；按 OK 启动。
 *  - 底部：5 个系统功能方块（keystone/Miracast/signalsource/myapps/settings）。
 *  - 输入：以 D-pad/遥控器焦点导航为主，触摸为辅。
 *
 * 两种模式：
 *  - 演示模式：固定展示 [demoApps]（Netflix/YouTube/Google Play/Chrome），
 *    不依赖设备是否安装，便于还原效果图外观。
 *  - 真实模式：通过 [LauncherRepository] 读取已安装的 launcher 应用。
 */
class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val repository by lazy { LauncherRepository(packageManager) }
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 主页一排展示的应用数量。 */
    private val homeAppCount = 4

    /** 底部 5 个系统功能方块（顺序与效果图一致）。 */
    private val tiles = listOf(
        QuickTile(R.string.tile_keystone, R.drawable.ic_keystone, QuickTile.Action.DEVICE_FEATURE),
        QuickTile(R.string.tile_miracast, R.drawable.ic_miracast, QuickTile.Action.DEVICE_FEATURE),
        QuickTile(R.string.tile_signalsource, R.drawable.ic_signalsource, QuickTile.Action.DEVICE_FEATURE),
        QuickTile(R.string.tile_myapps, R.drawable.ic_myapps, QuickTile.Action.OPEN_ALL_APPS),
        QuickTile(R.string.tile_settings, R.drawable.ic_settings, QuickTile.Action.OPEN_SETTINGS)
    )

    /** 演示模式固定展示的品牌应用。 */
    private val demoApps = listOf(
        DemoApp("Netflix", "com.netflix.mediaclient", R.drawable.ic_netflix),
        DemoApp("YouTube", "com.google.android.youtube", R.drawable.ic_youtube),
        DemoApp("Google Play", "com.android.vending", R.drawable.ic_googleplay),
        DemoApp("Chrome", "com.android.chrome", R.drawable.ic_chrome)
    )

    private val appViews = mutableListOf<View>()
    private val tileViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDemoToggle()
        bindClock()
        bindTiles()
        loadHomeApps()
    }

    private fun isDemoMode(): Boolean = prefs.getBoolean(KEY_DEMO_MODE, false)

    private fun initDemoToggle() {
        binding.demoToggle.text = getString(if (isDemoMode()) R.string.demo_on else R.string.demo_off)
        binding.demoToggle.setOnClickListener { toggleDemoMode() }
    }

    private fun toggleDemoMode() {
        val next = !isDemoMode()
        prefs.edit().putBoolean(KEY_DEMO_MODE, next).apply()
        binding.demoToggle.text = getString(if (next) R.string.demo_on else R.string.demo_off)
        loadHomeApps()
    }

    /** 顶部时间/日期。 */
    private fun bindClock() {
        val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH)
        binding.date.text = formatter.format(Date())
    }

    /** 绑定底部 5 个方块。 */
    private fun bindTiles() {
        binding.tileRow.removeAllViews()
        tileViews.clear()
        tiles.forEach { tile ->
            val item = ItemTileBinding.inflate(layoutInflater, binding.tileRow, false)
            item.icon.setImageResource(tile.iconRes)
            item.label.text = getString(tile.labelRes)
            item.root.isFocusable = true
            item.root.isClickable = true
            item.root.contentDescription = getString(tile.labelRes)
            item.root.setOnClickListener { handleTile(tile) }
            binding.tileRow.addView(item.root)
            tileViews.add(item.root)
        }
    }

    private fun loadHomeApps() {
        if (isDemoMode()) bindDemoApps() else bindRealApps()
    }

    /** 演示模式：用占位品牌图标填充一排。 */
    private fun bindDemoApps() {
        binding.appsRow.removeAllViews()
        appViews.clear()
        binding.emptyHint.visibility = View.GONE
        demoApps.forEach { demo ->
            val item = ItemAppBinding.inflate(layoutInflater, binding.appsRow, false)
            item.label.text = demo.label
            item.reflectionIcon.setIcon(ContextCompat.getDrawable(this, demo.iconRes))
            item.root.contentDescription = demo.label
            item.root.isFocusable = true
            item.root.isClickable = true
            item.root.setOnClickListener { launchPackage(demo.packageName, demo.label) }
            binding.appsRow.addView(item.root)
            appViews.add(item.root)
        }
        appViews.firstOrNull()?.requestFocus()
    }

    /** 真实模式：读取已安装应用填充一排。 */
    private fun bindRealApps() {
        lifecycleScope.launch {
            val apps = repository.loadFavoriteApps(this@LauncherActivity, homeAppCount)
            val icons = withContext(Dispatchers.IO) {
                apps.map { loadIcon(it.packageName) }
            }
            binding.appsRow.removeAllViews()
            appViews.clear()
            if (apps.isEmpty()) {
                binding.emptyHint.visibility = View.VISIBLE
                return@launch
            }
            binding.emptyHint.visibility = View.GONE
            apps.forEachIndexed { index, app ->
                val item = ItemAppBinding.inflate(layoutInflater, binding.appsRow, false)
                item.label.text = app.label
                item.reflectionIcon.setIcon(icons[index])
                item.root.contentDescription = app.label
                item.root.isFocusable = true
                item.root.isClickable = true
                item.root.tag = app
                item.root.setOnClickListener { launchApp(app) }
                binding.appsRow.addView(item.root)
                appViews.add(item.root)
            }
            appViews.firstOrNull()?.requestFocus()
        }
    }

    private fun loadIcon(packageName: String): Drawable? = try {
        packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
    }

    /** 启动指定的已安装应用。 */
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setComponent(app.launchComponent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.app_launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /** 演示模式：按包名启动，若未安装则提示。 */
    private fun launchPackage(packageName: String, label: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                showNotInstalled(label)
            }
        } else {
            showNotInstalled(label)
        }
    }

    private fun showNotInstalled(label: String) {
        Toast.makeText(this, getString(R.string.demo_not_installed, label), Toast.LENGTH_SHORT).show()
    }

    /** 处理底部方块的 OK/点击。 */
    private fun handleTile(tile: QuickTile) {
        when (tile.action) {
            QuickTile.Action.OPEN_ALL_APPS -> openAllApps()
            QuickTile.Action.OPEN_SETTINGS -> openSettings()
            QuickTile.Action.DEVICE_FEATURE ->
                Toast.makeText(
                    this,
                    getString(R.string.feature_not_ready, getString(tile.labelRes)),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun openAllApps() {
        startActivity(Intent(this, AllAppsActivity::class.java))
    }

    private fun openSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(this, R.string.app_launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 焦点导航（D-pad）：
     *  上/下：在 [demoToggle] → 应用一排 → 方块一排 之间切换；
     *  菜单键：进入全部已安装应用列表（对应“任意焦点可跳转到全部已安装 App List”）。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> if (moveFocusDown()) return true
                KeyEvent.KEYCODE_DPAD_UP -> if (moveFocusUp()) return true
                KeyEvent.KEYCODE_MENU -> {
                    openAllApps()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun moveFocusDown(): Boolean {
        val current = currentFocus ?: return false
        val index = appViews.indexOf(current)
        if (index >= 0) {
            (tileViews.getOrNull(index) ?: tileViews.lastOrNull())?.requestFocus()
            return true
        }
        if (current == binding.demoToggle) {
            appViews.firstOrNull()?.requestFocus()
            return true
        }
        return false
    }

    private fun moveFocusUp(): Boolean {
        val current = currentFocus ?: return false
        val index = tileViews.indexOf(current)
        if (index >= 0) {
            (appViews.getOrNull(index) ?: appViews.lastOrNull())?.requestFocus()
            return true
        }
        if (appViews.indexOf(current) >= 0) {
            binding.demoToggle.requestFocus()
            return true
        }
        return false
    }

    companion object {
        private const val PREFS_NAME = "smart_launcher"
        private const val KEY_DEMO_MODE = "demo_mode"
    }
}
