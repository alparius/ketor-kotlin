package alparius.kotlin.ketor.local_db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.jetbrains.anko.toast

private const val SQL_CREATE_ENTRIES =
    """CREATE TABLE IF NOT EXISTS ${FoodContract.FoodEntry.DB_TABLE} (
            ${FoodContract.FoodEntry.COL_ID} INTEGER PRIMARY KEY,
            ${FoodContract.FoodEntry.COL_NAME} TEXT,
            ${FoodContract.FoodEntry.COL_LEVEL} TEXT,
            ${FoodContract.FoodEntry.COL_KCAL} INTEGER,
            ${FoodContract.FoodEntry.COL_FATS} INTEGER,
            ${FoodContract.FoodEntry.COL_CARBS} INTEGER,
            ${FoodContract.FoodEntry.COL_PROTEIN} INTEGER,
            IS_TEMP INTEGER DEFAULT 0)
    """
// the temp-mark is deliberately not part of the contract so it sticks out a bit :)

private const val SQL_DELETE_ENTRIES =
    "DROP TABLE IF EXISTS ${FoodContract.FoodEntry.DB_TABLE}"

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        FoodContract.DB_NAME,
        null,
        FoodContract.DB_VERSION
    ) {

    private var context: Context? = context

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(SQL_CREATE_ENTRIES)
        context?.toast(" database is created")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL(SQL_DELETE_ENTRIES)
    }
}