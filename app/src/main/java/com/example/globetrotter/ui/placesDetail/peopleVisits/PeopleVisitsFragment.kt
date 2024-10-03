package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.globetrotter.R
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.FragmentPeopleVisitsBinding
import com.example.globetrotter.ui.placesDetail.peopleVisits.adapter.VisitsAdapter
import com.example.globetrotter.base.Resource
import com.example.globetrotter.ui.placesDetail.PlacesDetailFragmentArgs

class PeopleVisitsFragment : Fragment() {
    private lateinit var binding: FragmentPeopleVisitsBinding
    private val visitAdapter by lazy { VisitsAdapter() }
    private val viewModel: PeopleVisitsViewModel by viewModels()
    val args: PeopleVisitsFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPeopleVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        setupRecyclerView()

        viewModel.fetchVisitedPeople(args.placesId)

        viewModel.peopleList.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { userList ->
                        visitAdapter.submitList(userList)
                    }
                }

                is Resource.Loading -> {}
                is Resource.Error -> {}
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvPeople.adapter = visitAdapter
    }

    private fun initListener() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
