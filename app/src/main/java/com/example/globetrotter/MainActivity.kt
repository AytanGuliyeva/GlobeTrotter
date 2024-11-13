package com.example.globetrotter

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.globetrotter.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

//
//        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(SpecialDayWorker::class.java)
//            .setInitialDelay(10, TimeUnit.SECONDS) // İşin 10 saniye sonra çalışması
//            .build()
//
//        // WorkManager ile iş başlatma
//        WorkManager.getInstance(this).enqueue(workRequest)
        // Worker için OneTimeWorkRequest oluşturuluyor
        val workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(SpecialDayWorker::class.java)
            .setInitialDelay(1, TimeUnit.DAYS)  // 1 gün sonra çalışmaya başla
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)  // Başarısız olursa 10 dakika bekle
            .build()

        // WorkManager ile işi kuyruğa ekliyoruz
        WorkManager.getInstance(this).enqueue(workRequest)

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
