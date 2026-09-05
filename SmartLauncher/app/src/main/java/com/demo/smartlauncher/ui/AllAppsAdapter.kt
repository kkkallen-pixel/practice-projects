package com.demo.smartlauncher.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.smartlauncher.data.AppInfo
import com.demo.smartlauncher.databinding.ItemAppDrawerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * “全部已安装应用”网格的适配器。
 * 图标通过 [AppIconLoader] 异步加载，绑定位置变化时丢弃过期结果，避免错位。
 */
class AllAppsAdapter(
    private val items: List<AppInfo>,
    private val scope: CoroutineScope,
    private val iconLoader: AppIconLoader,
    private val onLaunch: (AppInfo) -> Unit
) : RecyclerView.Adapter<AllAppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppDrawerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        holder.binding.label.text = app.label
        holder.binding.root.contentDescription = app.label
        holder.binding.root.isFocusable = true
        holder.binding.root.isClickable = true
        holder.binding.icon.setImageDrawable(null)
        holder.binding.root.setOnClickListener { onLaunch(app) }

        scope.launch {
            val icon = iconLoader.load(app.packageName)
            // 避免回收复用导致图标错位
            if (holder.bindingAdapterPosition == position && icon != null) {
                holder.binding.icon.setImageDrawable(icon)
            }
        }
    }

    class ViewHolder(val binding: ItemAppDrawerBinding) : RecyclerView.ViewHolder(binding.root)
}
