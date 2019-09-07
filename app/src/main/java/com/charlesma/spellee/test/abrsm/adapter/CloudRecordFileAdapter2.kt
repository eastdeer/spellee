package com.charlesma.spellee.test.abrsm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.charlesma.spellee.R
import com.charlesma.spellee.databinding.ListViewItemPianoRecordFileBinding
import com.charlesma.spellee.test.abrsm.datamodel.Chapter
import com.charlesma.spellee.test.abrsm.datamodel.CloudRecordFile

class CloudRecordFileAdapter2(var chapter: Chapter) :
    RecyclerView.Adapter<CloudRecordFileAdapter2.CloudRecordViewHolder>() {
    // TODO: add empty adapter handling
    var itemClickListener: ((chapter: Chapter, cloudFile: CloudRecordFile, index: Int) -> Unit)? = null
    private val _itemClickListener = fun(chapter: Chapter, cloudFile: CloudRecordFile, index: Int) {
        itemClickListener?.invoke(
            chapter,
            cloudFile,
            index
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloudRecordViewHolder {
        val binding = DataBindingUtil.inflate<ListViewItemPianoRecordFileBinding>(
            LayoutInflater.from(parent.context),
            R.layout.list_view_item_piano_record_file,
            parent,
            false
        )
        return CloudRecordViewHolder(binding)
    }

    override fun getItemCount() = chapter.recordFileList.size

    override fun onBindViewHolder(holder: CloudRecordViewHolder, position: Int) {
        holder.bind(chapter, position)

    }


    inner class CloudRecordViewHolder(val dataBinding: ListViewItemPianoRecordFileBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {

        fun bind(chapter: Chapter, index: Int) {
            dataBinding.fileName = chapter.recordFileList[index].fileName
            dataBinding.cloudStatus = chapter.recordFileList[index].observableStatus
            dataBinding.root.setOnClickListener {
                _itemClickListener.invoke(
                    chapter,
                    chapter.recordFileList[index],
                    index
                )
            }
        }
    }
}