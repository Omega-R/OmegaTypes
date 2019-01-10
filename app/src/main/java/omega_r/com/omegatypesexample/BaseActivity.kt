package omega_r.com.omegatypesexample

import android.app.Activity
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import android.view.View

open class BaseActivity : AppCompatActivity() {

    fun <T : View> Activity.bind(@IdRes res: Int) : Lazy<T> = lazy { findViewById<T>(res) }

}