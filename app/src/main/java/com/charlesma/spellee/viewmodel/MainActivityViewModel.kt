package com.charlesma.spellee.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel:ViewModel() {
    val textToSpeechReadyLiveData = MutableLiveData<Boolean>(false)
    val textToSpeechLiveData = MutableLiveData<String>()
}