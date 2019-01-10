package omega_r.com.omegatypesexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.applyTo
import com.omega_r.libs.omegatypes.picasso.from
import com.omega_r.libs.omegatypes.setBackground
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : BaseActivity() {

    private val exampleTextView by bind<TextView>(R.id.textview)
    private val imageView by bind<ImageView>(R.id.imageview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text = Text.from(R.string.hello_world)
        text.applyTo(exampleTextView) // or exampleTextView.setText(text)
        val image = Image.from("https://avatars1.githubusercontent.com/u/28600571")

        thread {
            val stream = image.getStream(this, Bitmap.CompressFormat.PNG)
            val bitmap = BitmapFactory.decodeStream(stream)
            runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }

        }

    }

}
