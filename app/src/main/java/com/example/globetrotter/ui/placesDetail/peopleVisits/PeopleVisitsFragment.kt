package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentPeopleVisitsBinding

class PeopleVisitsFragment : Fragment() {
    private lateinit var binding: FragmentPeopleVisitsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPeopleVisitsBinding.inflate(inflater,container,false)
        return binding.root
    }


}