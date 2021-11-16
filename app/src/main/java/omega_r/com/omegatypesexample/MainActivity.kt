package omega_r.com.omegatypesexample


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.omega_r.libs.omegatypes.*
import com.omega_r.libs.omegatypes.file.File
import com.omega_r.libs.omegatypes.file.from
import com.omega_r.libs.omegatypes.image.*
import com.omega_r.libs.omegatypes.image.Image.Companion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread


class MainActivity : BaseActivity() {

    private val exampleTextView by bind<TextView>(R.id.textview)
    private val imageView by bind<ImageView>(R.id.imageview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val text = TextBuilder()
                .append(addText)
                .append(Text.empty())
                .append(Text.from("test "))
                .append(Text.from(Image.from(R.drawable.ic_test)))
                .append(Text.from(
                        R.string.hello_world,
                        Text.from(R.string.app_name),
                        textStyle = TextStyle.font(ResourcesCompat.getFont(this, R.font.noto_sans_regular)!!)
                ))
                .append(Text.from(
                        "   SEMI BOLD",
                        textStyle = TextStyle.font(ResourcesCompat.getFont(this, R.font.noto_sans_semi_bold)!!)
                ))
                .toText()

        text.applyTo(exampleTextView) // or exampleTextView.setText(text)

        val list = listOf(Text.from("1", TextStyle.color(Color.fromAttribute(R.attr.colorAccent))), Text.from("2", TextStyle.color(Color.fromAttribute(R.attr.colorAccent))), Text.from("3"))

        title = list.join(",", postfix = ".").getCharSequence(this)


        val image = Image.from("https://dejagerart.com/wp-content/uploads/2018/09/Test-Logo-Circle-black-transparent.png")


//        val image = intent.getSerializableExtra("test") as? Image ?: run {
//
//            val image = Image.from("https://dejagerart.com/wp-content/uploads/2018/09/Test-Logo-Circle-black-transparent.png")
//
//            ImageProcessors.current.launch {
//                val stream = image.getStream(this@MainActivity, Bitmap.CompressFormat.PNG)
//                val bitmap = BitmapFactory.decodeStream(stream)
//
//                val bitmapImage = Image.from(BitmapDrawable(this@MainActivity.resources, bitmap))
//
//                withContext(Dispatchers.Main) {
//                    imageView.setImage(bitmapImage)
//
//
////                    intent.putExtra("test", bitmapImage)
////                    finish()
////                    startActivity(intent)
////                imageView.setImageBitmap(bitmap)
//                }
//            }
//            image
//        }

        imageView.setImage(image, processor = GlideImagesProcessor(ImageProcessors.current), onImageApplied = {
            Log.d("TAG","onImageApplied")
        })



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
