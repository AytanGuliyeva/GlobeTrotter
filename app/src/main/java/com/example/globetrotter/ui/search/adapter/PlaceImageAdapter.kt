package com.example.globetrotter.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.globetrotter.databinding.ItemImageBinding

class PlacesImageAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: () -> Unit
) :
    RecyclerView.Adapter<PlacesImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.binding.root.context)
            .load(imageUrls[position])
            .into(holder.binding.imageView)
        holder.binding.imageView.setOnClickListener {
            onImageClick()
        }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}
