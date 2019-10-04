package omega_r.com.omegatypesexample


import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.omega_r.libs.omegatypes.Color
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.TextStyle
import com.omega_r.libs.omegatypes.image.*
import com.omega_r.libs.omegatypes.join


class MainActivity : BaseActivity() {

    companion object {

        init {
            GlideImagesProcessor.setAsCurrentImagesProcessor()
        }

    }

    private val exampleTextView by bind<TextView>(R.id.textview)
    private val imageView by bind<ImageView>(R.id.imageview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val text = Text.from("test ") +
                Text.from(
                        R.string.hello_world,
                        Text.from(R.string.app_name),
                        textStyle = TextStyle.font(ResourcesCompat.getFont(this, R.font.noto_sans_regular)!!)
                ) +
                Text.from(
                        "   SEMI BOLD",
                        textStyle = TextStyle.font(ResourcesCompat.getFont(this, R.font.noto_sans_semi_bold)!!)
                )
        text.applyTo(exampleTextView) // or exampleTextView.setText(text)

        val list = listOf(Text.from("1", TextStyle.color(Color.fromAttribute(R.attr.colorAccent))), Text.from("2", TextStyle.color(Color.fromAttribute(R.attr.colorAccent))), Text.from("3"))

        title = list.join(",", postfix = ".").getCharSequence(this)

        val image = Image.from("https://dejagerart.com/wp-content/uploads/2018/09/Test-Logo-Circle-black-transparent.png")


        imageView.setImage(image)

//        thread {
//            val stream = image.getStream(this, Bitmap.CompressFormat.PNG)
//            val bitmap = BitmapFactory.decodeStream(stream)
//            runOnUiThread {
//                imageView.setImageBitmap(bitmap)
//            }
//
//        }

    }

}
