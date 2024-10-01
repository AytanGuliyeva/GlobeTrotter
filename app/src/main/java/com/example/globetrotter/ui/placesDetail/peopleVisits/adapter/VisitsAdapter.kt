package com.example.globetrotter.ui.placesDetail.peopleVisits.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.ItemPeopleVisitsBinding

class VisitsAdapter() : RecyclerView.Adapter<VisitsAdapter.VisitsViewHolder>() {
    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }
    }

    val diffUtil = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(user: List<Users>) {
        diffUtil.submitList(user)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitsViewHolder {
        val binding =
            ItemPeopleVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: VisitsViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class VisitsViewHolder(private val binding: ItemPeopleVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Users) {

            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(binding.imgProfile)
            binding.txtUsername.text = item.username
        }
    }
}