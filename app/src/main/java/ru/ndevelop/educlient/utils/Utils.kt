package ru.ndevelop.educlient.utils

import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import org.xmlpull.v1.XmlPullParserException
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.*
import ru.ndevelop.educlient.repositories.PreferencesRepository
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException


object Utils {
    private val locale = Locale("ru", "RU")
    fun getMonthNumber(month: String) =
        when (month) {
            "Январь" -> 1
            "Февраль" -> 2
            "Март" -> 3
            "Апрель" -> 4
            "Май" -> 5
            "Июнь" -> 6
            "Июль" -> 7
            "Август" -> 8
            "Сентябрь" -> 9
            "Октябрь" -> 10
            "Ноябрь" -> 11
            "Декабрь" -> 12
            else -> 0

        }

    fun getCorrectMonthTitle(month: String) =
        when (month) {
            "Январь" -> "Января"
            "Февраль" -> "Февраля"
            "Март" -> "Марта"
            "Апрель" -> "Апреля"
            "Май" -> "Мая"
            "Июнь" -> "Июня"
            "Июль" -> "Июля"
            "Август" -> "Августа"
            "Сентябрь" -> "Сентября"
            "Октябрь" -> "Октября"
            "Ноябрь" -> "Ноября"
            "Декабрь" -> "Декабря"
            else -> ""

        }

    fun humanizeMonthNumber(month: Int) =
        when (month) {
            1 -> "Январь"
            2 -> "Февраль"
            3 -> "Март"
            4 -> "Апрель"
            5 -> "Май"
            6 -> "Июнь"
            7 -> "Июль"
            8 -> "Август"
            9 -> "Сентябрь"
            10 -> "Октябрь"
            11 -> "Ноябрь"
            12 -> "Декабрь"
            else -> ""
        }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!
            .activeNetworkInfo
        return activeNetworkInfo != null
    }

    fun getCalendar(day: Int, month: Int, year: Int): Calendar {
        val date = Calendar.getInstance()
        date[Calendar.YEAR] = year
        date[Calendar.MONTH] = month
        date[Calendar.DAY_OF_MONTH] = day
        return date
    }

    fun getDaysBetween(cal1: Calendar, cal2: Calendar): Int {
        var numberOfDays = 0
        while (cal1.before(cal2)) {
            if (
                (Calendar.SUNDAY != cal1.get(Calendar.DAY_OF_WEEK))) {
                numberOfDays++
            }
            cal1.add(Calendar.DATE, 1)
        }
        return numberOfDays
    }

    fun Boolean.toInt() = if (this) 1 else 0

    fun getDifferenceBetweenTwoTables(
        oldTable: ArrayList<TableItem>,
        newTable: ArrayList<TableItem>
    ): ArrayList<TableDifferenceFindingResult> {
        val resultArray: ArrayList<TableDifferenceFindingResult> = arrayListOf()
//TODO сложить сумму отдельн окаждой оценки(1,2,3,4,5) и вычесть (если увеличилась сумаа - увеличились оценки)
        var singleTableItem: TableDifferenceFindingResult
        var tempMarksArray: ArrayList<String>
        for (i in newTable.indices) {

            if (oldTable[i].marks.size < newTable[i].marks.size) {
                tempMarksArray = arrayListOf()
                val oldMap: MutableMap<String, Int> =
                    mutableMapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0)
                val newMap: MutableMap<String, Int> =
                    mutableMapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0)
                for (j in oldTable[i].marks.dropLast(1)) {
                    oldMap[j] = (oldMap[j] ?: 0) + 1
                }
                for (j in newTable[i].marks.dropLast(1)) {
                    newMap[j] = (newMap[j] ?: 0) + 1
                }
                newMap.forEach { (key, value) ->
                    if (value - (oldMap[key]
                            ?: 0) > 0
                    ) tempMarksArray.addAll(Array(value - (oldMap[key] ?: 0)) { key })
                }
                singleTableItem =
                    TableDifferenceFindingResult(newTable[i].lesson, tempMarksArray.toTypedArray())

                resultArray.add(singleTableItem)
            }
        }
        return resultArray
    }


    fun testDIff(
    ): ArrayList<TableDifferenceFindingResult> {
        val resultArray: ArrayList<TableDifferenceFindingResult> = arrayListOf()
        val oldItem1 = TableItem()
        oldItem1.apply {
            lesson = "lesson1"
            marks = arrayListOf(
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5.00"
            )
            finalMark = "4"
        }
        val oldItem2 = TableItem()
        oldItem2.apply {
            lesson = "lesson2"
            marks = arrayListOf("5", "5", "5.00")
            finalMark = "4"
        }
        val newItem1 = TableItem()
        newItem1.apply {
            lesson = "lesson1"
            marks = arrayListOf(
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "5",
                "2",
                "5.00"
            )
            finalMark = "4"
        }
        val newItem2 = TableItem()
        newItem2.apply {
            lesson = "lesson2"
            marks = arrayListOf("5", "5", "5", "5.00")
            finalMark = "4"
        }

        var singleTableItem: TableDifferenceFindingResult
        var tempMarksArray: ArrayList<String>
        val oldTable = arrayListOf(oldItem1, oldItem2)
        val newTable = arrayListOf(newItem1, newItem2)
        for (i in newTable.indices) {
            if (oldTable[i].marks.size < newTable[i].marks.size) {
                tempMarksArray = arrayListOf()
                val oldMap: MutableMap<String, Int> =
                    mutableMapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0)
                val newMap: MutableMap<String, Int> =
                    mutableMapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0)
                for (j in oldTable[i].marks.dropLast(1)) {
                    oldMap[j] = (oldMap[j] ?: 0) + 1
                }
                for (j in newTable[i].marks.dropLast(1)) {
                    newMap[j] = (newMap[j] ?: 0) + 1
                }
                newMap.forEach { (key, value) ->
                    if (value - (oldMap[key]
                            ?: 0) > 0
                    ) tempMarksArray.addAll(Array(value - (oldMap[key] ?: 0)) { key })
                }
                singleTableItem =
                    TableDifferenceFindingResult(newTable[i].lesson, tempMarksArray.toTypedArray())

                resultArray.add(singleTableItem)
            }
        }
        return resultArray
    }

    fun getScreenSize(context: Context): Pair<Int, Int> {
        val metrics = DisplayMetrics()
        val windowManager = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return width to height
    }

    fun hideKeyboardForm(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getNextMonth(month: String) =
        when (month) {
            "Январь" -> "Февраль"
            "Февраль" -> "Март"
            "Март" -> "Апрель"
            "Апрель" -> "Май"
            "Май" -> "Июнь"
            "Июнь" -> "Июль"
            "Июль" -> "Август"
            "Август" -> "Сентябрь"
            "Сентябрь" -> "Октябрь"
            "Октябрь" -> "Ноябрь"
            "Ноябрь" -> "Декабрь"
            "Декабрь" -> "Январь"
            else -> ""
        }

    fun getDayNumberFromString(day: String): Int {
        return if (day != "") day.toInt()
        else 1
    }

    fun getShortWeekDay(date: Calendar): String {
        val weekDay = SimpleDateFormat("E", locale).format(date.time)
        return weekDay.uppercase(locale)
    }

    fun getWeekDay(date: Calendar): String {
        val weekDay = SimpleDateFormat("EEEE", locale).format(date.time)
        return weekDay.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun handleException(e: Exception): String {
        if (e is XmlPullParserException) NetworkHelper.clearCookies()
        return when (e) {
            is TimeoutException, is SocketTimeoutException -> "Превышено время ожидания. Повторите попытку"
            is ConnectException, is SSLHandshakeException -> "Ошибка соединения с сайтом. Повторите попытку"
            is UnknownHostException -> "Ошибка сети. Проверьте интернет-соединение"
            is NumberFormatException -> "Внутренняя ошибка\nСкорее всего, что-то с сайтом edu.tatar.ru"
            is XmlPullParserException -> {
                "Ошибка обработки данных. Обычно это происходит, когда сайт edu.tatar.ru недоступен"
            }

            is IpException -> {
                "Ошибка загрузки. Убедитесь, что ваш IP-адрес - российский"
            }

            else -> "Неизвестная ошибка:\n$e"
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }


    fun getSubLists(payload: ArrayList<Day>): Array<List<Day>> {
        val size = payload.size / 3
        val additionSize = payload.size - size * 3
        val result: ArrayList<List<Day>> = arrayListOf()
        var lastIndex = 0
        for (i in 0 until size) {
            result.add(listOf(payload[lastIndex], payload[lastIndex + 1], payload[lastIndex + 2]))
            lastIndex += 3
        }
        when (additionSize) {
            1 -> result.add(listOf(payload[payload.size - 1]))
            2 -> result.add(listOf(payload[payload.size - 2], payload[payload.size - 1]))
        }
        return result.toTypedArray()

    }


    fun getThemesArray(): Array<Int> {
        return arrayOf(
            R.style.AvocadoTheme,
            R.style.PurpleTheme,
            R.style.OrangeTheme,
            R.style.BlueTheme,
            R.style.KiwiTheme,
            R.style.StrawberryTheme,
            R.style.WatermelonTheme
        )
    }

    fun onUserExit(userLogin: String): onExitAnswer {
        if (DataBaseHandler.getUserList().size == 1) {
            DataBaseHandler.deleteUserData(userLogin)
            PreferencesRepository.setCurrentGlobalUser(AuthData())
            return onExitAnswer.EMPTY_USER_LIST
        }

        if (userLogin == PreferencesRepository.getCurrentGlobalUser().login) { //if user exits from current user
            val nextUserAuthData = if (userLogin == DataBaseHandler.getUserList()
                    .first().second.login
            ) DataBaseHandler.getUserList()[1].second else DataBaseHandler.getUserList()
                .first().second

            val authStatus =
                NetworkHelper.checkAuth(nextUserAuthData)

            return if (authStatus.first == Answers.SUCCESSFUL && authStatus.second != null) {
                DataBaseHandler.deleteUserData(userLogin)
                NetworkHelper.clearCookies()
                PreferencesRepository.setCurrentGlobalUser(nextUserAuthData)
                NetworkHelper.saveUser(null)
                onExitAnswer.USER_UPDATED_SUCCESFULLY

            } else {
                NetworkHelper.clearCookies()
                onExitAnswer.ERROR
            }
        } else {
            DataBaseHandler.deleteUserData(userLogin)
            NetworkHelper.clearCookies()
            return onExitAnswer.USER_UPDATED_SUCCESFULLY
        }


    }

    fun getNumberOfQuarter(rawChar: Char): String {
        return when (rawChar) {
            '1' -> {
                "I"
            }

            '2' -> {
                "II"
            }

            '3' -> {
                "III"
            }

            '4' -> {
                "IV"
            }

            else -> {
                rawChar.toString()
            }
        }
    }


}

enum class onExitAnswer {
    EMPTY_USER_LIST, USER_UPDATED_SUCCESFULLY, ERROR
}

enum class LoadingStates {
    LOADING, ERROR
}