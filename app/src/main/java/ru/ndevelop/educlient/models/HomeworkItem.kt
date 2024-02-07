package ru.ndevelop.educlient.models

class HomeworkItem
 {
    var lesson: String = ""
    var text: String = ""
    var isDone: Boolean = false

     fun clear(){
         lesson = ""
         text = ""
         isDone = false
     }
}