package com.example.globetrotter.ui.userProfile.myOverviews.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.Story
import com.example.globetrotter.databinding.ItemMyOverviewsBinding
import kotlinx.coroutines.NonDisposableHandle
import kotlinx.coroutines.NonDisposableHandle.parent

class MyOverviewsAdapter(private val onDeleteOverview: (String) -> Unit) : RecyclerView.Adapter<MyOverviewsAdapter.MyOverviewsViewHolder>() {

    private var storyMap: Map<String, Story> = mapOf()

    fun submitStoryMap(storyMap: Map<String, Story>) {
        this.storyMap = storyMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOverviewsViewHolder {
        val binding = ItemMyOverviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyOverviewsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return storyMap.size
    }

    override fun onBindViewHolder(holder: MyOverviewsViewHolder, position: Int) {
        val placeName = storyMap.keys.toList()[position]
        val story = storyMap[placeName]
        holder.bind(placeName, story)
    }

    inner class MyOverviewsViewHolder(private val binding: ItemMyOverviewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(placeName: String, story: Story?) {
            story?.let {
                Glide.with(binding.root)
                    .load(it.imageUrl)
                    .into(binding.imgCountry)
                binding.txtCountryName.text=placeName

                binding.txtStory.text = story.caption
            }
            binding.btnOption.setOnClickListener {
                onDeleteOverview(story!!.placesId)
            }        }
    }
}
