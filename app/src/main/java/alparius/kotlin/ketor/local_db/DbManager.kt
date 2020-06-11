package alparius.kotlin.ketor.local_db

import alparius.kotlin.ketor.model.Food
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DbManager(context: Context) {
    private val dbHelper: DatabaseHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase by lazy { dbHelper.writableDatabase }

    // fully reset the local db
    fun reset(foodList: ArrayList<Food>) {
        for (food in this.queryAll()) {
            this.delete(food.id)
        }
        for (food in foodList) {
            this.insert(food)
        }
    }

    // store an entity locally
    // no id when offline
    // temp-mark when offline
    fun insert(food: Food, offline: Boolean = false): Long {
        val values = ContentValues()
        if (!offline) {
            values.put(FoodContract.FoodEntry.COL_ID, food.id)
        } else {
            values.put("IS_TEMP", 1)
        }
        values.put(FoodContract.FoodEntry.COL_NAME, food.name)
        values.put(FoodContract.FoodEntry.COL_LEVEL, food.level)
        values.put(FoodContract.FoodEntry.COL_KCAL, food.kcal)
        values.put(FoodContract.FoodEntry.COL_FATS, food.fats)
        values.put(FoodContract.FoodEntry.COL_CARBS, food.carbs)
        values.put(FoodContract.FoodEntry.COL_PROTEIN, food.protein)
        return db.insert(FoodContract.FoodEntry.DB_TABLE, "", values)
    }

    // get a single element
    fun queryOne(id: Number): Food {
        val cursor: Cursor = db.rawQuery(
            "select * from ${FoodContract.FoodEntry.DB_TABLE} WHERE ${FoodContract.FoodEntry.COL_ID} = $id",
            null
        )
        cursor.moveToFirst()
        val food = Food(
            cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_ID)),
            cursor.getString(cursor.getColumnIndex(FoodContract.FoodEntry.COL_NAME)),
            cursor.getString(cursor.getColumnIndex(FoodContract.FoodEntry.COL_LEVEL)),
            cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_KCAL)),
            cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_FATS)),
            cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_CARBS)),
            cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_PROTEIN)),
            cursor.getInt(cursor.getColumnIndex("IS_TEMP"))
        )
        cursor.close()
        return food
    }

    // get all local elements
    fun queryAll(): ArrayList<Food> {
        val cursor: Cursor = db.rawQuery("select * from ${FoodContract.FoodEntry.DB_TABLE}", null)

        val foodList = ArrayList<Food>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_ID))
                val name = cursor.getString(cursor.getColumnIndex(FoodContract.FoodEntry.COL_NAME))
                val level =
                    cursor.getString(cursor.getColumnIndex(FoodContract.FoodEntry.COL_LEVEL))
                val kcal = cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_KCAL))
                val fats = cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_FATS))
                val carbs = cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_CARBS))
                val protein =
                    cursor.getInt(cursor.getColumnIndex(FoodContract.FoodEntry.COL_PROTEIN))
                val is_temp = cursor.getInt(cursor.getColumnIndex("IS_TEMP"))
                foodList.add(Food(id, name, level, kcal, fats, carbs, protein, is_temp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return foodList
    }

    // update an element locally
    fun update(food: Food): Int {
        val values = ContentValues()
        values.put(FoodContract.FoodEntry.COL_NAME, food.name)
        values.put(FoodContract.FoodEntry.COL_LEVEL, food.level)
        values.put(FoodContract.FoodEntry.COL_KCAL, food.kcal)
        values.put(FoodContract.FoodEntry.COL_FATS, food.fats)
        values.put(FoodContract.FoodEntry.COL_CARBS, food.carbs)
        values.put(FoodContract.FoodEntry.COL_PROTEIN, food.protein)

        val selection = "id=?"
        val selectionArgs = arrayOf(food.id.toString())
        return db.update(FoodContract.FoodEntry.DB_TABLE, values, selection, selectionArgs)
    }

    // delete an element locally
    fun delete(id: Number): Int {
        val selection = "id=?"
        val selectionArgs = arrayOf(id.toString())
        return db.delete(FoodContract.FoodEntry.DB_TABLE, selection, selectionArgs)
    }

    fun close() {
        db.close()
    }
}