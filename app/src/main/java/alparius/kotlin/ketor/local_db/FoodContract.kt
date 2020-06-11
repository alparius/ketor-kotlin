package alparius.kotlin.ketor.local_db

import android.provider.BaseColumns

object FoodContract {
    const val DB_NAME = "foodsDB"
    const val DB_VERSION = 1

    // table contents are grouped together in an anonymous object
    object FoodEntry : BaseColumns {
        const val DB_TABLE = "foods"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_LEVEL = "level"
        const val COL_KCAL = "kcal"
        const val COL_FATS = "fats"
        const val COL_CARBS = "carbs"
        const val COL_PROTEIN = "protein"
    }
}