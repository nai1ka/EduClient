package ru.ndevelop.educlient.ui.table

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ndevelop.educlient.models.Message
import ru.ndevelop.educlient.models.MessageTypes
import ru.ndevelop.educlient.models.TableItem
import ru.ndevelop.educlient.models.TableItemsArrayHandler
import ru.ndevelop.educlient.repositories.PreferencesRepository
import ru.ndevelop.educlient.utils.DataBaseHandler
import ru.ndevelop.educlient.utils.NetworkHelper
import ru.ndevelop.educlient.utils.ParserHelper
import ru.ndevelop.educlient.utils.Utils

class TableViewModel : ViewModel() {
    val table = MutableLiveData<Pair<TableItemsArrayHandler, Boolean>>() // boolean - isFirstLaunch
    val error =MutableLiveData<String>()
    val message = MutableLiveData<Message>()

    init {
        try {
            val userLogin = PreferencesRepository.getCurrentGlobalUser().login
            val existingTable = DataBaseHandler.getTable(userLogin)
            if (existingTable.tableArray.size > 0) {
                if (table.value?.first?.tableArray.isNullOrEmpty())
                    table.value = Pair(existingTable, true)
                loadTable()
            } else {
                message.value = Message(MessageTypes.FIRST_LAUNCH_SIGNAL)
                loadTable()
            }
        }
        catch (e: Exception) {
            error.value = Utils.handleException(e)
        }
    }

    fun loadTable() {
        CoroutineScope(Dispatchers.Default).launch {
            val tableArrayList: ArrayList<TableItem>
            val downloadedTable = NetworkHelper.getTable()
            var e: Exception? = null
            try {
                if (downloadedTable.second == null) {
                    tableArrayList = ParserHelper.parseTable(downloadedTable.first)
                    withContext(Dispatchers.Main) {
                        if (tableArrayList.size > 0) {
                            table.value = Pair(TableItemsArrayHandler(downloadedTable.third,tableArrayList) , false)
                            DataBaseHandler.saveTable(
                                downloadedTable.third,
                                tableArrayList
                            )
                        } else {
                            error.value =
                                "Ошибка обработки данных. Обычно это происходит, когда сайт edu.tatar.ru недоступен"
                        }
                    }
                } else {
                    e = downloadedTable.second
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