package ru.ndevelop.educlient.repositories

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.PreferenceManager
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.AuthData
import ru.ndevelop.educlient.models.User
import ru.ndevelop.educlient.ui.mainActivity.Background
import java.io.ByteArrayOutputStream


object PreferencesRepository {

    private val prefs: SharedPreferences by lazy {
        val ctx = App.applicationContext()
        PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    fun setIsNotFirstLaunch() {
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("FIRST_LAUNCH", false)
        editor.apply()
    }

    fun getChosenTheme(): Int {

        return when (prefs.getString("THEME_TYPE", "Default")!!) {
            "Light" -> MODE_NIGHT_NO
            "Dark" -> MODE_NIGHT_YES
            "Default" -> MODE_NIGHT_FOLLOW_SYSTEM
            else -> MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun isFirstLaunch() = prefs.getBoolean("FIRST_LAUNCH", true)
    fun saveUser(user: User) {

        val editor: SharedPreferences.Editor = prefs.edit()

        val baos = ByteArrayOutputStream()
        user.avatar?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val compressImage: ByteArray = baos.toByteArray()
        val sEncodedImage: String = Base64.encodeToString(compressImage, Base64.DEFAULT)
        with(editor) {
            putString("NAME", user.fullName)
            putString("SCHOOL", user.school)
            putString("DATE", user.date)
            putString("SEX", user.sex)
            putString("BITMAP", sEncodedImage)


        }
        editor.apply()
    }

    fun getDiary(): String {
        return prefs.getString("DATA", "")!!
    }

    fun saveDiary(payload: String) {
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString("DATA", payload)
        editor.apply()
    }

    fun getUser(): User {
        with(prefs) {
            return User(
                getBitmap(),
                getString("NAME", "")!!,
                getString("SCHOOL", "")!!,
                getString("DATE", "")!!,
                getString("SEX", "")!!
            )
        }

    }

    private fun getBitmap(): Bitmap? {
        val pref = prefs.getString("BITMAP", "")
        val b: ByteArray =
            Base64.decode(pref, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(b, 0, b.size)

    }

    fun saveBackground(data: Bitmap) {
        val mSettings = prefs
        val editor = mSettings.edit()
        val baos = ByteArrayOutputStream()
        data.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val compressImage: ByteArray = baos.toByteArray()
        val sEncodedImage: String = Base64.encodeToString(compressImage, Base64.DEFAULT)
        editor.putString("BACKGROUND_IMAGE", sEncodedImage)
        editor.apply()
    }

    fun setBackgroundType(type: Background) {
        val mSettings = prefs
        val editor = mSettings.edit()
        editor.putInt("BG_TYPE", type.ordinal)
        editor.apply()
    }

    fun getBackgroundType(): Background {
        return Background.values()[prefs.getInt("BG_TYPE", 0)]
    }

    fun getBackgroundRaw(): String {
        return prefs.getString("BACKGROUND_IMAGE", "")!!
    }


    fun setCurrentGlobalUser(authData: AuthData) {
        val mSettings = prefs
        val editor = mSettings.edit()
        editor.putString("LOGGED_USER_LOGIN", authData.login)
        editor.putString("LOGGED_USER_PASSWORD", authData.password)
        editor.apply()
    }

    fun getCurrentGlobalUser() =
        AuthData(
            prefs.getString("LOGGED_USER_LOGIN", "")!!,
            prefs.getString("LOGGED_USER_PASSWORD", "")!!
        )


    fun setTheme(theme: Int) {
        val mSettings = prefs
        val editor = mSettings.edit()
        editor.putInt("START_THEME", theme)
        editor.apply()
    }

    fun getTheme(): Int {
        return prefs.getInt("START_THEME", R.style.KiwiTheme)
    }

    fun setActiveDiaryVersion(version: String) {
        val mSettings = prefs
        val editor = mSettings.edit()
        editor.putString("DIARY_VERSION", version)
        editor.apply()
    }

    fun getActiveDiaryVersion(): String {
        return prefs.getString("DIARY_VERSION", "") ?: ""
    }

    fun getIsSingleDayMode():Boolean{
        return prefs.getBoolean("ONLY_ONE_DAY_DIARY", false)
    }


}
