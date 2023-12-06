package com.example.lisbike_pora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lisbike_pora.databinding.NestedItemBinding

class NestedAdapter(var mList:List<String>, private val onClickObject: MyOnClick): RecyclerView.Adapter<NestedAdapter.NestedViewHolder>() {

    class NestedViewHolder(val binding: NestedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val  mTv = binding.nestedItemTv
    }

    interface MyOnClick {
        fun onClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedViewHolder {
        val view = LayoutInflater.from(parent.context)
        val listItem = NestedItemBinding.inflate(view,parent,false)
        return NestedAdapter.NestedViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: NestedViewHolder, position: Int) {
        holder.mTv.setText(mList.get(position))

        holder.itemView.setOnClickListener {
            onClickObject.onClick(position)
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }
}