package com.charlesma.spellee.test.abrsm.sound

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.*
import com.charlesma.spellee.test.abrsm.datamodel.Amplitude


class SoundDetector(
    lifecycle: Lifecycle,
    samplingHeartBeatLiveDate: LiveData<Int>,
    samplingAmplitudeLiveDate: MutableLiveData<Amplitude>,
    val outputFileName: String
) {

    private var mRecorder: MediaRecorder? = null

    init {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> resume()
                    Lifecycle.Event.ON_PAUSE -> pause()
                    Lifecycle.Event.ON_DESTROY -> {
                        clear()
                        lifecycle.removeObserver(this)
                    }
                }
            }
        })

        samplingHeartBeatLiveDate.observeForever(object : Observer<Int> {
            override fun onChanged(t: Int?) {
                when (t) {
                    Amplitude.SIG_CLEAR -> {
                        samplingHeartBeatLiveDate.removeObserver(this)
                    }
                    Amplitude.SIG_START_ONE_RECORD -> {
                        start()
                    }
                    Amplitude.SIG_STOP_ONE_RECORD -> {
                        stop()
                    }
                    else -> mRecorder?.run {
                        samplingAmplitudeLiveDate.postValue(Amplitude(maxAmplitude))
                    }
                }
            }
        }
        )
    }

    fun start() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
        } else {
            mRecorder?.reset()
        }

        mRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setOutputFile(outputFileName)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setOnInfoListener(object : MediaRecorder.OnInfoListener {
                override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
                }
            })
            prepare()
            start()
        }
    }

    fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mRecorder?.resume()
        }
    }

    fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mRecorder?.pause()
        }
    }

    fun stop() {
        mRecorder?.let {
            it.stop()
        }
    }

    fun clear(){
        mRecorder?.let {
            it.release()
        }
        mRecorder = null
    }

    fun getAmplitude(): Double {
        return mRecorder?.maxAmplitude?.toDouble() ?: 0.0
    }
}