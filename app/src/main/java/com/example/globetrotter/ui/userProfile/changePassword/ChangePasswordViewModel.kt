package com.example.globetrotter.ui.userProfile.changePassword

import android.app.ProgressDialog
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordViewModel : ViewModel() {
    private var firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    fun getUserInfo() {
        firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                //    _userInformation.postValue(Resource.Error("Error fetching user info"))
                } else {
                    val password = value?.getString(ConstValues.PASSWORD) ?: ""
                    _userInformation.postValue(
                        Resource.Success(
                            Users(password = password)
                        )
                    )
                }
            }
    }

    fun updateUserInfo(password: String, progressDialog: ProgressDialog) {
        val userRef = firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
        userRef.update(mapOf(ConstValues.PASSWORD to password))
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(progressDialog.context, "Password updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(progressDialog.context, "Failed to update password", Toast.LENGTH_SHORT).show()
            }
    }
}
