package com.charlesma.spellee.test.abrsm.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.charlesma.spellee.test.abrsm.datamodel.*
import com.charlesma.spellee.test.abrsm.repository.PianoRepository
import com.charlesma.spellee.util.AnalyticsUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

class PianoAbrsmViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SAMPLE_INTERVAL = 128L
        const val SAMPLE_AVG_COUNT = 32
        const val AMPLITUDE_THRESHOLD = 8800
        const val SINGLE_SNIPPET_DURATION_IN_SECONDS = 10

        const val RECORD_OUTPUT_FILENAME = "record_output"
        const val RECORD_UPLOAD_FILENAME = "record_upload"
    }

    val pianoRepository by lazy { PianoRepository(application) }
    val AFTERWORD = Snippet().apply { name = pianoRepository.pianoPerformanceAfterWords }
    val recordDirName: String by lazy {
        application.getExternalFilesDir(Environment.DIRECTORY_MUSIC).absolutePath
    }
    val inPerforming = ObservableBoolean(false)


    val _popupRecordListMutableLiveData = MutableLiveData<Pair<Boolean, Chapter>>()
    val popupRecordListLiveData: LiveData<Pair<Boolean, Chapter>> = _popupRecordListMutableLiveData

    val bookInRepo = liveData {

        emit(pianoRepository.abrsmBook)
//        onChapterClicked(0)
    }

    val avgDrillCount = ObservableInt(0)

    fun getRecordOutputFileName() = "$recordDirName/$RECORD_OUTPUT_FILENAME"

    fun getUploadingFileName() = "$recordDirName/$RECORD_UPLOAD_FILENAME"

    private val userSelectedChapterMutableLiveData = MutableLiveData<Int>()
    val currentChapterLiveData =
        Transformations.map(userSelectedChapterMutableLiveData) {
            bookInRepo.value?.chapters?.run {
                val averageDrillLevel = this.map { it.drillCount.get() }.average().toInt()
                avgDrillCount.set(averageDrillLevel)

                if (it in 0..this.size) {
                    get(it)
                } else {
                    get(0)
                }.apply {
                    status.set(true)
                }
            }
        }

    val currentShuffledSnippetListLiveData =
        Transformations.map(currentChapterLiveData) {
            it?.snippetList?.toMutableList()?.map { snippet ->
                snippet.apply {
                    statusColor.set(Snippet.ITEM_INTACT)
                }
            }?.shuffled().apply {

            }
        }


    val avgAmplitudeLiveDate = MutableLiveData<Pair<Int, Int>>()

    val samplingAmplitudeObserver = AmplitudeObserver(avgAmplitudeLiveDate)

    class AmplitudeObserver(val avgAmplitudeLiveDate: MutableLiveData<Pair<Int, Int>>) :
        Observer<Amplitude> {

        val latestSampleList = LinkedList<Int>()
        override fun onChanged(t: Amplitude?) {
            t?.let {
                latestSampleList.add(t.level)
                if (latestSampleList.size > SAMPLE_AVG_COUNT) {
                    latestSampleList.remove()
                }
                val avgValue = latestSampleList.average()
                avgAmplitudeLiveDate.postValue(Pair(t.level, avgValue.toInt()))
            }
        }

        fun isPerformanceDone() =
            latestSampleList.count { amplitude -> amplitude < AMPLITUDE_THRESHOLD } >= SAMPLE_AVG_COUNT


        fun reset() {
            latestSampleList.clear()
        }
    }

    val currentSnippetLiveData =
        Transformations.switchMap(currentShuffledSnippetListLiveData) { value ->
            var uploadJob: Job? = null
            liveData {

                value?.let {

                    currentChapterLiveData.value?.let { chapter ->
                        AnalyticsUtil.logABRSMAdapterDrillStart(
                            bookInRepo.value?.name ?: "",
                            chapter.name,
                            chapter.drillCount.get()
                        )
                        // wait for recycler view initialization complete
                        onChapterDrillStart(
                            bookInRepo.value?.name ?: "", chapter
                        )

                        delay(2048)

                        samplingHeartBeatMutalbeLiveDate.value = Amplitude.SIG_START_ONE_RECORD

                        val performanceDuration =
                            measureTimeMillis {
                                it.forEachIndexed { index, snippet ->
                                    snippet.apply {
                                        statusColor.set(Snippet.ITEM_CURRENT)
                                    }
                                    emit(Pair("Please Perform.. ${snippet.name}", index))
                                    waitForPerformanceCompleteSuspend()
                                    snippet.apply {
                                        statusColor.set(Snippet.ITEM_PASSED)
                                    }
                                }
                            }


                        samplingHeartBeatMutalbeLiveDate.value = Amplitude.SIG_STOP_ONE_RECORD


                        val unFormatedFileName =
                            SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(System.currentTimeMillis()))
                        val firebaseUploadingFileName =
                            pianoRepository.getFireBaseValidName(unFormatedFileName)
                        chapter.addNewRecordFile(
                            firebaseUploadingFileName,
                            pianoRepository.pianoCloudFileMaxCount
                        )

                        uploadJob?.cancel()
                        uploadJob = viewModelScope.launch {
                            // 1. rename record file
                            pianoRepository.moveRecordFileToUploadFile(
                                getRecordOutputFileName(),
                                getUploadingFileName()
                            )
                            // 2. upload record file
                            pianoRepository.uploadRecordFile(
                                bookInRepo.value?.name ?: "",
                                chapter,
                                getUploadingFileName(),
                                firebaseUploadingFileName
                            )
                        }


                        emit(Pair("Chapter finished. ${AFTERWORD.name}", -1))

                        onChapterDrillComplete(
                            bookInRepo.value?.name ?: "",
                            chapter,
                            performanceDuration shr 10
                        )
                    }
                }
            }
        }


    private val _drillCompleteAwardLiveDate = MutableLiveData<Pair<Chapter, String>>()
    val drillCompleteAwardLiveDate: LiveData<Pair<Chapter, String>> = _drillCompleteAwardLiveDate

    private suspend fun waitForPerformanceCompleteSuspend() {
        samplingAmplitudeLiveDate.observeForever(samplingAmplitudeObserver.apply { reset() })
        while (true) {
            samplingHeartBeatMutalbeLiveDate.postValue(Amplitude.SIG_SAMPLING)
            delay(SAMPLE_INTERVAL)
            if (samplingAmplitudeObserver.isPerformanceDone()) {
                samplingAmplitudeLiveDate.removeObserver(samplingAmplitudeObserver)
                break
            }
        }
    }


    private val samplingHeartBeatMutalbeLiveDate = MutableLiveData<Int>()
    val samplingHeartBeatLiveDate: LiveData<Int> = samplingHeartBeatMutalbeLiveDate.map { it }

    val samplingAmplitudeLiveDate = MutableLiveData<Amplitude>()


    fun onChapterClicked(index: Int) {
        userSelectedChapterMutableLiveData.postValue(index)
    }


    fun onChapterLongClicked(index: Int) {
        if (bookInRepo.value != null) {
            val chapter = bookInRepo.value!!.chapters[index]
            pianoRepository.refreshChapterRecordFileListAsync(
                chapter,
                bookInRepo.value!!.name,
                chapter.name
            )
            _popupRecordListMutableLiveData.postValue(Pair(true, chapter))
        }
    }


    override fun onCleared() {
        samplingAmplitudeLiveDate.removeObserver(samplingAmplitudeObserver)
        samplingHeartBeatMutalbeLiveDate.postValue(Amplitude.SIG_CLEAR)
        super.onCleared()
    }

    private fun onChapterDrillStart(
        bookName: String,
        chapter: Chapter
    ) {
        inPerforming.set(true)
    }

    private fun onChapterDrillComplete(
        bookName: String,
        chapter: Chapter,
        chapterPerformanceDurationInSeconds: Long
    ) {
        // only record performance longer than possible duration
        chapter.status.set(false)
        // only record performance for reasonable duration
        if (chapterPerformanceDurationInSeconds > (chapter.snippetList?.size
                ?: Int.MAX_VALUE) * SINGLE_SNIPPET_DURATION_IN_SECONDS
        ) {
            chapter.apply {
                val original = drillCount.get()
                drillCount.set(original + 1)
            }
            _drillCompleteAwardLiveDate.postValue(
                Pair(
                    chapter,
                    pianoRepository.awardGifUrlList.random()
                )
            )
            pianoRepository.updateFireStore(bookName, chapter)

            AnalyticsUtil.logABRSMAdapterDrillComplete(
                bookInRepo.value?.name ?: "",
                chapter.name,
                chapter.drillCount.get(),
                chapterPerformanceDurationInSeconds.toInt()
            )
        } else {


            AnalyticsUtil.logABRSMAdapterDrillFailed(
                bookInRepo.value?.name ?: "",
                chapter.name,
                chapter.drillCount.get(),
                chapterPerformanceDurationInSeconds.toInt()
            )
        }
        inPerforming.set(false)
    }


    private val _statusMutableLiveData = MutableLiveData<GenericDataWithStatus>()
    val statusLiveData: LiveData<GenericDataWithStatus> = _statusMutableLiveData

    private val _recordUriLivaData = MutableLiveData<Pair<Chapter, Uri>>()
    val recordUriLivaData: LiveData<Pair<Chapter, Uri>> = _recordUriLivaData
    fun onCloudRecordFileClicked(chapter: Chapter, index: Int) {
        if (inPerforming.get()) {
            _statusMutableLiveData.postValue(GenericDataWithStatus(UIStatus.STATUS_PERFORMING, ""))
        } else {
            pianoRepository.getChapterRecordFileUrl(
                bookInRepo.value!!.name,
                chapter,
                index,
                fun(uri: Uri) { _recordUriLivaData.postValue(Pair(chapter, uri)) })
            _statusMutableLiveData.postValue(GenericDataWithStatus(UIStatus.STATUS_IDLE, chapter))
        }
    }
}
