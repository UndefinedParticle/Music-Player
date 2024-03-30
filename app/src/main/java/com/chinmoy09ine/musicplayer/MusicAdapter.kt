package com.chinmoy09ine.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.chinmoy09ine.musicplayer.databinding.MusicItemBinding

class MusicAdapter(private val myContext: Context, private val list: ArrayList<MusicModel>): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var music: MusicModel? = null

    inner class MusicViewHolder(var binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root){



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding: MusicItemBinding = DataBindingUtil.inflate(LayoutInflater.from(myContext), R.layout.music_item, parent, false)
        return MusicViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        music = list[position]

        val isCurrentlyPlaying = (MyMediaPlayer.getInstance().isPlaying && MyMediaPlayer.currentIndex == position)

        holder.binding.songName.text = music!!.title
        holder.binding.songName.isSelected = true

        if (isCurrentlyPlaying) {
            holder.binding.animationView.visibility = View.VISIBLE
            holder.binding.songName.setTextColor(myContext.resources.getColor(R.color.backgroundColor3))
        } else {
            holder.binding.animationView.visibility = View.GONE
            holder.binding.songName.setTextColor(myContext.resources.getColor(R.color.black))
        }

        holder.binding.musicLayout.setOnClickListener {

            MyMediaPlayer.getInstance().reset()
            MyMediaPlayer.currentIndex = position

            myContext.startActivity(Intent(myContext, MusicActivity::class.java)
                .putExtra("songsList", list)
                .putExtra("currentPosition", position)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

        }

    }


}