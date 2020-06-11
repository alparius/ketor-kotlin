package alparius.kotlin.ketor.networking

import alparius.kotlin.ketor.model.Food
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiClient {
    @GET("foods")
    fun getAllFoodsAPI(): Observable<List<Food>>

    @POST("food")
    fun addFoodAPI(@Body food: Food): Completable

    @PUT("food/{id}")
    fun updateFoodAPI(@Path("id") id: Int, @Body food: Food): Completable

    @DELETE("food/{id}")
    fun deleteFoodAPI(@Path("id") id: Int): Completable

    companion object {
        fun create(): ApiClient {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://10.0.2.2:2019/api/")
                .build()
            return retrofit.create(ApiClient::class.java)
        }
    }
}