package com.example.globetrotter.ui.placesDetail

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentPlacesDetailBinding
import com.example.globetrotter.ui.getStarted.adapter.ViewPagerAdapter
import com.example.globetrotter.ui.search.adapter.PlacesImageAdapter
import com.google.android.material.tabs.TabLayoutMediator

class PlacesDetailFragment : Fragment() {
    private lateinit var binding: FragmentPlacesDetailBinding
    private val viewModel: PlacesDetailViewModel by viewModels()
    val args: PlacesDetailFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPlacesDetailBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        viewModel.categoryDrawable.observe(viewLifecycleOwner) { drawableRes ->
            Log.d(TAG, "Category: , Drawable: $drawableRes")
            binding.illusImage.setImageResource(drawableRes)
        }

        viewModel.placesResult.observe(viewLifecycleOwner) { placesResource ->
            when (placesResource) {
                is Resource.Success -> {

                    updatePostUI(placesResource.data)

                   // binding.progressBar.visibility = View.GONE
                }

                is Resource.Error -> {
                  //  binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    //binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.fetchPlaces(args.placesId)

    }
    private fun updatePostUI(places: Places) {
        val adapter = PlacesImageAdapter(places.placeImageUrls) {

        }
        binding.viewPager.adapter = adapter
        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.setViewPager2(binding.viewPager)

        binding.textPlace.text=places.place
        binding.textCategory2.text=places.category
        binding.textLocation2.text=places.location
        binding.textDescription2.text=places.description
//        val fullText = places.description
//        val shortText = if (fullText.length > 100) {
//            fullText.substring(0, 100) + "..."
//        } else {
//            fullText
//        }
//
//        binding.textDescription2.text = shortText
//
//        // "more" yazısını ekle
//        if (fullText.length > 100) {
//            binding.textDescription2.append(" more")
//            binding.textDescription2.setTextColor(Color.BLACK)
//
//            // More tıklanınca tüm metni göster
//            binding.textDescription2.setOnClickListener {
//                binding.textDescription2.text = fullText
//            }
//        }

        binding.textPrice.text="$ ${places.price}"
        if (places.price == "Price changes according to place and transport.") {
            binding.textPrice.setTextColor(resources.getColor(R.color.red, null))
        }
    }
    private fun initListener() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}