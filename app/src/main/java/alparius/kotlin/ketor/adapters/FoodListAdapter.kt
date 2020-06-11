package alparius.kotlin.ketor.adapters

import alparius.kotlin.ketor.FoodActivity
import alparius.kotlin.ketor.R
import alparius.kotlin.ketor.local_db.DbManager
import alparius.kotlin.ketor.model.Food
import alparius.kotlin.ketor.networking.ApiClient
import alparius.kotlin.ketor.utils.isOnline
import alparius.kotlin.ketor.utils.logd
import alparius.kotlin.ketor.utils.makeToast
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.food.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException

// mapping of keto-levels to display colors
val textColor = mapOf(
    "High" to "#FF8BC34A",
    "Medium" to "#FFFFC107",
    "Low" to "#FFF44336",
    null to "#00000000"
)

class FoodListAdapter(private val context: Context) :
    RecyclerView.Adapter<FoodListAdapter.ElementViewAdapter>() {

    private val client by lazy { ApiClient.create() }
    private var elementsList: ArrayList<Food> = ArrayList()
    private val dbManager = DbManager(context)

    init {
        refreshElements()
    }


    class ElementViewAdapter(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food, parent, false)
        return ElementViewAdapter(view)
    }

    override fun onBindViewHolder(holder: ElementViewAdapter, position: Int) {
        // the looks of the list items
        val thisFood = elementsList[position]
        holder.view.tvTitle.text = thisFood.name
        holder.view.tvTitle.setTextColor(Color.parseColor(textColor[thisFood.level]))
        holder.view.tvContent.text = thisFood.nutrients()

        // list item click handler to open update activity
        holder.view.setOnClickListener {
            val intent = Intent(context, FoodActivity::class.java)
            intent.putExtra("MainActId", thisFood.id)
            (context as Activity).startActivityForResult(intent, 0)
        }
    }

    override fun getItemCount() = elementsList.size

    // called when receives an item via a websocket broadcast
    fun webSocketUpdate(element: Food, action: String) {
        if (action == "INSERT") {
            // new elem, insert
            dbManager.insert(element)
            elementsList.add(element)
            notifyDataSetChanged()

        } else if (action == "UPDATE") {
            // if we don't have the updated elem (yet), it will be synced by other means, nothing to update
            val index = elementsList.indexOfFirst { it.id == element.id }
            if (index > -1) {
                dbManager.update(element)
                elementsList[index] = element
                notifyDataSetChanged()
            }

        } else { // action == "DELETE"
            // if we don't have the deleted elem (yet), it will be synced by other means, nothing to delete
            val index = elementsList.indexOfFirst { it.id == element.id }
            if (index > -1) {
                dbManager.delete(element.id)
                elementsList.removeAt(index)
                notifyDataSetChanged()
            }
        }
        logd("performed the broadcasted $action on: $element")
    }

    // try to push entities only stored locally
    private fun tryPushOnline() {
        val swapList: ArrayList<Food> = ArrayList()
        // filter for all temp-marked local elements
        swapList.addAll(dbManager.queryAll().filter { it.isTemp == 1 }.toList())
        for (food in swapList) {
            // remove them locally since it will come back via a broadcast
            dbManager.delete(food.id)
            elementsList.remove(food)
            notifyDataSetChanged()
            // if the connection breaks again, the elem gets added with the temp-mark, so all fine
            addElement(food)
        }
        if (swapList.size !=0) {
            logd("tried to push ${swapList.size} elems to the server")
        }
    }

    @SuppressLint("CheckResult")
    fun refreshElements() {
        tryPushOnline()
        if (isOnline(context)) {
            client.getAllFoodsAPI()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        elementsList.clear()
                        elementsList.addAll(result)
                        notifyDataSetChanged()
                        dbManager.reset(elementsList)

                    },
                    { throwable ->
                        if (throwable is HttpException) {
                            val body: ResponseBody = throwable.response()?.errorBody()!!
                            makeToast(context, "error: ${JSONObject(body.string())}").show()
                        }
                    }
                )
        } else {
            makeToast(context, "Showing local list").show()
            // if offline, show only locally available elements
            elementsList.clear()
            elementsList.addAll(dbManager.queryAll())
        }
    }

    @SuppressLint("CheckResult")
    fun addElement(element: Food) {
        if (isOnline(context)) {
            client.addFoodAPI(element)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        // no need to do anything, changes will be broadcasted
                        logd("element added -> $element")
                    },
                    { throwable ->
                        if (throwable is HttpException) {
                            val body: ResponseBody = throwable.response()?.errorBody()!!
                            makeToast(context, "error: ${JSONObject(body.string())}").show()
                        }
                    }
                )
        } else {
            makeToast(context, "Not online, will sync later!").show()
            // if offline, insert it locally with an temporary local ID and a temp-mark
            val id = dbManager.insert(element, offline = true)
            // set the id so the item is clickable in the list
            element.id = id.toInt()
            elementsList.add(element)
            notifyDataSetChanged()
        }
    }


    @SuppressLint("CheckResult")
    fun updateElement(element: Food) {
        if (isOnline(context)) {
            client.updateFoodAPI(element.id, element)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        // no need to do anything, changes will be broadcasted
                        makeToast(context, "Food updated!").show()
                        logd("element updated -> $element")
                    },
                    { throwable ->
                        if (throwable is HttpException) {
                            val body: ResponseBody = throwable.response()?.errorBody()!!
                            makeToast(context, "error: ${JSONObject(body.string())}").show()
                        }
                    }
                )
        } else {
            makeToast(context, "Not online!").show()
        }
    }

    @SuppressLint("CheckResult")
    fun deleteElement(element: Food) {
        if (isOnline(context)) {
            client.deleteFoodAPI(element.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        // no need to do anything, changes will be broadcasted
                        makeToast(context, "Food deleted!").show()
                        logd("element deleted -> $element")
                    },
                    { throwable ->
                        makeToast(context, "delete error: ${throwable.message}").show()
                    }
                )
        } else {
            makeToast(context, "Not online!").show()
        }
    }
}