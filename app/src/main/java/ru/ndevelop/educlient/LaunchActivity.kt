package ru.ndevelop.educlient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.LoginActivity
import ru.ndevelop.educlient.ui.mainActivity.MainActivity
import ru.ndevelop.educlient.utils.Utils

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferencesRepository.getCurrentGlobalUser().login != "" ) {
            val i = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        } else {
            val i = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
        super.onCreate(savedInstanceState)
    }


}