package com.example.globetrotter.ui.addPlaces

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentAddPlacesBinding
import com.example.globetrotter.retrofit.model.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class AddPlacesFragment : Fragment() {
    private lateinit var binding: FragmentAddPlacesBinding
    private var selectedImageBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 71
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    private val selectedImages = mutableListOf<Bitmap>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        initNavigationListeners()
        addImage()
        selectedImage()
    }

    private fun initNavigationListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()

        }
    }

    private fun selectedImage() {
        binding.imgAddPost.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                Intent.createChooser(galleryIntent, "Select Pictures"),
                PICK_IMAGE_REQUEST
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK) {
            if (data?.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        imageUri
                    )
                    selectedImages.add(bitmap)
                }
            } else if (data?.data != null) {
                val imageUri = data.data
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                selectedImages.add(bitmap)
            }
            if (selectedImages.isNotEmpty()) {
                binding.imgAddPost.setImageBitmap(selectedImages[0])
            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addImage() {
        binding.btnShare.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val place = binding.edtPlace.text.toString()
            val description = binding.edtDescription.text.toString()
            val category = binding.edtCategory.text.toString()
            val location = binding.edtLocation.text.toString()
            val price = binding.edtTicketPrice.text.toString()

            if (selectedImages.isNotEmpty()) {
                val ref = firestore.collection("Places").document()
                val placesId = ref.id

                val placesMap = hashMapOf<String, Any>(
                    "place" to place,
                    "description" to description,
                    "category" to category,
                    "location" to location,
                    "price" to price,
                    "placesId" to placesId
                )

                uploadImages(placesId, placesMap, ref)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select an image",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadImages(
        placesId: String,
        placesMap: HashMap<String, Any>,
        ref: DocumentReference
    ) {
        val uploadedImageUrls = mutableListOf<String>()

        for ((index, bitmap) in selectedImages.withIndex()) {
            val boas = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)
            val imageData = boas.toByteArray()

            val storageRef = storage.reference.child("images/$placesId-$index.jpg")
            storageRef.putBytes(imageData)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        uploadedImageUrls.add(uri.toString())
                        if (uploadedImageUrls.size == selectedImages.size) {
                            placesMap["placeImageUrls"] = uploadedImageUrls
                            addProductInfoFireStore(placesMap, ref)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Upload", "Failed to upload image $index", exception)
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload image $index",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun addProductInfoFireStore(placeMap: HashMap<String, Any>, ref: DocumentReference) {
        ref.set(placeMap)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_addPlacesFragment_to_discoverActivitiesFragment)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed...", Toast.LENGTH_SHORT).show()
            }
    }


}