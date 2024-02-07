package ru.ndevelop.educlient.ui.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParserException
import ru.ndevelop.educlient.App
import ru.ndevelop.educlient.R
import ru.ndevelop.educlient.models.DaysHolder
import ru.ndevelop.educlient.models.IpException
import ru.ndevelop.educlient.models.Message
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.*
import ru.ndevelop.educlient.utils.NetworkHelper.DEBUG_LOGIN

class DiaryViewModel : ViewModel() {

    private val days = MutableLiveData<Pair<DaysHolder,Boolean>>()
    val error = MutableLiveData<String>()
    var message = MutableLiveData<Message>()

    init {
        if (PreferencesRepository.getDiary() != "") {

            try {
                val tempDairy =  ParserHelper.parseDiary(PreferencesRepository.getDiary())
                days.value =  DaysHolder(tempDairy,Utils.getSubLists(tempDairy)) to true
                loadDiary()
            } catch (parseException: XmlPullParserException) {
                PreferencesRepository.saveDiary("")
                loadDiary()
            } catch (e: Exception) {
               error.value = Utils.handleException(e)
            }

        } else {
            CurrentDiaryState.isFirstLaunch= false
            message.value = Message(MessageTypes.FIRST_LAUNCH_SIGNAL)
            loadDiary()
        }
    }

    fun loadDiary() {

        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.IO) {

               val rawResult = if(PreferencesRepository.getCurrentGlobalUser().login==DEBUG_LOGIN){
                   Jsoup.parse(App.applicationContext().resources.getRawTextFile(R.raw.diary)) to null
                } else NetworkHelper.getSite("https://edu.tatar.ru/user/diary.xml")
                var e: Exception? = null
                try {
                    if( rawResult.first.body().text()=="Restricted IP") throw IpException()
                    if (rawResult.second == null) {
                        val result = ParserHelper.parseDiary(rawResult.first.body().toString())
                        withContext(Dispatchers.Main) {
                            days.value = DaysHolder(result,Utils.getSubLists(result)) to false
                            PreferencesRepository.saveDiary(rawResult.first.body().toString())
                        }
                    } else e = rawResult.second!!
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


    fun getDays() = days


}