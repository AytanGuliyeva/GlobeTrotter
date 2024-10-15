package com.example.globetrotter.ui.placesDetail.addYourTravel

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.databinding.FragmentAddYourTravelBinding
import com.example.globetrotter.ui.placesDetail.PlacesDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class AddYourTravelFragment : BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddYourTravelBinding
    private val viewModel: AddYourTravelViewModel by viewModels()
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private var selectPicture: Uri? = null

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
        storage = Firebase.storage

        arguments?.getString("placesId")?.let {
            placesId = it
        }
        binding.imgAddPost.setOnClickListener {
            selectImage(it)
        }

        buttonPost()
        registerLauncher()
    }
    private fun uploadStoryAndOverview() {
        val progress = ProgressDialog(requireActivity())
        progress.setMessage("Please wait while adding the post!")
        progress.show()

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val imageReference = storage.reference.child("story/$imageName")

        selectPicture?.let {
            imageReference.putFile(it).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { imgUrl ->
                    val downloadUrl = imgUrl.toString()
                    val randomKey = UUID.randomUUID().toString()
                    val myId = auth.currentUser!!.uid
                    val ref = firestore.collection("Story").document(myId)
                    val hmapkey = hashMapOf<String, Any>()
                    val hmap = hashMapOf<String, Any>()
                    hmap[ConstValues.IMAGE_URL] = downloadUrl
                    hmap["storyId"] = randomKey
                    hmap[ConstValues.USER_ID] = myId
                    hmap["caption"] = binding.edtCaption.text.toString()
                    hmap["placesId"] = placesId
                    hmapkey[placesId] = hmap
                    ref.set(hmapkey, SetOptions.merge()).addOnSuccessListener {
                        progress.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Story successfully shared!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }.addOnFailureListener { error ->
                        progress.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Story not shared: ${error.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    progress.dismiss()
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            progress.dismiss()
            Toast.makeText(requireContext(), "Please select a photo", Toast.LENGTH_SHORT).show()
        }
    }


    /*
        private fun uploadStoryAndOverview() {
            val progress = ProgressDialog(requireActivity())
            progress.setMessage("Please wait while adding the post!")
            progress.show()

            val uuid = UUID.randomUUID()
            val imageName = "$uuid.jpg"
            val imageReference = storage.reference.child("story/$imageName")

            selectPicture?.let {
                imageReference.putFile(it).addOnSuccessListener {
                    imageReference.downloadUrl.addOnSuccessListener { imgUrl ->
                        val downloadUrl = imgUrl.toString()
                        val randomKey = UUID.randomUUID().toString()
                        val myId = auth.currentUser!!.uid
                        val ref = firestore.collection("Story").document(myId)
                        val timeEnd = System.currentTimeMillis() + 86400000
                        val hmapkey = hashMapOf<String, Any>()
                        val hmap = hashMapOf<String, Any>()
                        hmap[ConstValues.IMAGE_URL] = downloadUrl
                        hmap["timeStart"] = System.currentTimeMillis()
                        hmap["timeEnd"] = timeEnd
                        hmap["storyId"] = randomKey
                        hmap[ConstValues.USER_ID] = myId
                        hmap["caption"] = binding.edtCaption.text.toString()
                        hmap["placesId"] = placesId
                        hmapkey[placesId] = hmap
                        ref.set(hmapkey, SetOptions.merge()).addOnSuccessListener {
                            progress.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "Story successfully shared!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }.addOnFailureListener { error ->
                            progress.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "Story not shared: ${error.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        progress.dismiss()
                        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                progress.dismiss()
                Toast.makeText(requireContext(), "Please select a photo", Toast.LENGTH_SHORT).show()
            }
        }
    */

    private fun selectImage(view: View) {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    view,
                    "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Give permission") {
                        permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
            } else {
                permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectPicture = intentFromResult.data
                        selectPicture?.let {
                            binding.imgAddPost.setImageURI(it)
                            binding.imgAddPost.visibility = View.VISIBLE
                        }
                    }
                }

            }

        permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
    }

    fun buttonPost() {
        binding.buttonAdd.setOnClickListener {
            if (selectPicture != null && !binding.edtCaption.text.isNullOrEmpty()) {
                uploadStoryAndOverview()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please add an image and a caption.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonDismess.setOnClickListener {
            dismiss()
        }
    }
}