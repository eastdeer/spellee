package com.charlesma.spellee.test.abrsm.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.charlesma.spellee.test.abrsm.datamodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING


class PianoRepository(val context: Context) {
    private val db = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firestoreSettings = settings
    }
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val fireBaseConfiguration: ABRSMCloudConfiguration by lazy {
            var firebaseConfig =
                FirebaseRemoteConfig.getInstance().getString(PIANO_ABRSM_CONFIGURATION)
            if (DEFAULT_VALUE_FOR_STRING.equals(firebaseConfig)) {
                firebaseConfig =
                    context.assets.open("config.json").bufferedReader().use { it.readText() }
            } else {
                firebaseConfig
            }
            firebaseConfig.let {
                Gson().fromJson(it, ABRSMCloudConfiguration::class.java)
            }
        }

    val pianoPerformanceAfterWords: String get() = fireBaseConfiguration.pianoAbrsmAfterWords

    val abrsmBook: ABRSMBook get() =fireBaseConfiguration.abrsmBook.apply {
        getDrillCount(this)
    }

    val awardGifUrlList : Array<String> get() = fireBaseConfiguration.awardGifList

    private fun getDrillCount(abrsmBook: ABRSMBook) {
        auth.currentUser?.let {
            val recordsRef = db.collection("userlogs").document(it.uid)
                .collection("records")
            val abrsmRef = recordsRef.document("abrsm")
            abrsmRef.get().addOnCompleteListener { docSearchRef ->
                if (docSearchRef.isSuccessful) {
                    if (docSearchRef.result?.exists() == true) {
                        abrsmBook.chapters.forEach { chapter ->
                            val cloudRecordedCount =
                                docSearchRef.result?.data?.get(chapter.name) as Long?
                            chapter.drillCount.set(cloudRecordedCount?.toInt() ?: 0)
                        }
                    } else {
                        hashMapOf<String, Int>()
                    }
                }
            }
        }
    }

    fun updateDrillCount(chapter: Chapter) {
        auth.currentUser?.let {
            val recordsRef = db.collection("userlogs").document(it.uid)
                .collection("records")
            val abrsmRef = recordsRef.document("abrsm")
            abrsmRef.get().addOnCompleteListener { docSearchRef ->
                //                if (docSearchRef.result?.exists() == true) {
//                    docSearchRef.result?.data?.set(chapter.name, chapter.drillCount.get())
//                    SetOptions.merge()
//                } else

                if (docSearchRef.isSuccessful) {
                    val doc = HashMap<String, Int>()
                    doc[chapter.name] = chapter.drillCount.get()
                    abrsmRef.set(
                        doc as MutableMap<String, Any>, SetOptions.merge()
                    )

//                        .addOnCompleteListener{ docAddRef ->
//                        if(docAddRef.isSuccessful){
//                            docAddRef.result?. = "abrsm"
//                        }
                }
            }
        }
    }
}