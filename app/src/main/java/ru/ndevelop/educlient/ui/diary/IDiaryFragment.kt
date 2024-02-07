package ru.ndevelop.educlient.ui.diary

import androidx.fragment.app.Fragment

abstract class IDiaryFragment: Fragment() {
    abstract fun setToToday()
    abstract fun updateDiary()
    abstract fun showFAB()
    abstract fun hideFAB()

}