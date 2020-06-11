package alparius.kotlin.ketor

import alparius.kotlin.ketor.adapters.FoodListAdapter
import alparius.kotlin.ketor.local_db.DbManager
import alparius.kotlin.ketor.model.Food
import android.app.AlertDialog
import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.view.Window
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_food.*
import org.jetbrains.anko.toast

class FoodActivity : AppCompatActivity() {

    private var id = 0



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

        setContentView(R.layout.activity_food)

        // decide if UPDATE or CREATE from the transmitted bundle
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            id = bundle.getInt("MainActId", 0)
        }

        if (id == 0) {
            // if CREATE mode, add the "Cancel" button and check the default radio value
            val levelId: Int = resources.getIdentifier("High", "id", packageName)
            (editLevel.findViewById(levelId) as RadioButton).isChecked = true

            btDel.text = getString(R.string.cancel)
            btDel.setOnClickListener {
                finish()
            }

        } else {
            // if UPDATE mode, load data into fields
            val thisFood = DbManager(this).queryOne(id) // rest of fields from local
            editName.setText(thisFood.name)
            val levelId: Int = resources.getIdentifier(thisFood.level, "id", packageName)
            (editLevel.findViewById(levelId) as RadioButton).isChecked = true
            editKcal.setText(thisFood.kcal.toString())
            editFats.setText(thisFood.fats.toString())
            editCarbs.setText(thisFood.carbs.toString())
            editProtein.setText(thisFood.protein.toString())

            // if UPDATE mode, add "Delete" listener and dialog
            btDel.setOnClickListener {
                val elementAdapter = FoodListAdapter(this)
                val builder = AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Confirm delete?")
                    .setPositiveButton("Delete") { _, _ ->
                        elementAdapter.deleteElement(thisFood)
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                builder.create().show()
            }
        }

        // in both modes, listener for the "Save" button
        btAdd.setOnClickListener {
            val elementAdapter = FoodListAdapter(this)
            // all fields must be populated to perform a save
            if (editName.text.toString() != "" && editKcal.text.toString() != "" && editFats.text.toString() != "" && editCarbs.text.toString() != "" && editProtein.text.toString() != "") {
                val name = editName.text.toString()
                val level =
                    (editLevel.findViewById(editLevel.checkedRadioButtonId) as RadioButton).text.toString()
                val kcal = editKcal.text.toString().toInt()
                val fats = editFats.text.toString().toInt()
                val carbs = editCarbs.text.toString().toInt()
                val protein = editProtein.text.toString().toInt()

                val food = Food(id, name, level, kcal, fats, carbs, protein)

                // CREATE or UPDATE the food
                if (id == 0) {
                    elementAdapter.addElement(food)
                    finish()
                    toast("Food added!")
                } else {
                    elementAdapter.updateElement(food)
                    finish()
                }

            } else {
                // show a dialog if there are missing fields
                val builder = AlertDialog.Builder(this)
                    .setMessage("Please complete the missing fields!")
                    .setPositiveButton("OK", null)
                builder.create().show()
            }
        }
    }
}