package com.example.globetrotter.ui.discoverActivities.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.ItemCategoryPlacesBinding
import com.example.globetrotter.ui.search.adapter.PlacesImageAdapter

class TopPlacesAdapter(
    var itemClick: (item: Places)->Unit,
    private val categoryPlaces:MutableList<Places>,

) : RecyclerView.Adapter<TopPlacesAdapter.TopPlacesViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopPlacesViewHolder {
        val binding =
            ItemCategoryPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopPlacesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TopPlacesViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    inner class TopPlacesViewHolder(private val binding: ItemCategoryPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Places) {


            binding.placeName.text=item.place
            val imageAdapter = PlacesImageAdapter(item.placeImageUrls) {
                itemClick(item)
            }
            binding.viewPager.adapter = imageAdapter

            val shortenedText = if (item.place.length > 8) {
                item.place.substring(0, 8) + "..."
            } else {
                item.place
            }
            binding.placeName.text = shortenedText

            val layoutParams = binding.root.layoutParams
            binding.root.layoutParams = layoutParams

            itemView.setOnClickListener {
                Log.d("PlacesAdapter", "Item clicked: ${item.placesId}")
                itemClick(item)
            }}
    }

}