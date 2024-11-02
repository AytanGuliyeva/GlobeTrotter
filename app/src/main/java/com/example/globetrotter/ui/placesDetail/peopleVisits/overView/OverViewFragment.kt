package com.example.globetrotter.ui.placesDetail.peopleVisits.overView

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.databinding.FragmentOverViewBinding
import com.example.globetrotter.ui.placesDetail.addYourTravel.AddYourTravelViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OverViewFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOverViewBinding
    private val viewModel: OverViewViewModel by viewModels()
    private val args: OverViewFragmentArgs by navArgs()

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    companion object {
        fun newInstance(userId: String,placesId:String): OverViewFragment {
            val fragment = OverViewFragment()
            val args = Bundle()
            args.putString("userId", userId)
            args.putString("placesId",placesId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var userId: String
    private lateinit var placesId:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOverViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore

        arguments?.getString("userId")?.let { userId = it }
        arguments?.getString("placesId")?.let { placesId = it }

        viewModel.getStories(userId, placesId)
        viewModel.fetchPlaces(placesId)
        viewModel.placesResult.observe(viewLifecycleOwner){placeResult ->
            when (placeResult) {
                is Resource.Success -> {
                    binding.txtCountryName.text=placeResult.data.place
                }
                is Resource.Error -> { /* Handle error */ }
                is Resource.Loading -> { /* Handle loading */ }
            }
        }
        viewModel.storyInformation.observe(viewLifecycleOwner) { storyResource ->
            when (storyResource) {
                is Resource.Success -> {
                    val story = storyResource.data.firstOrNull()
                    if (story != null) {
                        binding.imgCountry.visibility = View.VISIBLE
                        binding.txtStory.visibility = View.VISIBLE
                        binding.buttonFav.visibility=View.VISIBLE
                        binding.txtCaption.visibility=View.VISIBLE
                        binding.noPost.visibility=View.GONE


                        Glide.with(binding.root)
                            .load(story.imageUrl)
                            .into(binding.imgCountry)
                        binding.txtStory.text = story.caption

                        if (auth.currentUser!!.uid==story.userId){
                            binding.buttonFav.visibility=View.VISIBLE
                            viewModel.checkLikeStatus(story.storyId,binding.buttonFav)
                            binding.buttonFav.setOnClickListener {
                                viewModel.toggleLikeStatus(story.storyId,binding.buttonFav)
                            }
                        }

                    } else {
                        binding.noPost.visibility=View.VISIBLE
//                        binding.imgCountry.visibility = View.GONE
//                        binding.txtStory.visibility = View.GONE
//                        binding.btnSeeMore.visibility=View.GONE
//                        binding.txtCaption.visibility=View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.imgCountry.visibility = View.GONE
                    binding.txtStory.visibility = View.GONE
                    binding.buttonFav.visibility=View.GONE
                    binding.txtCaption.visibility=View.GONE
                }
                is Resource.Loading -> { /* Handle loading */ }
            }
        }

    }


}