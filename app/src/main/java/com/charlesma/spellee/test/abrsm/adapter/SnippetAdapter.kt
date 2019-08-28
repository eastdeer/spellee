package com.charlesma.spellee.test.abrsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charlesma.spellee.R
import com.charlesma.spellee.databinding.RecyclerViewItemPianoSnippetBinding
import com.charlesma.spellee.test.abrsm.datamodel.Snippet

class SnippetAdapter : ListAdapter<Snippet, SnippetAdapter.SnippetViewHolder>(DIFF_CALLBACK) {

    class SnippetViewHolder(val binding: RecyclerViewItemPianoSnippetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(snippet: Snippet) {
            binding.snippet = snippet
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnippetViewHolder {
        val dataBinding =
            DataBindingUtil.inflate<RecyclerViewItemPianoSnippetBinding>(
                LayoutInflater.from(parent.context)
                , R.layout.recycler_view_item_piano_snippet, parent, false
            )
        return SnippetViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: SnippetViewHolder, position: Int) {
        holder.bind(this.getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : ItemCallback<Snippet>() {
            override fun areItemsTheSame(oldItem: Snippet, newItem: Snippet) =
                oldItem.name.equals(newItem.name)

            override fun areContentsTheSame(oldItem: Snippet, newItem: Snippet) =
                areItemsTheSame(oldItem, newItem)
        }
    }
}