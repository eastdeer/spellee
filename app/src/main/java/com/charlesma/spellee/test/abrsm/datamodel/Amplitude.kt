package com.charlesma.spellee.test.abrsm.datamodel

class Amplitude(val level:Int) {
    companion object {
        const val SIG_SAMPLING = 1
        const val SIG_STOP_ONE_RECORD = 2
        const val SIG_START_ONE_RECORD = 3
        const val SIG_CLEAR = 4
    }
}