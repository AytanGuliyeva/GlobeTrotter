package com.example.globetrotter.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        Glide.with(this)
            .asGif()
            .load(R.drawable.travel)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.animation)

//        binding.btnLogin.setOnClickListener {
//            findNavController().navigate(R.id.discoverActivitiesFragment)
//
//
//        }
        checkLogin()
        btnLogin()
        initNavigationListeners()
    }

        private fun btnLogin() {
            binding.btnLogin.setOnClickListener {
                val username = binding.edtUsername.text.toString()
                val password = binding.edtPassword.text.toString()
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    auth.signInWithEmailAndPassword(
                        username, password
                    ).addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        findNavController().navigate(R.id.discoverActivitiesFragment)
                      //  findNavController().navigate(R.id.action_loginFragment_to_discoverActivitiesFragment)

                    }.addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Incorrect username or password", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        private fun checkLogin() {
            if (auth.currentUser != null) {
                findNavController().navigate(R.id.discoverActivitiesFragment)
               // findNavController().navigate(R.id.action_loginFragment_to_discoverActivitiesFragment)
            }
        }

        private fun initNavigationListeners() {
            binding.btnCreateNewAccount.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
                //findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            }
        }
    }