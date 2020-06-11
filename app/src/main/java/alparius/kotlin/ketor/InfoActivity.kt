package alparius.kotlin.ketor

import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val dark:Boolean = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("apptheme", false)
        if (dark) {
            setTheme(R.style.AppTheme2)
        }
        this.setTitle(PreferenceManager.getDefaultSharedPreferences(this).getString("appname", "ketor"))

        super.onCreate(savedInstanceState)

        // inside your activity (if you did not enable transitions in your theme)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            // set an exit transition
            exitTransition = Fade()
        }

        setContentView(R.layout.activity_info)

        infoText.text = getString(R.string.info_string)
    }
}
