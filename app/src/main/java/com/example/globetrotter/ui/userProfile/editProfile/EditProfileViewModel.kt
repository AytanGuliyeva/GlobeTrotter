package com.example.globetrotter.ui.userProfile.editProfile

import android.app.ProgressDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileViewModel : ViewModel() {
    private var firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    fun getUserInfo() {
        firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // _userInformation.postValue(Resource.Error())
                } else {
                    val username = value?.getString(ConstValues.USERNAME) ?: ""
                    val imageUrl = value?.getString(ConstValues.IMAGE_URL) ?: ""
                    _userInformation.postValue(
                        Resource.Success(
                            Users(
                                username = username,
                                imageUrl = imageUrl,
                            )
                        )
                    )
                }
            }
    }

    fun updateUserInfo(
        username: String,
        imageUrl: String,
        progressDialog: ProgressDialog
    ) {
        val userRef = firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
        userRef.update(
            mapOf(
                ConstValues.USERNAME to username,
                ConstValues.IMAGE_URL to imageUrl
            )
        )
            .addOnSuccessListener {
                progressDialog.dismiss()
            }.addOnFailureListener { }
    }
}