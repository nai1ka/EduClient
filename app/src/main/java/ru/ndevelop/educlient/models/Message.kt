package ru.ndevelop.educlient.models

data class Message(
    val type:MessageTypes,
    val text:String = ""
)

enum class MessageTypes{
    FIRST_LAUNCH_SIGNAL, TEXT_MESSAGE
}