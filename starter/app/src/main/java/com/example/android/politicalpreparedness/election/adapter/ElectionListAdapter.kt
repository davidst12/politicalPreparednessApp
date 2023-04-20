package com.example.android.politicalpreparedness.election.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.android.politicalpreparedness.databinding.ElectionItemBinding
//import com.example.android.politicalpreparedness.databinding.ViewholderElectionBinding
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.android.synthetic.main.election_item.view.*

class ElectionListAdapter(
    private val clickListener: ElectionListener
): ListAdapter<Election, ElectionListAdapter.ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ElectionViewHolder private constructor(private val binding: ElectionItemBinding): ViewHolder(binding.root){
        fun bind(item: Election, clickListener: ElectionListener){
            binding.electionItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup): ElectionViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElectionItemBinding.inflate(layoutInflater, parent, false)
                return ElectionViewHolder(binding)
            }
        }
    }

}

class ElectionDiffCallback: DiffUtil.ItemCallback<Election>(){

    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return  oldItem == newItem
    }
}

class ElectionListener(val clickListener: (election: Election) -> Unit){
    fun onClick(election: Election){
        clickListener(election)
    }
}