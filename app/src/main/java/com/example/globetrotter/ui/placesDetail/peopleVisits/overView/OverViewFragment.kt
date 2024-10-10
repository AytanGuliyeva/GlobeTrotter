package com.example.globetrotter.ui.placesDetail.peopleVisits.overView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentOverViewBinding
import com.example.globetrotter.ui.placesDetail.addYourTravel.AddYourTravelViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OverViewFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOverViewBinding
    private val viewModel: OverViewViewModel by viewModels()


    companion object {
        fun newInstance(userId: String): OverViewFragment {
            val fragment = OverViewFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOverViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("userId")?.let {
            userId = it
        } ?: run {
        }}

}