package com.example.globetrotter.ui.userProfile.changePassword

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.FragmentChangePasswordBinding
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.example.globetrotter.ui.userProfile.myOverviews.adapter.MyOverviewsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
     val viewModel: ChangePasswordViewModel by viewModels()
    private lateinit var progressDialoq: ProgressDialog
//    lateinit var auth: FirebaseAuth
//    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialoq = ProgressDialog(requireContext())
        auth = Firebase.auth
        firestore = Firebase.firestore
        initNavigationListeners()
        viewModel.getUserInfo()
        viewModel.userInformation.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    updateUI(user)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error retrieving user info", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    private fun updateUI(user: Users) {
        binding.txtDone.setOnClickListener {
            if (binding.edtCurrentPassword.text.toString() == user.password) {
                if (binding.editReNewPassword.text.toString() == binding.editNewPassword.text.toString()) {
                    progressDialoq.setTitle("Change Password")
                    progressDialoq.setMessage("Updating Password...")
                    progressDialoq.show()
                    val newPassword = binding.editReNewPassword.text.toString()
                    viewModel.updateUserInfo(newPassword, progressDialoq)
                    auth.currentUser!!.updatePassword(newPassword)
                } else {
                    Toast.makeText(requireContext(), "Re-typed password doesn't match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Current password is wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initNavigationListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
