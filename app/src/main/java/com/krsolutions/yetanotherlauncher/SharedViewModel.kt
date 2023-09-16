package com.krsolutions.yetanotherlauncher

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel(){
    val serverip = MutableLiveData<String>()

    init {
        serverip.value = "http://192.168.2.93/YAL.apk"
    }
    fun setServerIp ( text : String) {
        serverip.value = text
    }


}