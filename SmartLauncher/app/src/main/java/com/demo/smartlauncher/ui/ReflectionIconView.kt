package com.demo.smartlauncher.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * 带“倒影”的图标视图：上方绘制正常图标，下方绘制翻转且向下渐隐的镜像。
 *
 * 对应需求说明里的“注意：1. 倒影”。
 * 由于图标可能来自任意 App（颜色、尺寸不一），这里统一转成位图后绘制，
 * 并在 setupBitmap 阶段预生成带渐变 alpha 的反射位图，保证绘制开销最小。
 */
class ReflectionIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var iconBitmap: Bitmap? = null
    private var reflectionBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val reflectionPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    /** 反射部分占整个视图高度的比例。 */
    private val reflectionRatio = 0.34f

    /** 图标圆角半径（以图标短边的比例计）。 */
    private val cornerRadiusFraction = 0.22f

    /** 设置要显示的图标（可为 null，表示清除）。 */
    fun setIcon(drawable: Drawable?) {
        if (drawable == null) {
            iconBitmap = null
            reflectionBitmap = null
            invalidate()
            return
        }
        val size = 256
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val scale = size.toFloat() / maxOf(w, h)
        val bitmap = Bitmap.createBitmap(
            (w * scale).toInt().coerceAtLeast(1),
            (h * scale).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(canvas)
        // 先做圆角，再生成倒影，保证图标与镜像都带圆角
        val rounded = roundBitmap(bitmap, cornerRadiusFraction)
        iconBitmap = rounded
        reflectionBitmap = createReflection(rounded)
        invalidate()
    }

    /** 通过 [BitmapShader] + 圆角矩形裁剪，给图标加圆角。 */
    private fun roundBitmap(source: Bitmap, radiusFraction: Float): Bitmap {
        val radius = min(source.width, source.height) * radiusFraction
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val canvas = Canvas(output)
        canvas.drawRoundRect(
            RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
            radius,
            radius,
            paint
        )
        return output
    }

    /**
     * 生成翻转且自上而下渐隐的反射位图（顶部保留约 55% 不透明度，到底部为 0）。
     */
    private fun createReflection(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        // 1) 上下翻转绘制
        canvas.save()
        canvas.scale(1f, -1f)
        canvas.drawBitmap(source, 0f, -source.height.toFloat(), null)
        canvas.restore()
        // 2) 用线性渐变 alpha 作为蒙版，实现渐隐
        val maskPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            shader = LinearGradient(
                0f,
                0f,
                0f,
                source.height.toFloat(),
                Color.argb(0x8C, 0xFF, 0xFF, 0xFF),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(
            0f,
            0f,
            source.width.toFloat(),
            source.height.toFloat(),
            maskPaint
        )
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val icon = iconBitmap ?: return
        val refl = reflectionBitmap ?: return

        // 图标：按视图宽度缩放，占上方 1-reflectionRatio 的区域
        val aspect = icon.width.toFloat() / icon.height.toFloat()
        var iconHeight = height * (1f - reflectionRatio)
        var iconWidth = iconHeight * aspect
        // 若超宽，则以宽度为上限并等比降低高度，避免水平溢出
        if (iconWidth > width) {
            iconWidth = width.toFloat()
            iconHeight = iconWidth / aspect
        }
        val left = (width - iconWidth) / 2f
        val top = 0f
        canvas.drawBitmap(icon, null, android.graphics.RectF(left, top, left + iconWidth, top + iconHeight), paint)

        // 反射：位于图标下方，高度按比例，宽度与图标一致
        val reflHeight = height * reflectionRatio
        val reflLeft = (width - iconWidth) / 2f
        val reflTop = top + iconHeight
        canvas.drawBitmap(
            refl,
            null,
            android.graphics.RectF(reflLeft, reflTop, reflLeft + iconWidth, reflTop + reflHeight),
            reflectionPaint
        )
    }
}
