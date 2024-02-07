/*
 * Created by Na1ka on 08.05.20 21:46
 *
 */

package ru.ndevelop.educlient

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.yandex.mobile.ads.common.MobileAds


class App : MultiDexApplication() {
    companion object {
        private var instance: App? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

       // MobileAds.initialize(this) {}
      //  MobileAds.setAgeRestrictedUser(true)

    }
}