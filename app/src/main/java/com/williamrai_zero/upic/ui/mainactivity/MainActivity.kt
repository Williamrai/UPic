package com.williamrai_zero.upic.ui.mainactivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.williamrai_zero.upic.R
import com.williamrai_zero.upic.databinding.ActivityMainBinding
import com.williamrai_zero.upic.ui.fullimageactivity.FullImageActivity
import com.williamrai_zero.upic.util.networkutil.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ImageAdapter.OnImageListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAdapter: ImageAdapter

    private val imageViewModel: ImageViewModel by viewModels()
    private lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.btnReload.setOnClickListener {
            imageViewModel.getAllImages()
            binding.llNoConnection.visibility = View.GONE
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            imageViewModel.getAllImages()
            binding.swipeRefreshLayout.isRefreshing = false
            binding.llNoConnection.visibility = View.GONE
        }

        Log.d("values","${sharedPref.getBoolean(getString(R.string.first_load),false)}")

    }

    private fun init() {
        defaultLoadState()
        // call to network for data
        observerForImages(true)
        // recyclerView
        setupRecyclerView()

    }

    private fun defaultLoadState() {
        // default load value
        sharedPref = getSharedPreferences(getString(R.string.sharef_prefs),Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.first_load),true)
            commit()
        }
    }


    private fun observerForImages(isFirsLoad: Boolean) {
        imageViewModel.responseImage.observe(this) { response ->
            when (response) {
                // when response is successful
                is Resource.Success -> {
                    hideProgressBar()
                    // sets images to recyclerview adapter
                    response.data?.let { images ->
                        imageAdapter.postData(images)
                    }

                    with(sharedPref.edit()) {
                        putBoolean(getString(R.string.first_load),false)
                        commit()
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        if(sharedPref.getBoolean(getString(R.string.first_load),false)) {
                            binding.llNoConnection.visibility = View.VISIBLE

                        } else {
                            showNetworkSnackBar(message)
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }


    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }


    private fun showNetworkSnackBar(message: String) {
        Snackbar.make(
            binding.clMainActivity,
            "$message",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(this, this )
        binding.imageRecyclerView.apply {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            setHasFixedSize(true)
        }
    }

    override fun onImageClick(url: String) {
        val intent = Intent(this, FullImageActivity::class.java)
        intent.putExtra("url",url)
        startActivity(intent)
    }

}