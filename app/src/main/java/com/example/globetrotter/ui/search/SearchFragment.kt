package com.example.globetrotter.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentSearchBinding
import com.example.globetrotter.ui.discoverActivities.DiscoverActivitiesViewModel
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore

        setupRecyclerView()
        // Start observing ViewModel LiveData
        observePlaceResult()
        observeCategories()

        // Fetch data
        viewModel.fetchPlaces()
        viewModel.fetchCategoriesAndAddChips(binding.chipGroup)
    }

    private fun observePlaceResult() {
        viewModel.placesResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<List<Places>> -> {
                    // Handle successful fetching of places
                    val places = resource.data
                    placesAdapter.submitList(places)
                }

                is Resource.Error -> {
                    // Handle error
                    val exception = resource.exception
                    Log.e("SearchFragment", "Error fetching places: ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch places: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {
                    // Show loading state if needed
                    Log.d("SearchFragment", "Loading places...")
                    // Optionally, show a progress bar or loading animation
                    showLoading(true)
                }
            }
        }
    }
    private fun setupRecyclerView() {
        placesAdapter=PlacesAdapter()
        binding.rvPost.adapter=placesAdapter
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categoriesResource ->
            when (categoriesResource) {
                is Resource.Success -> {
                    Log.d(
                        "SearchFragment",
                        "Categories fetched successfully: ${categoriesResource.data}"
                    )
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch categories: ${categoriesResource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {
                    Log.d("SearchFragment", "Loading categories...")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.rvPost.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.rvPost.visibility = View.VISIBLE
        }
    }
}
