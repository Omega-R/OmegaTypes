package com.omega_r.libs.omegatypes.tools

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-02.
 */
class ImageAsyncExecutor(imageView: ImageView, private val extractor: suspend (Context) -> Bitmap?) : AsyncTask<Void, Void, Bitmap?>() {

    companion object {

        private val imageAsyncExecutors = WeakHashMap<ImageView, ImageAsyncExecutor>()

        fun executeImageAsync(imageView: ImageView, extractor: suspend (Context) -> Bitmap?): ImageAsyncExecutor {
            return ImageAsyncExecutor(imageView, extractor)
                    .apply {
                        execute()
                    }
        }

    }

    private val context: WeakReference<Context> = WeakReference(imageView.context)
    private val imageView: WeakReference<ImageView> = WeakReference(imageView)

    init {
        imageAsyncExecutors[imageView]?.cancel(false)
        imageAsyncExecutors[imageView] = this
    }

    override fun doInBackground(vararg params: Void): Bitmap? {
        val context = context.get() ?: return null
        return runBlocking {
            extractor(context)
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        val imageView = imageView.get() ?: return
        this.imageView.clear()
        if (result != null) {
            imageView.setImageBitmap(result)
        }
        imageAsyncExecutors.remove(imageView)
    }


}