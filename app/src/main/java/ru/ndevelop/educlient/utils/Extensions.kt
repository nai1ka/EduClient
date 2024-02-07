package ru.ndevelop.educlient.utils

import android.content.res.Resources
import android.view.View
import androidx.annotation.RawRes
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

const val dragCoefficient = 2
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(
        recyclerView,
        touchSlop * dragCoefficient
    )       // "2" was obtained experimentally
}

fun CardView.hide() {
    this.visibility = View.GONE
}

fun CardView.show() {
    this.visibility = View.VISIBLE
}

fun Resources.getRawTextFile(@RawRes id: Int) =
    openRawResource(id).bufferedReader().use { it.readText() }

