package com.example.globetrotter.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.SearchPostItemBinding

class PlacesAdapter(
   // private var itemClick: (item: Places) -> Unit
) :
    RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Places>() {
        override fun areItemsTheSame(oldItem: Places, newItem: Places): Boolean {
            return oldItem.placesId == newItem.placesId
        }

        override fun areContentsTheSame(oldItem: Places, newItem: Places): Boolean {
            return oldItem == newItem
        }
    }
    private val diffUtil = AsyncListDiffer(this, diffUtilCallBack)
    fun submitList(places: List<Places>) {
        diffUtil.submitList(places)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding =
            SearchPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class PlacesViewHolder(private val binding: SearchPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Places) {
            val imageAdapter = PlacesImageAdapter(item.placeImageUrls)
            binding.viewPager.adapter = imageAdapter

//            itemView.setOnClickListener {
//                itemClick(item)
//            }
        }

    }
}