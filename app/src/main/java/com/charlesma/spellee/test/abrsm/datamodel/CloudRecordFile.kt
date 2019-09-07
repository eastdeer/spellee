package com.charlesma.spellee.test.abrsm.datamodel

import androidx.databinding.ObservableBoolean

class CloudRecordFile(val fileName: String, status: Boolean) {
    constructor(fileName: String):this(fileName, false)

    val observableStatus = ObservableBoolean(false)

    init {
        observableStatus.set(status)
    }
}