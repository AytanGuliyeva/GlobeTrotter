package com.example.globetrotter.ui.flightSearch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentFlightSearchBinding

class FlightSearchFragment : Fragment() {
    private lateinit var binding: FragmentFlightSearchBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFlightSearchBinding.inflate(inflater,container,false)
        return binding.root
    }


}