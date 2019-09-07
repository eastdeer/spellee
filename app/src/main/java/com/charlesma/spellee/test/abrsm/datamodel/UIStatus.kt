package com.charlesma.spellee.test.abrsm.datamodel

import androidx.annotation.IntDef
import com.charlesma.spellee.test.abrsm.datamodel.UIStatus.Companion.STATUS_IDLE
import com.charlesma.spellee.test.abrsm.datamodel.UIStatus.Companion.STATUS_INITIALIZING
import com.charlesma.spellee.test.abrsm.datamodel.UIStatus.Companion.STATUS_PERFORMING
import com.charlesma.spellee.test.abrsm.datamodel.UIStatus.Companion.STATUS_REPLAYING

class GenericDataWithStatus(@StatusCode val status:Int,val data:Any?)

class UIStatus {
    companion object{
        const val STATUS_INITIALIZING = 0
        const val STATUS_PERFORMING = 1
        const val STATUS_IDLE = 2
        const val STATUS_REPLAYING = 3
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(STATUS_INITIALIZING,STATUS_PERFORMING,STATUS_IDLE,STATUS_REPLAYING)
annotation class StatusCode