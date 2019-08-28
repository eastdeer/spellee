package com.charlesma.spellee.test.abrsm.sound

import android.media.MediaRecorder
import androidx.lifecycle.*
import com.charlesma.spellee.test.abrsm.datamodel.Amplitude


class SoundDetector(
    lifecycle: Lifecycle,
    samplingHeartBeatLiveDate: LiveData<Int>,
    samplingAmplitudeLiveDate: MutableLiveData<Amplitude>
) {

    private var mRecorder: MediaRecorder? = null

    init {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> start()
                    Lifecycle.Event.ON_PAUSE -> stop()
                    Lifecycle.Event.ON_DESTROY -> lifecycle.removeObserver(this)
                }
            }
        })

        samplingHeartBeatLiveDate.observeForever(object : Observer<Int> {
            override fun onChanged(t: Int?) {
                when (t) {
                    Amplitude.SIG_STOP -> samplingHeartBeatLiveDate.removeObserver(this)
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
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            setOnInfoListener(object : MediaRecorder.OnInfoListener {
                override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
                }
            })
            prepare()
            start()
        }
    }

    fun stop() {
        mRecorder?.let {
            it.stop()
            it.release()
        }
        mRecorder = null
    }

    fun getAmplitude(): Double {
        return mRecorder?.maxAmplitude?.toDouble() ?: 0.0
    }
}