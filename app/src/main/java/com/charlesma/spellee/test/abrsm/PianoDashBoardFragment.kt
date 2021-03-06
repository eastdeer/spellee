package com.charlesma.spellee.test.abrsm

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.charlesma.spellee.R
import com.charlesma.spellee.test.abrsm.adapter.ChapterAdapter
import com.charlesma.spellee.test.abrsm.adapter.CloudRecordFileAdapter
import com.charlesma.spellee.test.abrsm.adapter.CloudRecordFileAdapter2
import com.charlesma.spellee.test.abrsm.adapter.SnippetAdapter
import com.charlesma.spellee.test.abrsm.datamodel.Chapter
import com.charlesma.spellee.test.abrsm.datamodel.CloudRecordFile
import com.charlesma.spellee.test.abrsm.sound.SoundDetector
import com.charlesma.spellee.test.abrsm.viewmodel.PianoAbrsmViewModel
import com.charlesma.spellee.viewmodel.MainActivityViewModel
import com.charlesma.spellee.widget.popup.ExoPlayerFragmentDirections
import kotlinx.android.synthetic.main.fragment_piano_dash_board.*
import kotlinx.android.synthetic.main.popup_window_could_file_list.*
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PianoDashBoardFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PianoDashBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PianoDashBoardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    var listPopupWindow: ListPopupWindow? = null
    var popupWindow: PopupWindow? = null

    private lateinit var soundDetector: SoundDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_piano_dash_board, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PianoDashBoardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PianoDashBoardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewmodel = ViewModelProviders.of(this)[PianoAbrsmViewModel::class.java]
//        chapter1.setOnClickListener { viewmodel.onChapterClicked(1) }
//        chapter2.setOnClickListener { viewmodel.onChapterClicked(2) }

        val snippetAdapter = SnippetAdapter()
        recycler_view.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = snippetAdapter

//        chapter_recyclerview.layoutManager = GridLayoutManager(context,3,GridLayoutManager.VERTICAL,false)
        chapter_recyclerview.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
//        chapter_recyclerview.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
        val chapterAdapter = ChapterAdapter(viewmodel, chapter_recyclerview)
        chapter_recyclerview.adapter = chapterAdapter
        chapter_recyclerview.itemAnimator = DefaultItemAnimator()


        viewmodel.currentChapterLiveData.observe(this, Observer {})

        viewmodel.currentShuffledSnippetListLiveData.observe(
            this,
            Observer { value ->
                snippetAdapter.submitList(value)
            })

        val activityViewModel = ViewModelProviders.of(activity!!)[MainActivityViewModel::class.java]
        activityViewModel.textToSpeechReadyLiveData.observe(this, Observer {
            when (it) {
                true -> enableChapters()
                else -> disableChapters()
            }
        })

        viewmodel.currentSnippetLiveData.observe(this, Observer {
            // read the name aloud
            activityViewModel.textToSpeechLiveData.postValue("${it.first}")
            if (it.second >= 0) {
                recycler_view.smoothScrollToPosition(it.second)

                if (it.second > 0) {
                    recycler_view.findViewHolderForAdapterPosition(it.second - 1)
                        ?.let { viewHolder ->
                            (viewHolder as SnippetAdapter.SnippetViewHolder).binding.musicNote.apply {
                                rotationY = 0f
                            }
                        }
                }
                recycler_view.findViewHolderForAdapterPosition(it.second)?.let { viewHolder ->
                    (viewHolder as SnippetAdapter.SnippetViewHolder).binding.musicNote.apply {
                        rotationY = 0f
                        animate().rotationY(3600f).setDuration(10000).start()
                    }
                }
            }
        })

        viewmodel.bookInRepo.observe(this, Observer {
            chapterAdapter.submitList(it.chapters.toList())
            chapter_recyclerview.postInvalidate()
        })

        val outputFileName =
            viewmodel.getRecordOutputFileName()

        soundDetector = SoundDetector(
            lifecycle,
            viewmodel.samplingHeartBeatLiveDate,
            viewmodel.samplingAmplitudeLiveDate,
            outputFileName
        )


        viewmodel.avgAmplitudeLiveDate.observe(this, Observer { value ->
            progress_horizontal.progress = value.first
            progress_horizontal.secondaryProgress = value.second
        })


        viewmodel.drillCompleteAwardLiveDate.observe(this, Observer { (chapter, awardGifUrl) ->
            context?.let {
                val chapterPosition = chapterAdapter.currentList?.indexOf(chapter)

                val currentChild =
                    chapter_recyclerview.findViewHolderForAdapterPosition(chapterPosition)
                val drillCountTv: TextView? =
                    currentChild?.itemView?.findViewById(R.id.practiced_count)


                if (drillCountTv != null) {
                    // text
                    ObjectAnimator.ofFloat(drillCountTv, "alpha", 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f)
                        .setDuration(6148).start()

                    // Gif
                    Glide.with(it).asGif()
                        .load(awardGifUrl)
                        .circleCrop()
                        .into(
                            GifRewardImageTarget(
                                drill_award_imageview,
                                Pair(
                                    drillCountTv!!.x + currentChild!!.itemView!!.x,
                                    drillCountTv!!.y + currentChild!!.itemView!!.y
                                )
                            )
                        )
                }
            }

        })


        viewmodel.popupRecordListLiveData.observe(this, Observer {

            if (it.first) {
                if (listPopupWindow == null) {
                    val adapter = CloudRecordFileAdapter(it.second)
                    val newPopupWindow = ListPopupWindow(context)
                    newPopupWindow.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW

                    val indexInAdapter = chapterAdapter.currentList.indexOf(it.second)
                    val clickedChapterView =
                        chapter_recyclerview.findViewHolderForAdapterPosition(indexInAdapter)

                    newPopupWindow.anchorView = clickedChapterView?.itemView
                    newPopupWindow.setAdapter(adapter)
                    newPopupWindow.setOnItemClickListener { parent, view, position, id ->
                        viewmodel.onCloudRecordFileClicked(it.second, position)
                        newPopupWindow.dismiss()
                    }
                    listPopupWindow = newPopupWindow
                }
                listPopupWindow?.let { listPopupWindow ->
                    //                    listPopupWindow.show()
                }

                if (popupWindow == null) {
                    val popupWindowContent = LayoutInflater.from(context)
                        .inflate(R.layout.popup_window_could_file_list, view as ViewGroup, false)
                    val recordFileRecyclerview: RecyclerView =
                        popupWindowContent.findViewById(R.id.recordFileRecyclerview)
                    recordFileRecyclerview.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    popupWindow = PopupWindow(
                        popupWindowContent,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                    )
                }

                val indexInAdapter = chapterAdapter.currentList.indexOf(it.second)
                val clickedChapterView =
                    chapter_recyclerview.findViewHolderForAdapterPosition(indexInAdapter)

                popupWindow?.let { popupWindow ->
                    // popupWindow.animationStyle
                    popupWindow.elevation =
                        resources.getDimension(R.dimen.conventional_margin_normal)
                    popupWindow.contentView.findViewById<RecyclerView>(R.id.recordFileRecyclerview)
                        .apply {
                            adapter = CloudRecordFileAdapter2(it.second).apply {
                                itemClickListener = fun(
                                    chapter: Chapter,
                                    cloudFile: CloudRecordFile,
                                    index: Int
                                ) {
                                    viewmodel.onCloudRecordFileClicked(chapter, index)
                                }
                            }
                        }

                    popupWindow.update()
                    popupWindow.elevation =
                        resources.getDimension(R.dimen.conventional_margin_normal)
                    popupWindow.showAsDropDown(clickedChapterView?.itemView)

                }


            } else {
                listPopupWindow?.let { listPopupWindow ->
                    listPopupWindow.dismiss()
                }
            }
        })

        viewmodel.recordUriLivaData.observe(this, Observer {
            openExoPlayerWithMediaUri(it.first, it.second)
        })

    }

    private fun enableChapters() {
        chapter_recyclerview.isEnabled = true
    }

    private fun disableChapters() {
        chapter_recyclerview.isEnabled = false
    }

    private fun openExoPlayerWithMediaUri(chapter: Chapter, uri: Uri) {
        val action =
            PianoDashBoardFragmentDirections.actionPianoDashBoardFragmentToExoPlayerFragment2(
                chapter.name,
                uri,
                "Recorded File"
            )
        findNavController().navigate(action)
    }

    class GifRewardImageTarget(imageView: ImageView, val coordinate: Pair<Float, Float>) :
        CustomViewTarget<ImageView, GifDrawable>(imageView) {
        override fun onLoadFailed(errorDrawable: Drawable?) {

        }

        override fun onResourceCleared(placeholder: Drawable?) {

        }

        override fun onResourceReady(
            resource: GifDrawable,
            transition: Transition<in GifDrawable>?
        ) {
            this.view.apply {
                visibility = View.VISIBLE
                setImageDrawable(resource)
            }

            resource.setLoopCount(1)
            resource.registerAnimationCallback(object :
                Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    super.onAnimationEnd(drawable)
                    // animate award_imageview to drill counter
                    val originalX = view.x
                    val originalY = view.y

                    view.animate().x(coordinate.first).y(coordinate.second).scaleX(0.1f)
                        .scaleY(0.1f).setDuration(512)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                restoreViewLayout()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                restoreViewLayout()
                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }

                            private fun restoreViewLayout() {
                                view.scaleX = 1.0f
                                view.scaleY = 1.0f
                                view.x = originalX
                                view.y = originalY
                                view.visibility = View.GONE
                            }
                        })
                }

                override fun onAnimationStart(drawable: Drawable?) {
                    super.onAnimationStart(drawable)

                }
            }
            )

            resource.start()
        }
    }

}
