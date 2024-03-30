package com.chinmoy09ine.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.chinmoy09ine.musicplayer.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicList: ArrayList<MusicModel>
    private lateinit var musicAdapter: MusicAdapter
    private var isPlaying: Boolean = true
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var rotation: Int = 0

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001 // Any unique code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        mediaPlayer = MyMediaPlayer.getInstance()
        musicList = ArrayList()
        musicAdapter = MusicAdapter(this, musicList)
        binding.recyclerView.adapter = musicAdapter

        loadMusic()

        binding.playPauseButton.setOnClickListener {
            playOrPauseSong()
        }

    }

    private fun loadMusic(){

        if (!checkPermission()) {
            requestPermission()
        }else {
            //Toast.makeText(this@MainActivity, "Permission granted!", Toast.LENGTH_SHORT).show()

            fetchMusic()
        }

    }

    private fun checkPermission(): Boolean {

        var permissionStatus = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionStatus = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO)
        }

        // Check if permission is granted or not
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        if(ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(this@MainActivity, "PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS!", Toast.LENGTH_SHORT).show()
        }else {
            Log.d("storagePermission", "Requested!")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }else{
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchMusic()
            } else {
                
                Toast.makeText(
                    this,
                    "Permission Denied! Please grant permission to access storage.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun fetchMusic() {
        musicList.clear()

        val musicProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor = contentResolver.query(
            musicUri,
            musicProjection,
            selection,
            null,
            null
        )

        cursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val data = cursor.getString(dataColumn)
                val duration = cursor.getString(durationColumn)

                Log.d("storagePermission", "title: $title")

                // You can add the retrieved music information to your list or perform any other operation
                val songData = MusicModel(id, title, artist, data, duration)
                if(File(songData.path).exists()) {
                    musicList.add(songData)
                }

            }

            musicAdapter.notifyDataSetChanged()
        }

        // Now musicList contains all music files from the storage
    }
    private fun changeStatusBarColor() {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.backgroundColor1)
    }

    private fun playOrPauseSong(){

        if(isPlaying){
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
            }

            binding.playIcon.visibility = View.VISIBLE
            binding.pauseIcon.visibility = View.GONE

        }else{
            binding.playIcon.visibility = View.GONE
            binding.pauseIcon.visibility = View.VISIBLE

            try {

                mediaPlayer.start()


            }catch (e: IOException){
                e.printStackTrace()
            }
        }

        rotateImage()
        isPlaying = !isPlaying
    }

    private fun rotateImage() {
        if (mediaPlayer.isPlaying) {
            binding.profileImage.rotation = rotation.toFloat()
            rotation++
        }else{
            binding.profileImage.rotation = 0F
            rotation = 0
        }

        handler.postDelayed({ rotateImage() }, 100)

    }


    override fun onStart() {
        super.onStart()
        loadMusic()

        if(MyMediaPlayer.currentIndex != -1){
            binding.musicLayout.visibility = View.VISIBLE
            val currentSong = musicList[MyMediaPlayer.currentIndex]

            binding.songName.text = currentSong.title
            binding.songName.isSelected = true

            if(MyMediaPlayer.getInstance().isPlaying){
                binding.playIcon.visibility = View.GONE
                binding.pauseIcon.visibility = View.VISIBLE
            }else{
                binding.playIcon.visibility = View.VISIBLE
                binding.pauseIcon.visibility = View.GONE
            }

            rotateImage()
        }else{
            binding.musicLayout.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        finish()
    }

}