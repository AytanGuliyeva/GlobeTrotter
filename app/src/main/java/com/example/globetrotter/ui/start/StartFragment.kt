package com.example.globetrotter.ui.start

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.globetrotter.databinding.FragmentStartBinding
import com.example.globetrotter.ui.getStarted.GetStartedFragment
import com.example.globetrotter.ui.getStarted.adapter.ViewPagerAdapter
import com.example.globetrotter.ui.login.LoginFragment
import com.example.globetrotter.ui.signUp.SignUpFragment

class StartFragment : Fragment() {
    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentStartBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = binding.viewPager
        val dotsIndicator = binding.dotsIndicator

        val adapter = ViewPagerAdapter(requireActivity())
        adapter.addFragment(GetStartedFragment(), "Get Started")
        adapter.addFragment(LoginFragment(), "Login")
        adapter.addFragment(SignUpFragment(), "Sign Up")


        viewPager.adapter = adapter
        dotsIndicator.setViewPager2(viewPager)
    }

}