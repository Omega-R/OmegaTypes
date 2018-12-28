package omega_r.com.omegatypesexample

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.applyTo
import com.omega_r.libs.omegatypes.picasso.from

class MainActivity : BaseActivity() {

    private val exampleTextView by bind<TextView>(R.id.textview)
    private val imageView by bind<ImageView>(R.id.imageview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text = Text.from(R.string.hello_world)
        text.applyTo(exampleTextView) // or exampleTextView.setText(text)
        val image = Image.from("https://avatars1.githubusercontent.com/u/28600571")
        image.applyTo(imageView) // or imageView.setImage(image)
    }

}
