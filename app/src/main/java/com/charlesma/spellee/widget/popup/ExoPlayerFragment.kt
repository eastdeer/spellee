package com.charlesma.spellee.widget.popup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.charlesma.spellee.R
import com.charlesma.spellee.databinding.FragmentExoplayerDialogBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_exoplayer_dialog.*


class ExoPlayerFragment : DialogFragment() {

    val args: ExoPlayerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentExoplayerDialogBinding>(
            inflater,
            R.layout.fragment_exoplayer_dialog,
            container,
            false
        )
        dataBinding.chapterName = args.chapterName
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(false)
        isCancelable = false

        var player = ExoPlayerFactory.newSimpleInstance(context)
        palyerView.player = player

        close_btn.setOnClickListener { _ ->
            palyerView?.player?.stop()
            palyerView?.player?.release()
            findNavController().popBackStack()
        }

        preparePlayer(player, args.cloudFileUri, args.cloudFileName)
    }

    private fun preparePlayer(player: ExoPlayer, mediaUri: Uri, cloudFileName:String?) {
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "file:2019-0903-xxxx")
        )
        // This is the MediaSource representing the media to be played.
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaUri)
        // Prepare the player with the source.
        player.prepare(videoSource)
    }

    override fun onResume() {
        super.onResume()
        palyerView.player?.playWhenReady = true
    }

    override fun onPause() {
        palyerView.player?.playWhenReady = false
        super.onPause()
    }

}