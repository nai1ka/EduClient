package ru.ndevelop.educlient.ui.mainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(){
    val chosenItemId = MutableLiveData<Int>()
}