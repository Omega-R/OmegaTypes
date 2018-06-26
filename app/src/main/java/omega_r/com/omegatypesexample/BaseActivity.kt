package omega_r.com.omegatypesexample

import android.app.Activity
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View

open class BaseActivity : AppCompatActivity() {

    fun <T : View> Activity.bind(@IdRes res: Int) : Lazy<T> = lazy { findViewById<T>(res) }

}