package com.chinmoy09ine.musicplayer

import java.io.Serializable


data class MusicModel(
    val id: Long,
    val title: String,
    val artist: String,
    val path: String,
    val duration: String
): Serializable