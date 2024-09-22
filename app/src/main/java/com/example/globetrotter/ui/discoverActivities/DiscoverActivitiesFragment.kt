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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.PlaceWithVisitedCount
import com.example.globetrotter.databinding.FragmentDiscoverActivitiesBinding
import com.example.globetrotter.ui.discoverActivities.adapter.CategoryAdapter
import com.example.globetrotter.ui.discoverActivities.adapter.TopPlacesAdapter
import com.example.globetrotter.ui.discoverActivities.story.StoryAdapter
import com.example.globetrotter.ui.search.SearchFragmentDirections
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
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
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var topPlacesAdapter: TopPlacesAdapter

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
        setupRecyclerView()
        viewModel.fetchPopularPlaces()
        viewModel.fetchCategories()
        viewModel.places.observe(viewLifecycleOwner) { placesResource ->
            when (placesResource) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    categoryAdapter.categoryPlaces.clear()
                    categoryAdapter.categoryPlaces.addAll(placesResource.data)
                    categoryAdapter.notifyDataSetChanged()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch places: ${placesResource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {

                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.categories.observe(viewLifecycleOwner) { categoriesResource ->
            when (categoriesResource) {
                is Resource.Success -> {
                    categoryAdapter.submitList(categoriesResource.data)

                }

                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
        initNavigationListeners()
        if (auth.currentUser?.uid == "Jcs1FXd95xfRNPibSm3Z4IFP6513") {
            binding.floatActionButton.visibility = View.VISIBLE
        }
        viewModel.fetchInformation()

    }


    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter({ place ->
            placesDetail(place.placesId)
        }, mutableListOf())
        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategory.adapter = categoryAdapter
    }

    private fun placesDetail(placesId: String) {
        val action =
            DiscoverActivitiesFragmentDirections.actionDiscoverActivitiesFragmentToPlacesDetailFragment(
                placesId
            )
        findNavController().navigate(action)
    }

    private fun initNavigationListeners() {
        binding.floatActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_discoverActivitiesFragment_to_addPlacesFragment)
        }
    }


}