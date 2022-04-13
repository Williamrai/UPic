package com.williamrai_zero.upic.ui.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.williamrai_zero.upic.databinding.ActivityMainBinding
import com.williamrai_zero.upic.ui.viewmodels.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAdapter: ImageAdapter

    private val imageViewModel: ImageViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    private fun init() {
        // call to network for data
        setupData()
        // recyclerView
        setupRecyclerView()
    }

    private fun setupData() {
        imageViewModel.responseImage.observe(this) { response ->
            when (response.size) {
                1 -> {
                    Toast.makeText(this,response[0].url,Toast.LENGTH_LONG).show()
                } else -> {
                imageAdapter.postData(response)
                }
            }
            
            // until response is filled runs progress bar
            if (response.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
            }
        }

    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(this)

        binding.imageRecyclerView.apply {
            adapter = imageAdapter
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
        }
    }

}