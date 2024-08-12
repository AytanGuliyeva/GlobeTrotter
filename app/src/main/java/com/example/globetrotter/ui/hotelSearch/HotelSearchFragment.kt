package com.example.globetrotter.ui.hotelSearch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentHotelSearchBinding


class HotelSearchFragment : Fragment() {
    private lateinit var binding: FragmentHotelSearchBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentHotelSearchBinding.inflate(inflater,container,false)
        return binding.root
    }


}