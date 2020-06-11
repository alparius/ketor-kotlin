package alparius.kotlin.ketor.model

import com.google.gson.annotations.SerializedName

data class Food(
    @field:SerializedName("id") var id: Int = 0,
    @field:SerializedName("name") var name: String = "",
    @field:SerializedName("level") var level: String = "",
    @field:SerializedName("kcal") var kcal: Int = 0,
    @field:SerializedName("fats") var fats: Int = 0,
    @field:SerializedName("carbs") var carbs: Int = 0,
    @field:SerializedName("protein") var protein: Int = 0,
    var isTemp: Int? = 0
) {
    fun nutrients(): String {
        return "$kcal kcal - fats:${fats}g, carbs:${carbs}g, protein:${protein}g"
    }
}