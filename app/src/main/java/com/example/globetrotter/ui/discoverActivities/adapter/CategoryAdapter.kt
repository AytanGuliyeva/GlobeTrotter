package com.example.globetrotter.ui.discoverActivities.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotter.R
import com.example.globetrotter.data.PlaceWithVisitedCount
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.ItemCategoryPlacesBinding
import com.example.globetrotter.databinding.ItemTopActivitiesBinding

class CategoryAdapter(
    private val itemClick: (Places) -> Unit,
    var categoryPlaces: MutableList<Places> = mutableListOf(),
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var topPlacesAdapter = TopPlacesAdapter(itemClick, categoryPlaces)

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(categories: List<String>) {
        differ.submitList(categories)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemTopActivitiesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    inner class CategoryViewHolder(private val binding: ItemTopActivitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val topPlacesAdapter = TopPlacesAdapter(itemClick, mutableListOf())

        init {
            binding.rvTopPlaces.adapter = topPlacesAdapter
            binding.rvTopPlaces.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }

        fun bind(category: String) {
            binding.categoryNames.text = category
            setCategoryIcon(category)
            val filteredPlaces = categoryPlaces.filter { it.category == category }
            Log.d("FilteredPlaces", "Category: $category, Places: $filteredPlaces")
            topPlacesAdapter.submitList(filteredPlaces)
        }

        private fun setCategoryIcon(category: String) {
            val iconResId = when (category) {
                "Historical" -> R.drawable.icon_historical
                "Natural Wonders" -> R.drawable.icon_natural_wonders
                "Mountains" -> R.drawable.icon_mountain
                "Beaches" -> R.drawable.icon_beach
                "Camping & Hiking" -> R.drawable.icon_camping
                "Urban Exploration" -> R.drawable.icon_urban
                "Islands" -> R.drawable.icon_island
                "Cultural Experiences" -> R.drawable.icon_cultural
                else -> 0
            }

            if (iconResId != 0) {
                binding.categoryNames.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconResId, 0)
            }
        }
    }
}