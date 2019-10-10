package com.omega_r.libs.omegatypes.tools

import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewTreeObserver.OnPreDrawListener
import com.omega_r.libs.omegatypes.image.ImageProcessors
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-02.
 */
class ImageSizeExtractor<V : View>(target: V, callback: suspend (view: V) -> Unit) : OnPreDrawListener, OnAttachStateChangeListener {

    companion object {

        private val imageSizeExtractors = WeakHashMap<View, ImageSizeExtractor<*>>()


    }

    private val target: WeakReference<V> = WeakReference(target)
    private var callback: (suspend (view: V) -> Unit)? = callback

    init {
        imageSizeExtractors[target]?.cancel()
        imageSizeExtractors[target] = this

        target.addOnAttachStateChangeListener(this)
        if (target.windowToken != null) {
            onViewAttachedToWindow(target)
        }
    }

    override fun onPreDraw(): Boolean {
        val target = target.get() ?: return true
        val vto = target.viewTreeObserver
        if (!vto.isAlive) {
            return true
        }
        val width = target.width
        val height = target.height

        if (width <= 0 || height <= 0) {
            return true
        }

        target.removeOnAttachStateChangeListener(this)
        vto.removeOnPreDrawListener(this)
        this.target.clear()

        ImageProcessors.current.launch {
            callback?.invoke(target)
        }

        return true
    }

    override fun onViewAttachedToWindow(view: View) {
        view.viewTreeObserver.addOnPreDrawListener(this)
    }

    override fun onViewDetachedFromWindow(view: View) {
        view.viewTreeObserver.removeOnPreDrawListener(this)
    }

    fun cancel() {
        callback = null

        val target = target.get() ?: return
        this.target.clear()

        target.removeOnAttachStateChangeListener(this)

        val vto = target.viewTreeObserver
        if (vto.isAlive) {
            vto.removeOnPreDrawListener(this)
        }

    }


}