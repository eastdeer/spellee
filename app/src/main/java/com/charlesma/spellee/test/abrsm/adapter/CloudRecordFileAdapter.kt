package com.charlesma.spellee.test.abrsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.charlesma.spellee.R
import com.charlesma.spellee.databinding.ListViewItemPianoRecordFileBinding
import com.charlesma.spellee.test.abrsm.datamodel.Chapter

class CloudRecordFileAdapter(val chapter: Chapter): BaseAdapter() {
// TODO: add empty adapter handling
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        DataBindingUtil
        val binding = DataBindingUtil.inflate<ListViewItemPianoRecordFileBinding>(LayoutInflater.from(parent?.context),R.layout.list_view_item_piano_record_file,parent,false)
        binding.fileName = chapter.recordFileList[position].fileName
        binding.cloudStatus = chapter.recordFileList[position].observableStatus
        return binding.root
    }

    override fun getItem(position: Int): Any =
        chapter.recordFileList[position]


    override fun getItemId(position: Int): Long =
        position.toLong()


    override fun getCount() = chapter.recordFileList.size

}