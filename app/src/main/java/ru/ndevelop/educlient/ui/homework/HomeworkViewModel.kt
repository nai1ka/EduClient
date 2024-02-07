package ru.ndevelop.educlient.ui.homework

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.HomeworkDay
import ru.ndevelop.educlient.models.IpException
import ru.ndevelop.educlient.models.Message
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.*
import ru.ndevelop.educlient.utils.NetworkHelper.DEBUG_LOGIN
import java.util.*

class HomeworkViewModel : ViewModel() {

    val homeworks =
        MutableLiveData<Pair<MutableList<HomeworkDay>, Boolean>>() //true если первый запуск(без загрузки с интернета)
    val error = MutableLiveData<String>()
    val message = MutableLiveData<Message>()
    private var currentDate: Calendar = Calendar.getInstance()
    private val dataBaseHandler = DataBaseHandler

    init {
        if (!dataBaseHandler.isHomeworkTableEmpty()) {
            try {
                if (homeworks.value?.first.isNullOrEmpty())
                    homeworks.value = dataBaseHandler.getHomeworkList() to true
                loadHomeworks()
            } catch (e: Exception) {

                error.value = Utils.handleException(e)
            }

        } else {
            message.value = Message(MessageTypes.FIRST_LAUNCH_SIGNAL)
            loadHomeworks()
        }

    }

    fun loadHomeworks() {
        currentDate = Calendar.getInstance()
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.IO) {
                var e: Exception? = null
                val rawResult =
                    if (PreferencesRepository.getCurrentGlobalUser().login == DEBUG_LOGIN) {
                        Jsoup.parse(App.applicationContext().resources.getRawTextFile(R.raw.diary)) to null
                    } else NetworkHelper.getSite("https://edu.tatar.ru/user/diary.xml")
                try {
                    val calendar = Calendar.getInstance()
                    DataBaseHandler.optimizeDatabase(
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.MONTH) + 1
                    )
                    if(rawResult.first.body().text()=="Restricted IP") throw IpException()
                    if (rawResult.second == null ) {
                        val result = ParserHelper.parseHomework(
                            rawResult.first.body().toString(),
                            currentDate.get(Calendar.DAY_OF_MONTH),
                            currentDate.get(Calendar.MONTH) + 1
                        )
                        if (result.isNotEmpty()) {
                            result.forEach { day ->
                                day.tasks.forEachIndexed { index, item ->
                                    dataBaseHandler.updateIfExistsElseInsert(
                                        item,
                                        day.date.toString() + Utils.getMonthNumber(day.month) + index.toString(),
                                        day.date,
                                        Utils.getMonthNumber(day.month)
                                    )
                                }
                            }
                            val resultTable = dataBaseHandler.getHomeworkList()
                            withContext(Dispatchers.Main) {
                                homeworks.value = resultTable to false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                homeworks.value = arrayListOf<HomeworkDay>() to false
                            }
                        }


                    } else {

                        e = rawResult.second
                    }
                } catch (err: Exception) {
                    e = err
                }
                withContext(Dispatchers.Main) {
                    if (e != null) {
                        error.value = Utils.handleException(e)

                    }
                }
            }
        }
    }
}