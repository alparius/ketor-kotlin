package alparius.kotlin.ketor.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast

// return true if there's a server connection
fun isOnline(ctx: Context): Boolean {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    if (networkInfo != null && networkInfo.isConnected) {
        return true
    }
    return false
}

// logging made simpler
fun Any.logd(message: Any? = "no message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}

// toasting made simpler
fun makeToast(ctx: Context, message: String): Toast {
    return Toast.makeText(ctx, message, Toast.LENGTH_LONG)
}