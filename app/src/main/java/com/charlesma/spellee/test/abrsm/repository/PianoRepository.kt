package com.charlesma.spellee.test.abrsm.repository

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.charlesma.spellee.test.abrsm.datamodel.*
import com.charlesma.spellee.util.AnalyticsUtil
import com.charlesma.spellee.util.EventConstant.Companion.EVENT_CLOUD_FILE_FAILED
import com.charlesma.spellee.util.EventConstant.Companion.PARAM_CLOUD_FILE_LIST
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.ArrayList


class PianoRepository(val context: Context) {
    private val db = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firestoreSettings = settings
    }
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val storageRef = FirebaseStorage.getInstance().reference


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

    val pianoCloudFileMaxCount:Int get()= fireBaseConfiguration.pianoMaxCloudFileCount

    val abrsmBook: ABRSMBook
        get() = fireBaseConfiguration.abrsmBook.apply {
            fulfillFromFireStore(this)
        }

    val awardGifUrlList: Array<String> get() = fireBaseConfiguration.awardGifList

    private fun fulfillFromFireStore(abrsmBook: ABRSMBook) {
        auth.currentUser?.let {
            val recordsRef = db.collection("userlogs").document(it.uid)
                .collection("records")
            val abrsmRef = recordsRef.document("abrsm") // TODO: upgrade to dynamic book name
            abrsmRef.get().addOnCompleteListener { docSearchRef ->
                if (docSearchRef.isSuccessful) {
                    if (docSearchRef.result?.exists() == true) {
                        abrsmBook.chapters.forEach { chapter ->
                            val cloudRecordedCount =
                                docSearchRef.result?.data?.get(chapter.name) as Long?
                            chapter.drillCount.set(cloudRecordedCount?.toInt() ?: 0)
                            chapter.recordFileList = (
                                    docSearchRef.result?.data?.get("${chapter.name}$PIANO_ABRSM_CHAPTER_SUFFIX") as ArrayList<String>?
                                        ?: arrayListOf()
                                    ).map { fileName -> CloudRecordFile(fileName) }.toMutableList()
                        }
                    } else {
                        hashMapOf<String, Int>()
                    }
                }
            }
        }
    }

    fun updateFireStore(abrsmBookName: String, chapter: Chapter) {
        auth.currentUser?.let {
            val recordsRef = db.collection("userlogs").document(it.uid)
                .collection("records")
            val abrsmRef = recordsRef.document("abrsm") // TODO: upgrade to dynamic book name
            abrsmRef.get().addOnCompleteListener { docSearchRef ->
                //                if (docSearchRef.result?.exists() == true) {
//                    docSearchRef.result?.data?.set(chapter.name, chapter.drillCount.get())
//                    SetOptions.merge()
//                } else

                if (docSearchRef.isSuccessful) {
                    val doc = HashMap<String, Any>()
                    doc[chapter.name] = chapter.drillCount.get()
                    doc["${chapter.name}$PIANO_ABRSM_CHAPTER_SUFFIX"] = chapter.recordFileList.map { it.fileName }
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

    fun moveRecordFileToUploadFile(recordPathFileName: String, uploadPathFileName: String) {
        val sourceFile = File(recordPathFileName)
        val destFile = File(uploadPathFileName)

        if (destFile.exists()) {
            destFile.delete()
        }

        if (sourceFile.exists()) {
            sourceFile.renameTo(destFile)
        }
    }

    private fun getABRSMChapterRecordDir(userId: String, bookName: String, chapterName: String) =
        "userupload/$userId/${getFireBaseValidName(bookName)}/${getFireBaseValidName(chapterName)}"

    fun getFireBaseValidName(bucketOrFileName: String) =
        bucketOrFileName.replace(Regex("[\\W]+"), "_")

    /**
     * this method may fail, the consequence is losing a record file
     */
    fun uploadRecordFile(
        bookName: String,
        chapter: Chapter,
        sourceFileName: String,
        destFileName: String
    ) {
        auth.currentUser?.let {
            val file = Uri.fromFile(File(sourceFileName))
            val firebaseDir = getABRSMChapterRecordDir(it.uid, bookName, chapter.name)
            val uploadFileRef = storageRef.child("$firebaseDir/$destFileName")

            uploadFileRef.putFile(file)
                .addOnSuccessListener { taskSnapshot ->
                    // Get a URL to the uploaded content
                    val downloadUrl = uploadFileRef.downloadUrl
                    updateRecordFileStatusIfExisting(
                        bookName,
                        chapter,
                        destFileName
                    )
                }
                .addOnFailureListener {
                    // Handle unsuccessful uploads
                    // ...
                }
        }
    }

    fun refreshChapterRecordFileListAsync(chapter: Chapter, bookName: String, chapterName: String) {
        auth.currentUser?.let {
            val firebaseDir = getABRSMChapterRecordDir(it.uid, bookName, chapterName)
//            val recordDirRef = storageRef.child("$firebaseDir")
            val recordDirRef = storageRef.child(firebaseDir)
            recordDirRef.listAll(/*Chapter.MAX_RECORD_COUNT * 2*/)
                .addOnSuccessListener { listResult ->
                    //                storageReference.
                    listResult.items.forEach { cloudRecordFile ->
                        updateRecordFileStatusIfExisting(bookName, chapter, cloudRecordFile.name)
                        // delete remote files if not existing in log file list
                    }
                }.addOnFailureListener { exception ->
                    AnalyticsUtil.logEvent(EVENT_CLOUD_FILE_FAILED, Bundle().apply {
                        putString(PARAM_CLOUD_FILE_LIST, recordDirRef.path)
                    })
                }

        }

    }

    private fun updateRecordFileStatusIfExisting(
        bookName: String,
        chapter: Chapter,
        cloudRecordFileName: String
    ) {
        val index = chapter.recordFileList.indexOfFirst { it.fileName.equals(cloudRecordFileName) }
        if (index >= 0) {
            chapter.recordFileList.getOrNull(index)?.observableStatus?.set(true)
        } else {
            deleteChapterRecordFile(bookName, chapter.name, cloudRecordFileName)
        }
    }

    fun getChapterRecordFileUrl(bookName: String, chapter: Chapter, index: Int,fireBaseCallBack:(uri:Uri)->Unit) {
        auth.currentUser?.let {
            val firebaseDir = getABRSMChapterRecordDir(it.uid, bookName, chapter.name)
            if (index >= 0 && index < chapter.recordFileList.size) {
                val destFileName = chapter.recordFileList[index].fileName
                val recordFileRef = storageRef.child("$firebaseDir/$destFileName")
                recordFileRef.downloadUrl.addOnSuccessListener { uri -> fireBaseCallBack(uri) }
            }
        }
    }


    fun deleteChapterRecordFile(
        bookName: String,
        chapterName: String,
        destFileName: String
    ) {
        auth.currentUser?.let {
            val firebaseDir = getABRSMChapterRecordDir(it.uid, bookName, chapterName)
            val destFileRef = storageRef.child("$firebaseDir/$destFileName")
            destFileRef.delete().addOnSuccessListener { }
                .addOnFailureListener { }
        }
    }
}