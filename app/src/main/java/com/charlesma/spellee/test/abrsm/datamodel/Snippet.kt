package com.charlesma.spellee.test.abrsm.datamodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt

class Snippet {
    companion object{
        const val ITEM_CURRENT = 1
        const val ITEM_PASSED = 2
        const val ITEM_INTACT = 3
    }
    lateinit var name: String
    var description: String? = ""
    var midi: String? = ""
    var statusColor = ObservableInt(0)
}