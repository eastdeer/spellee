package com.charlesma.spellee.test.abrsm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charlesma.spellee.R
import com.charlesma.spellee.databinding.RecyclerViewItemAbrsmChapterBinding
import com.charlesma.spellee.test.abrsm.datamodel.Chapter
import com.charlesma.spellee.test.abrsm.viewmodel.PianoAbrsmViewModel

class ChapterAdapter(val viewModel: PianoAbrsmViewModel) : ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Chapter>() {
            override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter) =
                oldItem.name.equals(newItem.name)

            override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter) =
                areItemsTheSame(oldItem, newItem)
        }
    }


    class ChapterViewHolder(private val databinding: RecyclerViewItemAbrsmChapterBinding,val viewModel: PianoAbrsmViewModel) :
        RecyclerView.ViewHolder(databinding.root) {
        fun bind(chapter: Chapter,position:Int) {
            databinding.chapter = chapter
            databinding.averageDrillLevel = viewModel.avgDrillCount
            databinding.root.setOnClickListener { viewModel.onChapterClicked(position) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val databinding = DataBindingUtil.inflate<RecyclerViewItemAbrsmChapterBinding>(
            LayoutInflater.from(parent.context),
            R.layout.recycler_view_item_abrsm_chapter,
            parent,
            false
        )
        return ChapterViewHolder(databinding,viewModel)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position),position)
    }

}