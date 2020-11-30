import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient

fun getApi(url: String, builder: okhttp3.Request.Builder.() -> Unit = {}): String? {
    val request = okhttp3.Request.Builder()
        .url(url)
        .apply(builder)
        .get()
        .build()
    val response = OkHttpClient().newCall(request).execute()
    return if (response.code == 200) response.body!!.string() else null
}

inline fun <reified T> getJsonApi(url: String, noinline builder: okhttp3.Request.Builder.() -> Unit = {}) = getApi(url, builder).fromJson<T>()

/**
 * converts [this] to a Json string
 */
fun Any?.toJson(): String = Gson().toJson(this)

/**
 * converts [this] to a Json string but its formatted nicely
 */
fun Any?.toPrettyJson(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)

/**
 * Takes [this] and coverts it to an object
 */
inline fun <reified T> String?.fromJson(): T? = try {
    Gson().fromJson(this, object : TypeToken<T>() {}.type)
} catch (e: Exception) {
    null
}
