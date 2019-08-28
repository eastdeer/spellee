package com.charlesma.spellee.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsUtil {
    companion object {
        lateinit var mFirebaseAnalytics: FirebaseAnalytics

        fun logABRSMAdapterDrillStart(
            bookName: String,
            adapterName: String, initialDrillCount: Int
        ) {
            val bundle = Bundle()
            bundle.putString(EventConstant.PARAM_PIANO_BOOK, bookName)
            bundle.putString(EventConstant.PARAM_PIANO_CHAPTER, adapterName)
            bundle.putInt(EventConstant.PARAM_PIANO_CHAPTER_DRILL_COUNT, initialDrillCount)

            logEvent(EventConstant.EVENT_PIANO_DRILL_START, bundle)
        }

        fun logABRSMAdapterDrillComplete(
            bookName: String,
            adapterName: String,
            initialDrillCount: Int,
            durationInSeconds: Int
        ) {
            logABRSMAdapterDrillEnded(
                EventConstant.EVENT_PIANO_DRILL_COMPLETE,
                bookName,
                adapterName,
                initialDrillCount,
                durationInSeconds
            )
        }


        fun logABRSMAdapterDrillFailed(
            bookName: String,
            adapterName: String,
            initialDrillCount: Int,
            durationInSeconds: Int
        ) {
            logABRSMAdapterDrillEnded(
                EventConstant.EVENT_PIANO_DRILL_FAILED,
                bookName,
                adapterName,
                initialDrillCount,
                durationInSeconds
            )
        }

        private fun logABRSMAdapterDrillEnded(
            eventName: String,
            bookName: String,
            adapterName: String,
            initialDrillCount: Int,
            durationInSeconds: Int
        ) {
            val bundle = Bundle()
            bundle.putString(EventConstant.PARAM_PIANO_BOOK, bookName)
            bundle.putString(EventConstant.PARAM_PIANO_CHAPTER, adapterName)
            bundle.putInt(EventConstant.PARAM_PIANO_CHAPTER_DRILL_COUNT, initialDrillCount)
            bundle.putFloat(
                EventConstant.PARAM_PIANO_CHAPTER_DRILL_DURATION,
                durationInSeconds.div(60f)
            )

            logEvent(eventName, bundle)
        }

        fun logEvent(eventName: String, bundle: Bundle = Bundle()) {
            mFirebaseAnalytics.logEvent(eventName, bundle)
        }
    }

}