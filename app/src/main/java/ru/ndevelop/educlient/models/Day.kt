package ru.ndevelop.educlient.models

data class Day(
    var month: String = "",
    var date: String = "",
    var lessons: ArrayList<String> = ArrayList(),
    var tasks: ArrayList<String> = ArrayList(),
    var marks: ArrayList<String> = ArrayList(),
    var visibility: ArrayList<Int> = ArrayList(),
    var isEmpty: Boolean = true,
    var emptyLessonsInBottom:Int = 0 //считается только после всех уроков(первые пустые уроки не учитываются)
)
