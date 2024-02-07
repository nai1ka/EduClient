package ru.ndevelop.educlient.models


class HomeworkDay {
    var date:Int = 1
    var month:String = ""
    var isDayEmpty = true
    var tasks: ArrayList<HomeworkItem> = arrayListOf(HomeworkItem(), HomeworkItem(),HomeworkItem(),HomeworkItem(),HomeworkItem(),HomeworkItem(),HomeworkItem(),HomeworkItem())
    fun clearTasks(){
        tasks = arrayListOf()
    }
}