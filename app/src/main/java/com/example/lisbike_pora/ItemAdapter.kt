package com.example.lisbike_pora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lisbike_pora.databinding.EachItemBinding


class ItemAdapter(private var mList: List<DataModel>, private val navController: NavController):
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), NestedAdapter.MyOnClick {
    private var list: List<String> = listOf()

    class ItemViewHolder(val binding: EachItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val linearLayout = binding.linearLayout
        val expandableLayout = binding.expandableLayout
        val mTextView = binding.itemTv
        val mArrowImage = binding.arroImageview
        val nestedRecyclerView = binding.childRv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
        val listItem = EachItemBinding.inflate(view,parent,false)
        return ItemViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val model: DataModel = mList[position]
        holder.mTextView.setText(model.getItemText())

        val isExpandable = model.isExpandable()
        holder.expandableLayout.setVisibility(if (isExpandable) View.VISIBLE else View.GONE)

        if (isExpandable){
            holder.mArrowImage.setImageResource(R.drawable.arrow_up);
        }else{
            holder.mArrowImage.setImageResource(R.drawable.arrow_down);
        }

        val adapter = NestedAdapter(list, this)
        holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.nestedRecyclerView.setHasFixedSize(true)
        holder.nestedRecyclerView.adapter = adapter

        holder.linearLayout.setOnClickListener {
            model.setExpandable(!model.isExpandable())
            list = model.getNestedList()!!
            notifyItemChanged(holder.adapterPosition)
        }
    }
    override fun onClick(position: Int) {
        // Handle the click event and open the appropriate fragment
        val nestedItem = list[position]

        // Example: Open a fragment based on the nested item clicked
        when (nestedItem) {
            "Input" -> {
                // Open ImageInputFragment
                navController.navigate(R.id.action_homeFragment_to_imageInputFragment)
            }
            "State" -> {
                navController.navigate(R.id.action_homeFragment_to_accelerometerStateFragment)
            }
            "Simulation" -> {
                navController.navigate(R.id.action_homeFragment_to_imageSimulationFragment)
            }
            "Simulation " -> {
                navController.navigate(R.id.action_homeFragment_to_accelerometerSimulationFragment)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }
}