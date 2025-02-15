package com.example.globetrotter.ui.userProfile.myOverviews

import android.app.AlertDialog
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
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Story
import com.example.globetrotter.databinding.FragmentMyOverViewsBinding
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.example.globetrotter.ui.userProfile.UserProfileViewModel
import com.example.globetrotter.ui.userProfile.myOverviews.adapter.MyOverviewsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyOverViewsFragment : Fragment() {
    private lateinit var binding: FragmentMyOverViewsBinding
    val viewModel: MyOverviewsViewModel by viewModels()
    private val overviewList = ArrayList<Story>()
    private lateinit var myOverviewsAdapter: MyOverviewsAdapter


    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyOverViewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        initNavigationListeners()
        setupRecyclerView()
        viewModel.getOverviews()
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.storyInformation.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val storyMap = resource.data
                    binding.noPost.visibility = if (storyMap.isNotEmpty()) View.GONE else View.VISIBLE
                    myOverviewsAdapter.submitStoryMap(storyMap)
                }
                is Resource.Loading -> { /* Show loading if needed */ }
                is Resource.Error -> { /* Show error if needed */ }
            }
        }

        viewModel.overviewDeleted.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data == true) {
                        Toast.makeText(requireContext(), "Overview deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error deleting overview", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> { /* Show loading if needed */ }
            }
        }
    }

    private fun setupRecyclerView() {
        myOverviewsAdapter = MyOverviewsAdapter { placesId ->
            showDeleteConfirmationDialog(placesId)
        }
        binding.rvOverview.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOverview.adapter = myOverviewsAdapter
    }

    private fun showDeleteConfirmationDialog(placesId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Overview")
            .setMessage("Are you sure you want to delete this overview?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteOverview(placesId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun initNavigationListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}

