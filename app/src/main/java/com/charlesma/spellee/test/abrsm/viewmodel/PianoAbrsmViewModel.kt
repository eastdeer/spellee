package com.charlesma.spellee.test.abrsm.viewmodel

import android.app.Application
import androidx.databinding.ObservableInt
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.charlesma.spellee.test.abrsm.datamodel.Amplitude
import com.charlesma.spellee.test.abrsm.datamodel.Chapter
import com.charlesma.spellee.test.abrsm.datamodel.Snippet
import com.charlesma.spellee.test.abrsm.repository.PianoRepository
import kotlinx.coroutines.delay
import java.util.*
import kotlin.system.measureTimeMillis

class PianoAbrsmViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SAMPLE_INTERVAL = 128L
        const val SAMPLE_AVG_COUNT = 32
        const val AMPLITUDE_THRESHOLD = 8800
        const val SINGLE_SNIPPET_DURATION = 15 * 1000
    }

    val pianoRepository by lazy { PianoRepository(application) }
    val AFTERWORD = Snippet().apply { name = pianoRepository.pianoPerformanceAfterWords }

//    val userSelectedChapterMutableLiveData = liveData {  }

    val bookInRepo = liveData {

        emit(pianoRepository.abrsmBook)
//        onChapterClicked(0)
    }

    val avgDrillCount = ObservableInt(0)

    val userSelectedChapterMutableLiveData = MutableLiveData<Int>()
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

            liveData {
                value?.let {

                    val performanceDuration =
                        measureTimeMillis {
                            it.forEachIndexed { index, snippet ->
                                snippet.apply {
                                    statusColor.set(Snippet.ITEM_CURRENT)
                                }
                                emit(Pair("Please Perform.. ${snippet.name}",index))
                                waitForPerformanceCompleteSuspend()
                                snippet.apply {
                                    statusColor.set(Snippet.ITEM_PASSED)
                                }
                            }
                        }
                    emit(Pair("Chapter finished. ${AFTERWORD.name}",-1))

                    currentChapterLiveData.value?.let { chapter ->
                        onChapterDrillComplete(
                            chapter,
                            performanceDuration
                        )
                    }
                }
            }
        }


    private val _drillCompleteAwardLiveDate = MutableLiveData<Pair<Chapter,String>>()
    val drillCompleteAwardLiveDate:LiveData<Pair<Chapter,String>> = _drillCompleteAwardLiveDate

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


    override fun onCleared() {
        samplingAmplitudeLiveDate.removeObserver(samplingAmplitudeObserver)
        samplingHeartBeatMutalbeLiveDate.postValue(Amplitude.SIG_STOP)
        super.onCleared()
    }

    private fun onChapterDrillComplete(chapter: Chapter, chapterPerformanceDuration: Long) {
        // only record performance longer than possible duration
        chapter.status.set(false)
        // only record performance for reasonable duration
//        if (chapterPerformanceDuration > (chapter.snippetList?.size
//                ?: Int.MAX_VALUE) * SINGLE_SNIPPET_DURATION
//        )
//        {
            chapter.apply {
                val original = drillCount.get()
                drillCount.set(original + 1)
            }
            _drillCompleteAwardLiveDate.postValue(Pair(chapter,pianoRepository.awardGifUrlList.random()))
            pianoRepository.updateDrillCount(chapter)
//        }
    }

}