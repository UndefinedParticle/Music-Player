package com.chinmoy09ine.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.chinmoy09ine.musicplayer.databinding.ActivityMusicBinding
import java.io.IOException

class MusicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicBinding
    private lateinit var musicList: ArrayList<MusicModel>
    private lateinit var currentSong: MusicModel
    private var isPlaying: Boolean = true
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        changeStatusBarColor()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_music)
        binding.lifecycleOwner = this

        mediaPlayer = MyMediaPlayer.getInstance()
        musicList = intent.getSerializableExtra("songsList") as ArrayList<MusicModel>

        setMusic()

        binding.previousButton.setOnClickListener {
            playPreviousSong()
        }

        binding.playPauseButton.setOnClickListener {
            playOrPauseSong()
        }

        binding.nextButton.setOnClickListener {
            playNextSong()
        }


        // SeekBar change listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update music position only if changed by user
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    updateSeekBar()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


    }

    private fun setMusic(){

        currentSong = musicList[MyMediaPlayer.currentIndex]

        binding.songName.text = currentSong.title
        binding.songName.isSelected = true
        binding.playIcon.visibility = View.GONE
        binding.pauseIcon.visibility = View.VISIBLE
        isPlaying = true
        binding.totalTime.text = formatTime(currentSong.duration.toLong())

        Log.d("currentSong", "title: ${currentSong.title} pos: ${MyMediaPlayer.currentIndex}")
        playMusic()
    }

    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            binding.seekBar.progress = mediaPlayer.currentPosition
            binding.currentTime.text = formatTime(mediaPlayer.currentPosition.toLong())
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    private fun playPreviousSong(){

        if(MyMediaPlayer.currentIndex == 0){
            return
        }
        MyMediaPlayer.currentIndex -= 1

        setMusic()

    }

    private fun playMusic(){
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(currentSong.path)
            mediaPlayer.prepare()
            mediaPlayer.start()

            binding.seekBar.progress = 0
            binding.seekBar.max = currentSong.duration.toInt()
            binding.animationView.playAnimation()

            updateSeekBar()

        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    private fun playOrPauseSong(){

        if(isPlaying){
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
            }
            binding.animationView.pauseAnimation()

            binding.playIcon.visibility = View.VISIBLE
            binding.pauseIcon.visibility = View.GONE

        }else{
            binding.playIcon.visibility = View.GONE
            binding.pauseIcon.visibility = View.VISIBLE

            try {

                mediaPlayer.start()
                binding.animationView.resumeAnimation()

                updateSeekBar()

            }catch (e:IOException){
                e.printStackTrace()
            }
        }

        isPlaying = !isPlaying
    }


    private fun playNextSong(){

        if(MyMediaPlayer.currentIndex == musicList.size - 1){
            return
        }
        MyMediaPlayer.currentIndex += 1

        setMusic()
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return "%02d:%02d".format(minutes, remainingSeconds)
    }

    private fun changeStatusBarColor() {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.backgroundColor1)
    }

}