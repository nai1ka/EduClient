package ru.ndevelop.educlient.utils

import android.view.View
import org.jsoup.select.Elements
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ru.ndevelop.educlient.models.Day
import ru.ndevelop.educlient.models.HomeworkDay
import ru.ndevelop.educlient.models.QuarterItem
import ru.ndevelop.educlient.models.TableItem
import ru.ndevelop.educlient.repositories.PreferencesRepository
import java.io.StringReader


object ParserHelper {
    fun parseDiary(xmlData: String): ArrayList<Day> {
        val dayList = ArrayList<Day>()
        val factory =
            XmlPullParserFactory.newInstance()
        var isDayNumberCorrect = true
        var lastDay = "1"
        var lastMonth = "1"
        var day = Day()

        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(xmlData))
        var eventType = xpp.eventType
        var month = ""
        var diaryVersion = "0"
        while (eventType != XmlPullParser.END_DOCUMENT) {

            val tagName = xpp.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "diary") {
                        diaryVersion = xpp.getAttributeValue(0)
                    }
                    if (tagName == "page")
                        month = xpp.getAttributeValue(0)
                    if (lastMonth != month) isDayNumberCorrect = true

                    if (tagName == "day") {
                        day = Day()
                        day.date = xpp.getAttributeValue(0).replace("\n", "").trim()
                        if (Utils.getDayNumberFromString(day.date) < Utils.getDayNumberFromString(
                                lastDay
                            ) && lastMonth == month
                        )
                            isDayNumberCorrect = false
                        if (!isDayNumberCorrect)
                            day.month = Utils.getNextMonth(month)
                        else day.month = month
                        lastDay = day.date
                        lastMonth = month

                    }
                    if (tagName == "class") {
                        var lesson = xpp.nextText().replace("\n", "").trim()
                        if (lesson == "") lesson = "Нет урока"
                        day.lessons.add(lesson)
                    }
                    if (tagName == "task") {
                        var task = xpp.nextText().replace("\n", "").trim()
                        if (task == "") task = "Здесь пусто..."
                        day.tasks.add(task)
                    }
                    if (tagName == "marks" && xpp.depth == 6) {
                        val mark = xpp.nextText().replace("\n", "").trim()
                        day.marks.add(mark)
                        if (mark != "") {
                            day.visibility.add(View.VISIBLE)
                        } else {
                            day.visibility.add(View.INVISIBLE)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "day") {
                        var emptyInBottom = 0
                        var isLeastOneLesson = false
                        day.lessons.forEach {
                            if (it != "Нет урока") isLeastOneLesson = true
                            if (isLeastOneLesson && it == "Нет урока") emptyInBottom++
                        }
                        //day.lessons.forEach { fullyEmpty += if (it == "Нет урока") 1 else 0 }
                        day.isEmpty = !isLeastOneLesson
                        day.emptyLessonsInBottom = emptyInBottom
                        dayList.add(day)
                    }
                }
            }
            eventType = xpp.next()

        }
        val activeDiaryVersionFromPreferences = PreferencesRepository.getActiveDiaryVersion()
        if (activeDiaryVersionFromPreferences != diaryVersion && PreferencesRepository.getDiary() != "")
            CurrentDiaryState.isTabLayoutNeedsToBeUpdated = true
        PreferencesRepository.setActiveDiaryVersion(diaryVersion)
        return dayList
    }

    fun parseHomework(
        xmlData: String,
        current_day: Int,
        current_month: Int
    ): ArrayList<HomeworkDay> {
        val homeworksList = ArrayList<HomeworkDay>()
        val factory =
            XmlPullParserFactory.newInstance()
        var homeworkDay = HomeworkDay()
        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(xmlData))
        var eventType = xpp.eventType
        var month = ""
        var lesson: String
        var task: String

        var isCorrectDay = false
        var iClasses = 0
        var iTasks = 0
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = xpp.name
            when (eventType) {

                XmlPullParser.START_TAG -> {

                    if (tagName == "page")
                        month = xpp.getAttributeValue(0)
                    if (tagName == "day") {

                        homeworkDay = HomeworkDay()

                        homeworkDay.date = xpp.getAttributeValue(0).replace("\n", "").trim().toInt()
                        homeworkDay.month = month

                    }
                    if (homeworkDay.date >= current_day && Utils.getMonthNumber(homeworkDay.month) >= current_month)
                        isCorrectDay = true

                    if (tagName == "classes") {
                        iClasses = 0
                    }
                    if (tagName == "tasks") {
                        iTasks = 0
                    }
                    if (tagName == "class") {
                        if (isCorrectDay) {
                            lesson = xpp.nextText().replace("\n", "").trim()
                            if (lesson != "") {
                                homeworkDay.isDayEmpty = false
                                homeworkDay.tasks[iClasses].lesson = lesson
                            }
                            iClasses += 1
                        }
                    }
                    if (tagName == "task") {
                        if (isCorrectDay) {
                            task = xpp.nextText().replace("\n", "").trim()
                            if (task == "") task = "Задание не добавлено"
                            if (homeworkDay.tasks[iTasks].lesson != "") {
                                homeworkDay.tasks[iTasks].text = task
                            } else {
                                homeworkDay.tasks.removeAt(iTasks)
                                iTasks -= 1
                            }

                            iTasks += 1
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "day" && isCorrectDay && !homeworkDay.isDayEmpty) {

                        homeworksList.add(homeworkDay)
                    }
                }
            }
            eventType = xpp.next()
        }
        return homeworksList
    }

    fun parseTable(payload: Elements?): ArrayList<TableItem> {
        val tableArrayList: ArrayList<TableItem> = ArrayList()
        if (payload != null)
            for (element in payload) {
                val tableItem = TableItem()
                val selection = element.select("td")
                tableItem.lesson = selection[0].text()
                val tempMarks: ArrayList<String> = ArrayList()
                for (i in 1..selection.size - 3) {
                    val tempText = selection[i].text()
                    if (tempText != "") tempMarks.add(tempText)
                }
                tableItem.finalMark = selection.last().text()
                tableItem.marks = (tempMarks)
                tableArrayList.add(tableItem)

            }
        return tableArrayList
    }

    fun parseQuarter(payload: Elements): ArrayList<QuarterItem> {
        val result: ArrayList<QuarterItem> = arrayListOf()
        val tempPayload = payload.select("tbody").select("tr")

        for (i in tempPayload.indices) {
            val tempResult = tempPayload[i].select("td")
            result.add(QuarterItem(tempResult[0].text(), tempResult.subList(1, tempResult.size)))
        }
        return result

    }

}