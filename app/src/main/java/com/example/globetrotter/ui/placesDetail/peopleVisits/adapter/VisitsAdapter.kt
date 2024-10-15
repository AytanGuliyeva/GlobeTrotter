package com.example.globetrotter.ui.placesDetail.peopleVisits.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.ItemPeopleVisitsBinding
import com.example.globetrotter.ui.placesDetail.peopleVisits.PeopleVisitsFragmentDirections
import com.example.globetrotter.ui.placesDetail.peopleVisits.overView.OverViewFragmentDirections

class VisitsAdapter(private var itemClick: (item: Users) -> Unit) :
    RecyclerView.Adapter<VisitsAdapter.VisitsViewHolder>() {
    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Pair<Users, Boolean>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Users, Boolean>,
            newItem: Pair<Users, Boolean>
        ): Boolean {
            return oldItem.first.userId == newItem.first.userId
        }

        override fun areContentsTheSame(
            oldItem: Pair<Users, Boolean>,
            newItem: Pair<Users, Boolean>
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val diffUtil = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(userDetails: List<Pair<Users, Boolean>>) {
        Log.d("VisitsAdapter", "Submitting list with ${userDetails.size} items.")
        userDetails.forEach {
            Log.d("VisitsAdapter", "User: ${it.first.username}, Has Story: ${it.second}")
        }
        diffUtil.submitList(userDetails)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitsViewHolder {
        val binding =
            ItemPeopleVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitsViewHolder(binding)
    }

    override fun getItemCount(): Int = diffUtil.currentList.size

    override fun onBindViewHolder(holder: VisitsViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class VisitsViewHolder(private val binding: ItemPeopleVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<Users, Boolean>) {
            val user = item.first
            Glide.with(binding.root)
                .load(user.imageUrl)
                .into(binding.imgProfile)
            binding.txtUsername.text = user.username
            val hasStory = item.second
            binding.imgOverview.visibility = if (hasStory) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                itemClick(user)
            }
        }
    }
}
