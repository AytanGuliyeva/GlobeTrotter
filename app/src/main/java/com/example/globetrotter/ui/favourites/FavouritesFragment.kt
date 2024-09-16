package com.example.globetrotter.ui.favourites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentFavouritesBinding
import com.example.globetrotter.ui.search.SearchFragmentDirections
import com.example.globetrotter.ui.search.SearchViewModel
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FavouritesFragment : Fragment() {
    private lateinit var binding: FragmentFavouritesBinding
    private val viewModel: FavouritesViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        setupRecyclerView()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.placesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading(false)
                    val placesList = result.data
                    placesAdapter.submitList(placesList)
                    if (placesList.isEmpty()) {
                        binding.noPost.visibility=View.VISIBLE
                       // Toast.makeText(requireContext(), "No favourites found.", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error fetching favourites: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading(true)
                }
            }
        }
    }
    private fun placesDetail(placesId: String) {
        val action =
            FavouritesFragmentDirections.actionFavouritesFragmentToPlacesDetailFragment(placesId)
        findNavController().navigate(action)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvPost.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun setupRecyclerView() {
        placesAdapter = PlacesAdapter { place ->
            placesDetail(place.placesId)
        }
        binding.rvPost.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvPost.adapter = placesAdapter
    }

}