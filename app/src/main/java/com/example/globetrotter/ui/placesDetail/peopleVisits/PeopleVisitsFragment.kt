package com.example.globetrotter.ui.placesDetail.peopleVisits

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.globetrotter.R
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.FragmentPeopleVisitsBinding
import com.example.globetrotter.ui.placesDetail.peopleVisits.adapter.VisitsAdapter
import com.example.globetrotter.base.Resource

class PeopleVisitsFragment : Fragment() {
    private lateinit var binding: FragmentPeopleVisitsBinding
    private lateinit var visitAdapter: VisitsAdapter

    private val viewModel: PeopleVisitsViewModel by viewModels()
    private val args: PeopleVisitsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPeopleVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchVisitedPeople(args.placesId)
    }

    private fun initListener() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        visitAdapter = VisitsAdapter()
        binding.rvPeople.layoutManager = LinearLayoutManager(context)
        binding.rvPeople.adapter = visitAdapter
    }

    private fun observeViewModel() {
        viewModel.peopleList.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { userList ->
                        visitAdapter.submitList(userList) // Users and story status
                        Log.e("TAGstoryfrag", "fetchStoriesForUser: $userList", )

                    }
                }
                is Resource.Loading -> {
                    // Handle loading state
                }
                is Resource.Error -> {
                    // Handle error state
                }
            }
        })
    }
}
