package ru.ndevelop.educlient.models


import org.jsoup.nodes.Element

data class QuarterItem(
    val lessonName:String,
    val marks:List<Element>
)
