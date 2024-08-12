package com.example.globetrotter.ui.discoverActivities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.globetrotter.databinding.FragmentDiscoverActivitiesBinding

class DiscoverActivitiesFragment : Fragment() {
    private lateinit var binding: FragmentDiscoverActivitiesBinding
    private val viewModel: DiscoverActivitiesViewModel  by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDiscoverActivitiesBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val coordinates = query.split(",")
                    if (coordinates.size == 2) {
                        val latitude = coordinates[0].toDoubleOrNull()
                        val longitude = coordinates[1].toDoubleOrNull()
                        if (latitude != null && longitude != null) {
                            viewModel.fetchActivities(latitude, longitude, 10)

                            Log.e("TAG", "l")
                            Log.e("TAG", "${viewModel.fetchActivities(latitude, longitude, 1)}")


                        } else {
                            Log.e("TAG", "Invalid coordinates format")
                        }
                    } else {
                        Log.e("TAG", "Invalid coordinates format")
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })


    }

}