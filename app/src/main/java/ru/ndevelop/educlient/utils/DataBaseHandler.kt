package ru.ndevelop.educlient.utils

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.models.*
import ru.ndevelop.educlient.repositories.PreferencesRepository.setCurrentGlobalUser
import ru.ndevelop.educlient.utils.Utils.toInt


const val DATABASENAME = "EduClient Database"
const val USERS_TABLENAME = "USERS"
const val HOMEWORK_TABLENAME = "HOMEWORK"
const val COL_LESSON = "LESSON"
const val COL_HOMEWORK_ID = "HOMEWORK_ID"
const val COL_TASK = "TASK"
const val COL_IS_DONE = "IS_DONE"
const val COL_DAY = "COL_DAY"
const val COL_MONTH = "COL_MONTH"
const val COL_NAME = "name"
const val COL_LOGIN = "login"
const val COL_TABLE = "table_marks"
const val COL_PASS = "password"
const val VERSION = 2

object DataBaseHandler : SQLiteOpenHelper(
    App.applicationContext(), DATABASENAME, null,
    VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable =
            "CREATE TABLE $USERS_TABLENAME ($COL_NAME TEXT, $COL_LOGIN TEXT, $COL_PASS TEXT, $COL_TABLE TEXT)"
        val createHomeworkTable =
            "CREATE TABLE $HOMEWORK_TABLENAME ($COL_MONTH INTEGER, $COL_DAY INTEGER, $COL_HOMEWORK_ID INTEGER, $COL_LESSON TEXT, $COL_TASK TEXT, $COL_IS_DONE INTEGER)"
        db?.execSQL(createTable)
        db?.execSQL(createHomeworkTable)

    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        when (newVersion) {
            2 -> db?.execSQL("ALTER TABLE $USERS_TABLENAME ADD COLUMN $COL_TABLE TEXT")
        }
    }

    fun addUser(data: Pair<String, AuthData>): Boolean { // true если такого пользователя ещё не было
        var flag = false

        if (!getUserList().contains(data)) {
            val database = this.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(COL_NAME, data.first)
            contentValues.put(COL_LOGIN, data.second.login)
            contentValues.put(COL_PASS, data.second.password)
            database.insert(USERS_TABLENAME, null, contentValues)
            flag = true
        }

        return flag


    }

    fun updateIfExistsElseInsert(data: HomeworkItem, id: String, day: Int, month: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_LESSON, data.lesson)
        contentValues.put(COL_TASK, data.text)
        contentValues.put(COL_DAY, day)
        contentValues.put(COL_MONTH, month)

        if (isHomeworkLessonExists(id)) {
            db.update(HOMEWORK_TABLENAME, contentValues, "$COL_HOMEWORK_ID = ?", arrayOf(id))
        } else {
            contentValues.put(COL_HOMEWORK_ID, id)
            db.insert(HOMEWORK_TABLENAME, null, contentValues)
        }

       /* val rows = db.update(HOMEWORK_TABLENAME, contentValues, "$COL_HOMEWORK_ID = $id", null)
        if (rows == 0 && data.lesson != "") {
            contentValues.put(COL_HOMEWORK_ID, id)
            db.insertOrThrow(HOMEWORK_TABLENAME, null, contentValues)
        }*/
    }


    private fun isHomeworkLessonExists(homeworkId: String): Boolean {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val selectQuery = "SELECT  *  FROM $HOMEWORK_TABLENAME WHERE $COL_HOMEWORK_ID=$homeworkId;"
        cursor = db.rawQuery(selectQuery, null)
        val result = cursor != null && cursor.count > 0
        cursor.close()
        return result
    }

    fun setHomeworkState(id: Int, flag: Boolean) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_IS_DONE, flag.toInt())
        db.update(HOMEWORK_TABLENAME, contentValues, "$COL_HOMEWORK_ID = $id", null)
    }

    fun getHomeworkList(): MutableList<HomeworkDay> {
        val result: MutableList<HomeworkDay> = mutableListOf()
        var homeworkDay = HomeworkDay()
        homeworkDay.clearTasks()
        var homeworkItem: HomeworkItem
        var i = 0
        var pastId = -1
        var pastDate = -1
        var pastMonth = ""
        val db = this.readableDatabase
        try {
            val query = "Select * from $HOMEWORK_TABLENAME"
            val tempResult = db.rawQuery(query, null)
            if (tempResult.moveToFirst()) {
                do {
                    val id = tempResult.getInt(tempResult.getColumnIndexOrThrow(COL_HOMEWORK_ID))
                    homeworkItem = HomeworkItem()
                    if (pastId == -1) pastId = id / 10
                    if (id / 10 != pastId) {
                        homeworkDay.date = pastDate
                        homeworkDay.month = pastMonth
                        result.add(homeworkDay)
                        homeworkDay = HomeworkDay()
                        homeworkDay.clearTasks()
                    }
                    homeworkItem.lesson =
                        tempResult.getString(tempResult.getColumnIndexOrThrow(COL_LESSON))
                    homeworkItem.text =
                        tempResult.getString(tempResult.getColumnIndexOrThrow(COL_TASK))
                    if (homeworkItem.lesson != "") homeworkDay.isDayEmpty = false
                    homeworkItem.isDone =
                        tempResult.getInt(tempResult.getColumnIndexOrThrow(COL_IS_DONE)) == 1
                    if (homeworkItem.lesson != "") homeworkDay.tasks.add(homeworkItem)
                    i++
                    pastId = id / 10
                    pastDate =
                        tempResult.getInt(tempResult.getColumnIndexOrThrow(COL_DAY))
                    pastMonth = Utils.humanizeMonthNumber(
                        tempResult.getInt(
                            tempResult.getColumnIndexOrThrow(
                                COL_MONTH
                            )
                        )
                    )
                } while (tempResult.moveToNext())

                homeworkDay.date = pastDate
                homeworkDay.month = pastMonth
                result.add(homeworkDay)
            }
            tempResult.close()

        } catch (e: SQLiteException) {

        }
        return result

    }

    fun isHomeworkTableEmpty(): Boolean {
        val db = this.readableDatabase
        var empty = true
        val cur: Cursor = db.rawQuery("SELECT COUNT(*) FROM $HOMEWORK_TABLENAME", null)
        if (cur.moveToFirst()) {
            empty = (cur.getInt(0) == 0)
        }
        cur.close()

        return empty
    }


    fun getUserList(): ArrayList<Pair<String, AuthData>> {
        val result: ArrayList<Pair<String, AuthData>> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $USERS_TABLENAME"
        val tempResult = db.rawQuery(query, null)
        if (tempResult.moveToFirst()) {
            do {
                val name = tempResult.getString(tempResult.getColumnIndexOrThrow(COL_NAME))
                val tmpLogin: String? =
                    tempResult.getString(tempResult.getColumnIndexOrThrow(COL_LOGIN))

                val tmpPassword: String? = tempResult.getString(
                    tempResult.getColumnIndexOrThrow(
                        COL_PASS
                    )
                )
                if (tmpLogin == null) {
                    clearUsers()
                    setCurrentGlobalUser(AuthData("", ""))
                } else if (tmpPassword == null) {
                    deleteUserData(tmpLogin)
                    setCurrentGlobalUser(AuthData("", ""))
                } else {
                    val loginData = AuthData(
                        tmpLogin, tmpPassword
                    )
                    result.add(name to loginData)
                }

            } while (tempResult.moveToNext())
        }
        tempResult.close()
        return result
    }

    fun deleteUserData(login: String) {
        val database = this.writableDatabase
        if (login.isNotEmpty())
            database.delete(USERS_TABLENAME, "$COL_LOGIN = ?", arrayOf(login))

    }

    fun clearHomeworks() {
        val database = this.writableDatabase
        database.execSQL("delete from $HOMEWORK_TABLENAME")
    }

    fun clearUsers() {
        val database = this.writableDatabase
        database.execSQL("delete from $USERS_TABLENAME")
    }

    fun optimizeDatabase(day: Int, month: Int) {
        val db = this.writableDatabase
        val sql =
            "DELETE FROM $HOMEWORK_TABLENAME WHERE $COL_MONTH < $month OR ($COL_DAY < $day AND $COL_MONTH <= $month) "
        db.execSQL(sql)
    }

    fun getTable(userLogin: String): TableItemsArrayHandler {
        val result: ArrayList<TableItem> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $USERS_TABLENAME WHERE $COL_LOGIN=?"
        val tempResult = db.rawQuery(query, arrayOf(userLogin))
        if (tempResult.moveToFirst()) {
            do {
                val rawTable =
                    tempResult.getString(tempResult.getColumnIndexOrThrow(COL_TABLE)) ?: ""
                val rawLessons = rawTable.split('%')
                var tempTableItem = TableItem()
                if (rawTable != "")
                    rawLessons.forEach {

                        val tempLesson = it.split('~')
                        tempTableItem.lesson = tempLesson[0]
                        tempTableItem.marks =
                            if (tempLesson[1] != "") ArrayList(tempLesson[1].split(", ")) else ArrayList()
                        tempTableItem.finalMark = tempLesson[2]
                        result.add(tempTableItem)
                        tempTableItem = TableItem()


                    }
            } while (tempResult.moveToNext())
        }
        tempResult.close()
        return TableItemsArrayHandler(userLogin, result)
    }

    fun isTableEmpty(userLogin: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $USERS_TABLENAME WHERE $COL_LOGIN=?"
        val tempResult = db.rawQuery(query, arrayOf(userLogin))
        if (tempResult.moveToFirst()) {
            val rawTable = tempResult.getString(tempResult.getColumnIndexOrThrow(COL_TABLE)) ?: ""
            return rawTable.isEmpty()
        }
        tempResult.close()
        return true
    }

    fun saveTable(userLogin: String, table: ArrayList<TableItem>) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_LOGIN, userLogin)

        var tableString = ""
        table.forEachIndexed { _, lesson ->
            tableString += "${lesson.lesson}~${lesson.marks.joinToString()}~${lesson.finalMark}%"

        }
        tableString = tableString.dropLast(1)
        contentValues.put(COL_TABLE, tableString)
        val rows =
            db.update(USERS_TABLENAME, contentValues, "$COL_LOGIN = ?", arrayOf(userLogin))
        if (rows == 0) {
            db.insert(USERS_TABLENAME, null, contentValues)
        }

    }


}