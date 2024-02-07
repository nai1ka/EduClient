import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.material.color.MaterialColors
import ru.ndevelop.educlient.R


class AutoBackgroundButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {
    init {
        if (!isInEditMode) {
            val currentColor = MaterialColors.getColor(context, R.attr.colorAccent, Color.BLACK)
            val c = Color.parseColor(currentColor.toString(16))
            if (c.red >= 150 || c.green >= 150 || c.blue >= 150) {
               // this.setTextColor(Color.BLACK)

            } else {

               // this.setTextColor(Color.WHITE)

            }
        }
    }
}