package com.example.globetrotter.ui.placesDetail

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentPlacesDetailBinding
import com.example.globetrotter.ui.addYourTravel.AddYourTravelFragment
import com.example.globetrotter.ui.getStarted.adapter.ViewPagerAdapter
import com.example.globetrotter.ui.search.adapter.PlacesImageAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PlacesDetailFragment : Fragment() {
    private lateinit var binding: FragmentPlacesDetailBinding
    private val viewModel: PlacesDetailViewModel by viewModels()
    val args: PlacesDetailFragmentArgs by navArgs()
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlacesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        viewModel.categoryDrawable.observe(viewLifecycleOwner) { drawableRes ->
            Log.d(TAG, "Category: , Drawable: $drawableRes")
            binding.illusImage.setImageResource(drawableRes)
        }


        observeVisitersCount()
        viewModel.placesResult.observe(viewLifecycleOwner) { placesResource ->
            when (placesResource) {
                is Resource.Success -> {
                    viewModel.fetchVisitedCount(placesResource.data.placesId)
                    viewModel.checkVisitStatus(placesResource.data.placesId, binding.buttonVisited)

                    updatePostUI(placesResource.data)
                    initListener(placesResource.data)

                    Log.e("TAG3", "onViewCreated: ${placesResource.data}")

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

    private fun observeVisitersCount() {
        viewModel.visitedCount.observe(viewLifecycleOwner) { visitersCount ->
            if (visitersCount <= 1) {
                binding.textVisitedCount.text = "$visitersCount visiter"
            } else {
                binding.textVisitedCount.text = "$visitersCount visiters"
            }
        }
    }

    private fun updatePostUI(places: Places) {
        viewModel.checkFavStatus(places.placesId, binding.buttonFav)
        viewModel.checkVisitStatus(places.placesId, binding.buttonVisited)
        binding.buttonFav.setOnClickListener {
            viewModel.toggleLikeStatus(places.placesId, binding.buttonFav)
        }
//        binding.buttonVisited.setOnClickListener {
//            viewModel.toggleVisitedStatus(places.placesId, binding.buttonVisited)
//        }
        val adapter = PlacesImageAdapter(places.placeImageUrls) {

        }
        binding.viewPager.adapter = adapter
        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.setViewPager2(binding.viewPager)

        binding.textPlace.text = places.place
        binding.textCategory2.text = places.category
        binding.textLocation2.text = places.location
        binding.textDescription2.text = places.description

        binding.textPrice.text = "$ ${places.price}"
        if (places.price == "Price changes according to place and transport.") {
            binding.textPrice.setTextColor(resources.getColor(R.color.red, null))
        }
    }

    private fun initListener(places: Places) {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonVisited.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.visited_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnYes: TextView = dialog.findViewById(R.id.btnYes)
            val btnNo: TextView = dialog.findViewById(R.id.btnNo)

            btnYes.setOnClickListener {
//                if (!viewModel.isVisited.value!!) {
                    val bottomSheet = AddYourTravelFragment.newInstance(places.placesId)
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                    viewModel.visitedClickListener(places.placesId)
                    viewModel.toggleVisitedStatus(places.placesId, binding.buttonVisited)
             //   }
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
            //    if (viewModel.isVisited.value!!) {
                    viewModel.toggleVisitedStatus(places.placesId, binding.buttonVisited)
                    Toast.makeText(requireContext(), "Visit removed", Toast.LENGTH_SHORT).show()
               // }
                dialog.dismiss()
            }


            dialog.show()
        }
    }

}
