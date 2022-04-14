package com.williamrai_zero.upic.ui.mainactivity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.williamrai_zero.upic.R
import com.williamrai_zero.upic.databinding.ImageLayoutBinding
import com.williamrai_zero.upic.model.ImageItem
import com.williamrai_zero.upic.ui.fullimageactivity.FullImageActivity

class ImageAdapter(private val context: Context, private val onImageListener: OnImageListener) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ImageLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    // if different data
    // what is diffUtil.ItemCallback
    private val diffCallBack = object : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem == newItem
        }
    }

    // what is AsyncListDiffer
    private val differ = AsyncListDiffer(this, diffCallBack)

    fun postData(list: List<ImageItem>) = differ.submitList(list)

    // Adapters classes
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // set the width and height of the imageview
        val width = parent.width / 2
        val layoutParams = view.ivImages.layoutParams
        layoutParams.width = width

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currImage = differ.currentList[position]

        holder.binding.apply {
            val imageUrl = currImage.url

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .transform(CenterCrop(), RoundedCorners(12))
                .into(ivImages)
        }

        holder.binding.ivImages.setOnClickListener {
            onImageListener.onImageClick(currImage.url)
        }
    }

    override fun getItemCount() = differ.currentList.size

    interface OnImageListener {
        fun onImageClick(url: String)
    }
}