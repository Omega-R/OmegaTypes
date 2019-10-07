package omega_r.com.omegatypesexample


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.omega_r.libs.omegatypes.Color
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.TextStyle
import com.omega_r.libs.omegatypes.file.File
import com.omega_r.libs.omegatypes.file.from
import com.omega_r.libs.omegatypes.image.*
import com.omega_r.libs.omegatypes.join
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


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


        val image = intent.getSerializableExtra("test") as? Image ?: run {

            val image = Image.from("https://dejagerart.com/wp-content/uploads/2018/09/Test-Logo-Circle-black-transparent.png")

            ImageProcessors.current.launch {
                val stream = image.getStream(this@MainActivity, Bitmap.CompressFormat.PNG)
                val bitmap = BitmapFactory.decodeStream(stream)

                val bitmapImage = Image.from(BitmapDrawable(this@MainActivity.resources, bitmap))

                withContext(Dispatchers.Main) {
                    imageView.setImage(bitmapImage)


                    intent.putExtra("test", bitmapImage)
                    finish()
                    startActivity(intent)
//                imageView.setImageBitmap(bitmap)
                }
            }
            image
        }

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
