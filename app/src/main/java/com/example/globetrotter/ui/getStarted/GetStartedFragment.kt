package com.example.globetrotter.ui.getStarted

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.globetrotter.MainActivity
import com.example.globetrotter.R
import com.example.globetrotter.databinding.FragmentGetStartedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetStartedFragment : Fragment() {
    private lateinit var binding: FragmentGetStartedBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentGetStartedBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            if (isAdded) {
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigate(R.id.action_getStartedFragment_to_loginFragment2)
                }, 1000)
            }

//        Glide.with(this)
//            .asGif()
//            .load(R.drawable.travel)
//            .transition(DrawableTransitionOptions.withCrossFade())
//            .into(binding.imageView)


//        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                // Do nothing on progress change
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                // Do nothing on start tracking
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                // Maksimum değere ulaşıldığında geri döndür
//                if (seekBar?.progress == seekBar?.max) {
//                    seekBar?.progress = 0
//                }
//            }
//        })
    }
}