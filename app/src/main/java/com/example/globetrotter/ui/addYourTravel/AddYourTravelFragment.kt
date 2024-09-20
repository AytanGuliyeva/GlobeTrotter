package com.example.globetrotter.ui.addYourTravel

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.globetrotter.R
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.data.Places
import com.example.globetrotter.databinding.FragmentAddYourTravelBinding
import com.example.globetrotter.ui.discoverActivities.DiscoverActivitiesViewModel
import com.example.globetrotter.ui.placesDetail.PlacesDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class AddYourTravelFragment : BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddYourTravelBinding
    private val viewModelDetail: PlacesDetailViewModel by viewModels()
    private val viewModel: AddYourTravelViewModel by viewModels()
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    companion object {
        fun newInstance(placesId: String): AddYourTravelFragment {
            val fragment = AddYourTravelFragment()
            val args = Bundle()
            args.putString("placesId", placesId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var placesId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddYourTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        arguments?.getString("placesId")?.let {
            placesId = it
        }
        buttonPost()
    }

    fun addOverviewToPlace(placesId: String, overview: String, userId: String) {
        val overviewId = UUID.randomUUID().toString()
        val overviewRef = firestore.collection("Overviews").document(placesId)

        val overviewData = hashMapOf(
            "overview" to overview,
            "userId" to userId,
            "time" to com.google.firebase.Timestamp.now(),
            "overviewId" to overviewId
        )

        overviewRef.set(mapOf("overview" to overviewData), SetOptions.merge())
            .addOnSuccessListener {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Overview added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Failed to add overview.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun sendOverview() {
        val overview = binding.edtCaption
        if (overview.text.trim().toString() == "") {
            Toast.makeText(
                requireContext(),
                "Please add the overview", Toast.LENGTH_SHORT
            ).show()
            overview.text.clear()
        } else {
            addOverviewToPlace(placesId, overview.text.toString(), auth.currentUser!!.uid)
            overview.text.clear()
            dismiss()
        }
    }

    fun buttonPost() {
        binding.buttonAdd.setOnClickListener {
            sendOverview()
        }
        binding.buttonDismess.setOnClickListener {
            dismiss()
        }

    }
}
