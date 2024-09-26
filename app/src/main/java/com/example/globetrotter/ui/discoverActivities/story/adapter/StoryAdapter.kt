package com.example.globetrotter.ui.discoverActivities.story.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotter.data.Story
import com.example.globetrotter.databinding.ItemAddStoryBinding

class StoryAdapter(private var storyList: List<Story>) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding =
            ItemAddStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return storyList.size
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
       holder.bind(storyList[position],position)
    }
    inner class StoryViewHolder(private val binding: ItemAddStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story,position: Int){

            if (position==0){
                binding.story.visibility= View.VISIBLE
            }else{
                binding.story.visibility=View.GONE
            }
        }
    }
}