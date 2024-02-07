package ru.ndevelop.educlient.models

import android.graphics.Bitmap

data class User(
    var avatar:Bitmap?,
    var fullName: String = "",
    var school: String = "",
    var date: String = "",
    var sex: String = ""
)
