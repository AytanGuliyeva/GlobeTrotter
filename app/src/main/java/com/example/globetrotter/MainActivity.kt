package com.example.globetrotter

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.globetrotter.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNav, navController)
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.favouritesFragment
                || destination.id == R.id.searchFragment
                || destination.id == R.id.userProfileFragment
                || destination.id == R.id.discoverActivitiesFragment
            ) {
                showBottomNavigationView()
            } else {
                hideBottomNavigationView()
            }
        }
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.discoverActivitiesFragment -> {
                    navController.popBackStack(R.id.discoverActivitiesFragment, false)
                    true
                }
                R.id.searchFragment -> {
                    navController.popBackStack(R.id.searchFragment, false)
                    navController.navigate(R.id.searchFragment)
                    true
                }
                R.id.userProfileFragment -> {
                    navController.popBackStack(R.id.userProfileFragment, false)
                    navController.navigate(R.id.userProfileFragment)
                    true
                }

                R.id.favouritesFragment -> {
                    navController.popBackStack(R.id.favouritesFragment, false)
                    navController.navigate(R.id.favouritesFragment)
                    true
                }
                else -> false
            }
        }


    }

    
    fun hideBottomNavigationView() {
        binding.bottomNav.visibility = View.GONE
    }

    fun showBottomNavigationView() {
        binding.bottomNav.visibility = View.VISIBLE
    }
}
