package com.example.globetrotter.ui.userProfile.editProfile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Users
import com.example.globetrotter.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

class EditProfileFragment : Fragment() {
    lateinit var binding: FragmentEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private var selectedImageBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var progressDialoq: ProgressDialog
    private lateinit var imageUrl: String

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialoq = ProgressDialog(requireContext())
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        initNavigationListeners()
        viewModel.getUserInfo()
        viewModel.userInformation.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    updateUI(user)
                    imageUrl = user.imageUrl
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        initNavigationListeners()
        selectedImage()
        addImage()
    }


    private fun initNavigationListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateUI(users: Users){
        binding.txtEditUsurname.setText(users.username)
        Glide.with(requireContext()).load(users.imageUrl).into(binding.imgProfile)
    }
    private fun selectedImage(){
        binding.imgProfile.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent,PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                selectedImageUri
            )
            binding.imgProfile.setImageBitmap(selectedImageBitmap)
        } else {
            Toast.makeText(requireContext(), "Something gone wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImage() {
        binding.txtDone.setOnClickListener {
            progressDialoq.setTitle("Info")
            progressDialoq.setMessage("Update user info")
            progressDialoq.show()
            val username = binding.txtEditUsurname.text.toString()

            if (selectedImageBitmap != null) {
                uploadImage(username)
            } else {
                updateUserProfile(username,imageUrl)
            }
        }
    }

    private fun uploadImage(username: String) {
        selectedImageBitmap?.let { bitmap ->
            val boas = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)
            val imageData = boas.toByteArray()
            val uuid = UUID.randomUUID()
            val imageName = "$uuid.jpg"

            val storageRef = storage.reference.child(ConstValues.IMAGES).child(imageName)
            storageRef.putBytes(imageData)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        updateUserProfile(username, downloadUrl)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                        .show()
                }

        }
    }


    private fun updateUserProfile(username: String, imageUrl: String) {
        viewModel.updateUserInfo(username, imageUrl, progressDialoq)
    }
}