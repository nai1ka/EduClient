package ru.ndevelop.educlient.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.AuthData
import ru.ndevelop.educlient.models.QuarterTable
import ru.ndevelop.educlient.models.User
import ru.ndevelop.educlient.repositories.PreferencesRepository
import java.io.InputStream
import java.net.*
import java.util.*

object NetworkHelper {
    private var cookies: Map<String, String>? = null
    const val TIMEOUT = 10000
    const val DEBUG_LOGIN = "4823114196"

    private fun getAuthContext(authData: AuthData): Connection.Response {
        return Jsoup.connect("https://edu.tatar.ru/logon")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 YaBrowser/20.4.2.201 Yowser/2.5 Safari/537.36")
            .data("main_login2", authData.login)
            .data("main_password2", authData.password)
            .referrer("https://edu.tatar.ru/logon")
            .followRedirects(true)
            .method(Connection.Method.POST)
            .timeout(TIMEOUT)
            .header("Connection", "close")
            .execute()
    }

    private fun getDemoAuthContext(): Document {
        return Jsoup.parse(App.applicationContext().resources.getRawTextFile(R.raw.anketa))

    }

    fun getSite(site: String): Pair<Document, Exception?> {

        return try {
            val authData =
                PreferencesRepository.getCurrentGlobalUser()
            if (cookies.isNullOrEmpty()) {
                val loginPage = getAuthContext(authData)
                cookies = loginPage.cookies()
            }
            val document = Jsoup.connect(site)
                .cookies(cookies)
                .timeout(TIMEOUT)
                .get()
            document to null
        } catch (e: Exception) {
            Document("") to e
        }
    }


    fun checkAuth(authData: AuthData): Pair<Answers, Document?> {
        try {

            if (authData.login == DEBUG_LOGIN) {
                return Answers.SUCCESSFUL to getDemoAuthContext()
            }

            val page = getAuthContext(authData)
            // ФИО : page.parse().select("table.tableEx").select("td")[1].text()
            // Школа page.parse().select("table.tableEx").select("td")[5].text()
            //Дата page.parse().select("table.tableEx").select("td")[9].text()
            // Пол page.parse().select("table.tableEx").select("td")[11].text()
            //Интересы page.parse().select("table.tableEx").select("td")[13].text()
            val parsedPage = page.parse()
            val errorText = parsedPage.select("div.inner")
                .text()
            return when (errorText) {
                "Неверный логин или пароль. Забыли пароль?" -> {
                    clearCookies()
                    Answers.PASSWORD_ERROR to parsedPage
                }
                "Доступ в систему временно заблокирован" -> {
                    clearCookies()
                    Answers.DDOS_ERROR to parsedPage
                }
                "Введите логин и пароль." -> {
                    clearCookies()

                    Answers.LOGIN_ERROR to parsedPage
                }
                "Restricted IP" -> {
                    clearCookies()
                    Answers.IP_ERROR to parsedPage
                }
                else -> {
                    if(parsedPage.select("div.panel-body").size!=0){
                        cookies = page.cookies()
                        Answers.SUCCESSFUL to parsedPage
                    }
                    else{
                        clearCookies()
                        Answers.PASSWORD_ERROR to parsedPage
                    }

                }
            }
        } catch (e: Exception) {
            return Answers.LOADING_ERROR to null
        }
    }

    fun getTable(): Triple<Elements, Exception?, String> {
        val authData = PreferencesRepository.getCurrentGlobalUser()
        try {
            if (authData.login == DEBUG_LOGIN) {
                val document =
                    Jsoup.parse(App.applicationContext().resources.getRawTextFile(R.raw.table))
                return Triple(
                    document.select("table[class=table term-marks]").select("tbody").select("tr"),
                    null,
                    authData.login
                )
            }

            if (cookies == null) {
                val page = getAuthContext(authData)
                cookies = page.cookies()
            }
            val document = Jsoup.connect("https://edu.tatar.ru/user/diary/term")
                .cookies(cookies)
                .timeout(TIMEOUT)
                .get()
            return Triple(
                document.select("table[class=table term-marks]").select("tbody").select("tr"),
                null,
                authData.login
            )
        } catch (e: Exception) {
            return Triple(Elements(), e, authData.login)
        }

    }

    fun getQuarterMarks(): Pair<QuarterTable?, Exception?> {
        try {

            val userData = PreferencesRepository.getCurrentGlobalUser()

            if (cookies == null && userData.login != DEBUG_LOGIN) {
                val page = getAuthContext(userData)
                cookies = page.cookies()
            }
            val data =
                if (userData.login == DEBUG_LOGIN) Jsoup.parse(
                    App.applicationContext().resources.getRawTextFile(
                        R.raw.quarters
                    )
                )
                else Jsoup.connect("https://edu.tatar.ru/user/diary/year?term=year")
                    .cookies(cookies)
                    .timeout(TIMEOUT)
                    .get()
            val selectedTable = data.select("table.table")
            val tableTypeItems: ArrayList<String> = arrayListOf()
            selectedTable.select("thead").select("tr").select("td")
                .forEachIndexed { index, element -> if (index > 0) tableTypeItems.add(element.text()) }
            return QuarterTable(selectedTable, tableTypeItems) to null
        } catch (e: Exception) {
            return null to e
        }
    }

    fun clearCookies() {
        cookies = null
    }


    fun saveUser(doc: Document? = null): String? {
        try {
            val table: Elements
            val imageUrl: String
            if (doc != null) {
                table = doc.select("table.tableEx").select("td")
                imageUrl = "https://edu.tatar.ru${
                    doc.select("img[src$=.jpg][class=img-thumbnail]").attr("src")
                }"
            } else {
                val userData = PreferencesRepository.getCurrentGlobalUser()
                val page = getAuthContext(userData).parse()
                table = page.select("table.tableEx").select("td")
                imageUrl = "https://edu.tatar.ru${
                    page.select("img[src$=.jpg][class=img-thumbnail]").attr("src")
                }"
            }

            CoroutineScope(Dispatchers.Default).launch {
                var school = ""
                var birthday = ""
                var sex = ""
                var name = ""
                for (i in table.indices) {
                    when (table[i].text()) {
                        "Имя:" -> name = table[i + 1].text()
                        "Школа" -> school = table[i + 1].text()
                        "Дата рождения:" -> birthday = table[i + 1].text()
                        "Пол:" -> sex = table[i + 1].text()
                    }
                }
                val user = User(
                    downloadImageFromPath(imageUrl),
                    name,
                    school, birthday, sex
                )
                PreferencesRepository.saveUser(
                    user
                )
            }
            return table[1].text()
        } catch (e: Exception) {
            return null
        }
    }


    private suspend fun downloadImageFromPath(path: String?): Bitmap? {
        var bmp: Bitmap? = null
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val `in`: InputStream?
                val responseCode: Int
                try {
                    val url = URL(path)
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                    con.doInput = true
                    con.connect()
                    responseCode = con.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        `in` = con.inputStream
                        bmp = BitmapFactory.decodeStream(`in`)
                        `in`.close()

                    } else bmp = null

                } catch (ex: Exception) {
                    Log.e("Exception", ex.toString())
                }
            }
        }
        return bmp
    }
}

enum class Answers {
    SUCCESSFUL, PASSWORD_ERROR, DDOS_ERROR, LOADING_ERROR, LOGIN_ERROR, IP_ERROR
}