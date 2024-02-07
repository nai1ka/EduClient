package ru.ndevelop.educlient.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.databinding.ActivityLoginBinding
import ru.ndevelop.educlient.models.AuthData
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.ui.mainActivity.MainActivity
import ru.ndevelop.educlient.utils.*
import kotlin.system.exitProcess


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val db = DataBaseHandler
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(PreferencesRepository.getTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnSubmit.setOnClickListener(this)
        binding.btnSubmit.isEnabled = false
        binding.etLogin.doOnTextChanged { text, start, count, after ->
            binding.btnSubmit.isEnabled =
                !(binding.etPassword.text.isNullOrEmpty() or text.isNullOrBlank())
        }
        binding.etPassword.doOnTextChanged { text, start, count, after ->
            binding.btnSubmit.isEnabled =
                !(binding.etLogin.text.isNullOrEmpty() or text.isNullOrBlank())
        }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doLoginJob()
            }
            false
        }
        binding.frLogin.foreground.alpha = 0
    }

    private fun exitApp() {
        finishAffinity()
        exitProcess(1)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (db.getUserList().size == 0) {
            if (doubleBackToExitPressedOnce) {
                exitApp()
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        } else finish()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubmit -> {
                doLoginJob()
            }

        }
    }

    private suspend fun setButtonState(state: Boolean) {
        withContext(Dispatchers.Main) {
            when (state) {
                true -> {
                    binding.btnSubmit.isEnabled = true
                }
                else -> {
                    binding.btnSubmit.isEnabled = false
                }
            }
        }
    }

    private suspend fun hideLoadingScreen() {
        withContext(Dispatchers.Main) {
            binding.frLogin.foreground.alpha = 0
            binding.pbLogin.visibility = View.GONE

        }
    }

    private fun doLoginJob() {

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Нет доступа в интернет", Toast.LENGTH_SHORT).show()
            return
        }
        Utils.hideKeyboardForm(this@LoginActivity, binding.frLogin)
        if (!binding.etLogin.text.isNullOrEmpty() and !binding.etPassword.text.isNullOrBlank()) {
            val i = Intent(this, MainActivity::class.java)
            CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) {
                    binding.frLogin.foreground.alpha = 160
                    binding.pbLogin.visibility = View.VISIBLE
                }
                setButtonState(false)
                val status = NetworkHelper.checkAuth(
                    AuthData(
                        binding.etLogin.text.toString(),
                        binding.etPassword.text.toString()
                    )
                )
                if (status.first == Answers.SUCCESSFUL && status.second != null) {
                    val userName =
                        NetworkHelper.saveUser(status.second!!)
                    if (userName != null) {
                        val userAddStatus =
                            db.addUser(
                                userName to AuthData(
                                    binding.etLogin.text.toString(),
                                    binding.etPassword.text.toString()
                                )
                            )
                        PreferencesRepository.setCurrentGlobalUser(
                            AuthData(
                                binding.etLogin.text.toString(),
                                binding.etPassword.text.toString()
                            )
                        )

                        if (!userAddStatus) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Пользователь с такими данными уже существует. Будет произведён вход в существующего",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }

                    startActivity(i)
                } else if (status.first == Answers.PASSWORD_ERROR && status.second != null) {


                    Snackbar.make(
                        binding.etLogin,
                        "Неверный логин или пароль!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    setButtonState(true)
                    hideLoadingScreen()
                } else if (status.first == Answers.DDOS_ERROR && status.second != null) {
                    hideLoadingScreen()
                    Snackbar.make(
                        binding.etLogin,
                        "Слишком много неудачных попыток ввода. Попробуйте позже",
                        Snackbar.LENGTH_LONG
                    ).show()
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(7000)
                        withContext(Dispatchers.Main) {
                            setButtonState(true)
                        }
                    }

                } else if (status.first == Answers.LOADING_ERROR && status.second == null) {

                    Snackbar.make(
                        binding.etLogin,
                        "Ошибка загрузки. Скорее всего, что-то с сайтом edu.tatar.ru",
                        Snackbar.LENGTH_LONG
                    ).show()
                    hideLoadingScreen()
                    setButtonState(true)
                } else if (status.first == Answers.IP_ERROR) {

                    Snackbar.make(
                        binding.etLogin,
                        "Ошибка загрузки. Убедитесь, что ваш IP-адрес - российский",
                        Snackbar.LENGTH_LONG
                    ).show()
                    hideLoadingScreen()
                    setButtonState(true)
                }
            }


        } else {
            Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()

        }
    }

}
