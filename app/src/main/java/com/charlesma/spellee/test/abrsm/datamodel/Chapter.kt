package com.charlesma.spellee.test.abrsm.datamodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt

class Chapter(){

    lateinit var name: String
    var description: String? = null
    var snippetList: Array<Snippet>? =null

    val status by lazy { ObservableBoolean(false) }
    val drillCount by lazy { ObservableInt(0) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chapter

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}