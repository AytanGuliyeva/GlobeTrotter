package com.example.globetrotter.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentSearchBinding
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.google.android.material.tabs.TabLayout
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
    private var selectedPlaces: Places? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2


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
        observePlaceResult()
        observeCategories()


        viewModel.fetchPlaces()
        viewModel.fetchCategoriesAndAddChips(binding.chipGroup)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterPlacesByLocation(newText)
                return true
            }
        })
    }
    private fun filterPlacesByLocation(query: String?) {
        query?.let {
            viewModel.searchPlaces(it)
        }
    }



    private fun observePlaceResult() {
        viewModel.placesResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<List<Places>> -> {
                    placesAdapter.submitList(resource.data)
                    showLoading(false)
                }

                is Resource.Error -> {
                    Log.e("SearchFragment", "Error fetching places: ${resource.exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch places: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                }

                is Resource.Loading -> {
                    Log.d("SearchFragment", "Loading places...")
                    showLoading(true)
                }
            }
        }
    }


    private fun setupRecyclerView() {
        placesAdapter = PlacesAdapter(itemClick = { place ->
            placesDetail(place.placesId)
        })
        binding.rvPost.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvPost.adapter = placesAdapter
    }

    fun placesDetail(placesId: String) {
        val action = SearchFragmentDirections.actionSearchFragmentToPlacesDetailFragment(placesId)
        findNavController().navigate(action)
    }


    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categoriesResource ->
            when (categoriesResource) {
                is Resource.Success -> {
                    Log.d("SearchFragment", "Categories fetched successfully: ${categoriesResource.data}")
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvPost.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
