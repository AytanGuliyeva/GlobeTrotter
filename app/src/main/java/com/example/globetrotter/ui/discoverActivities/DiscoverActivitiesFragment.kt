package com.example.globetrotter.ui.discoverActivities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.databinding.FragmentDiscoverActivitiesBinding
import com.example.globetrotter.ui.discoverActivities.story.StoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DiscoverActivitiesFragment : Fragment() {
    private lateinit var binding: FragmentDiscoverActivitiesBinding
    private val viewModel: DiscoverActivitiesViewModel by viewModels()
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    private lateinit var storyAdapter: StoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiscoverActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        initNavigationListeners()

        if (auth.currentUser?.uid == "Jcs1FXd95xfRNPibSm3Z4IFP6513") {
            binding.floatActionButton.visibility = View.VISIBLE
        }

        viewModel.fetchInformation()
        viewModel.fetchCategoriesAndAddChips(binding.chipGroup)
        Log.e("TAG", "onViewCreated: ${viewModel.fetchCategoriesAndAddChips(binding.chipGroup)}")
        viewModel.userInformation.observe(viewLifecycleOwner) { userResource ->
            when (userResource) {
                is Resource.Success -> {
                    Glide.with(binding.root)
                        .load(userResource.data.imageUrl)
                        .into(binding.imgProfile)
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "${userResource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                is Resource.Loading -> {
                }
            }
        }
        viewModel.categories.observe(viewLifecycleOwner) { categoriesResource ->
            when (categoriesResource) {
                is Resource.Success -> {
                    Log.d("TAG", "Categories fetched successfully: ${categoriesResource.data}")
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch categories: ${categoriesResource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {
                    // Optional: Show loading indicator
                }
            }
        }
    }

    private fun initNavigationListeners() {
        binding.floatActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_discoverActivitiesFragment_to_addPlacesFragment)
        }
    }


}