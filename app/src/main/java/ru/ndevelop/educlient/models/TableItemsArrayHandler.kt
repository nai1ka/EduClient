package ru.ndevelop.educlient.models

import ru.ndevelop.educlient.models.TableItem

data class TableItemsArrayHandler(
    val userLogin:String,
    val tableArray :ArrayList<TableItem>

)