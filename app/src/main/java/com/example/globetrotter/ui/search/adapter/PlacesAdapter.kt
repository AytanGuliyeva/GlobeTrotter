package com.example.globetrotter.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.SearchPostItemBinding

class PlacesAdapter : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Places>() {
        override fun areItemsTheSame(oldItem: Places, newItem: Places): Boolean {
            return oldItem.placesId == newItem.placesId
        }

        override fun areContentsTheSame(oldItem: Places, newItem: Places): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(places: List<Places>) {
        differ.submitList(places)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding =
            SearchPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    inner class PlacesViewHolder(private val binding: SearchPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Places) {
            val imageAdapter = PlacesImageAdapter(item.placeImageUrls)
            binding.viewPager.adapter = imageAdapter
            val shortenedText = if (item.place.length > 8) {
                item.place.substring(0, 8) + "..."
            } else {
                item.place
            }
            binding.placeName.text = shortenedText

            val layoutParams = binding.root.layoutParams
            layoutParams.height = if (adapterPosition % 2 == 0) 400 else 200
            binding.root.layoutParams = layoutParams
        }
    }
}
