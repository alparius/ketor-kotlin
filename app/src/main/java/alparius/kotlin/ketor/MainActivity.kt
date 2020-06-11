package alparius.kotlin.ketor

import alparius.kotlin.ketor.adapters.FoodListAdapter
import alparius.kotlin.ketor.model.Food
import alparius.kotlin.ketor.utils.logd
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val NORMAL_CLOSURE_STATUS = 1000

// ANIMATIONS: slide-in-out and overwitten infopage fade
// rxjava: asnyc, event based, observables, pipeline
// materiladesign: autoinflate, fab, buttons, cardview, colorscheme etc
// settings: appname, theme, copy my mail

class MainActivity : AppCompatActivity() {
    lateinit var adapter: FoodListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val dark:Boolean = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("apptheme", false)
        if (dark) {
            setTheme(R.style.AppTheme2)
        }
        this.setTitle(PreferenceManager.getDefaultSharedPreferences(this).getString("appname", "ketor"))

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        adapter = FoodListAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        lvFoods.layoutManager = layoutManager
        lvFoods.adapter = adapter

        // FAB to add new foods
        fab.setOnClickListener {
            val intent = Intent(this, FoodActivity::class.java)
            startActivityForResult(intent, 0, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }

        // websocket test
        val clientWebSocket = OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build()
        val request = Request.Builder().url("ws://10.0.2.2:2019").build()
        val wsListener = EchoWebSocketListener()
        clientWebSocket.newWebSocket(request, wsListener)

        // triggering the shutdown of the dispatcher's executor so this process can exit cleanly
        clientWebSocket.dispatcher().executorService().shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.refresh -> {
                    adapter.refreshElements()
                }
                R.id.appInfo -> {
                    val intent = Intent(this, InfoActivity::class.java)
                    startActivityForResult(intent, 0, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivityForResult(intent, 0)
                }
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

    // fire this when returning from a "startActivityForResult"
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        adapter = FoodListAdapter(this)
        lvFoods.adapter = adapter
        if (lvFoods.adapter != null) {
            lvFoods.adapter!!.notifyDataSetChanged()
        }
    }

    inner class EchoWebSocketListener : WebSocketListener() {
        private lateinit var webSocket: WebSocket

        override fun onOpen(webSocket: WebSocket, response: Response) {
            this.webSocket = webSocket
            webSocket.send("Hello, there!")
            webSocket.send("What's up?")
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            logd("Receiving : ${text!!}")
            GlobalScope.launch(Dispatchers.Main) {
                val bcFood = Food(
                    id = JSONObject(text).getString("id").toInt(),
                    name = JSONObject(text).getString("name"),
                    level = JSONObject(text).getString("level"),
                    kcal = JSONObject(text).getString("kcal").toInt(),
                    fats = JSONObject(text).getString("fats").toInt(),
                    carbs = JSONObject(text).getString("carbs").toInt(),
                    protein = JSONObject(text).getString("protein").toInt()
                )
                // the purpose of the broadcast is in the action field
                val action = JSONObject(text).getString("action")
                // update the local db with the broadcasted changes
                adapter.webSocketUpdate(bcFood, action)
                lvFoods.adapter = adapter
            }
        }

        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            logd("Receiving bytes : ${bytes!!.hex()}")
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
            logd("Closing : $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logd("Error : ${t.message}" + t)
        }

        fun send(message: String) {
            webSocket.send(message)
        }

        fun close() {
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!")
        }
    }


}