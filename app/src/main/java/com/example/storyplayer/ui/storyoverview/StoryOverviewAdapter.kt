package com.example.storyplayer.ui.storyoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyplayer.R
import com.example.storyplayer.data.StoryGroup
import com.example.storyplayer.databinding.ItemStoryGroupBinding


class StoryOverviewAdapter(private val data: List<StoryGroup>, private var onItemClicked: ((index: Int) -> Unit)): RecyclerView.Adapter<StoryOverviewAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemStoryGroupBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind (data: StoryGroup, position: Int) {
            Glide.with(binding.root.context).load(R.drawable::class.java.getField(data.photo).getInt(null)).into(binding.ivProfilePic)
            binding.clRoot.setOnClickListener { onItemClicked(position) }
            binding.tvUsername.text = data.username
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemStoryGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }
}