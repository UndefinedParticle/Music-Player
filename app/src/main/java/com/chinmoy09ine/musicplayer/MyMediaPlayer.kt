package com.chinmoy09ine.musicplayer

import android.media.MediaPlayer

object MyMediaPlayer {
    public var mediaPlayer: MediaPlayer? = null
    public var currentIndex: Int = -1

    fun getInstance(): MediaPlayer {
        return mediaPlayer ?: synchronized(this) {
            mediaPlayer ?: MediaPlayer().also { mediaPlayer = it }
        }
    }
}
