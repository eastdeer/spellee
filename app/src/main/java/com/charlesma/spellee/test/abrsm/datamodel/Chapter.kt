package com.charlesma.spellee.test.abrsm.datamodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import java.text.SimpleDateFormat
import java.util.*

class Chapter {

    lateinit var name: String
    var description: String? = null
    var snippetList: Array<Snippet>? = null

    val status by lazy { ObservableBoolean(false) }
    val drillCount by lazy { ObservableInt(0) }
    var recordFileList :MutableList<CloudRecordFile> = ArrayList<CloudRecordFile>(MAX_CLOUD_RECORD_FILE_COUNT)

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


    private fun canHaveMoreRecord(max_record_count:Int) = recordFileList.size < max_record_count

    private fun getRecordFileName(recordCompleteTimeInMilliSeconds: Long): String = Date().run {
        time = recordCompleteTimeInMilliSeconds
        SimpleDateFormat("dd.MM.yyyy HH:mm").format(this)
    }

    fun addNewRecordFile(recordFileName: String, max_record_count:Int): String {
        if (!canHaveMoreRecord(max_record_count) && recordFileList.isNotEmpty()) {
            recordFileList.removeAt(0)
        }
        return recordFileName.apply {
            recordFileList.add(CloudRecordFile(this))
        }
    }
}