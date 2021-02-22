package com.example.mediaplayer.models

class Song constructor(private var title: String, private var artist: String) {
    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getArtist(): String {
        return artist
    }

    fun setArtist(artist: String) {
        this.artist = artist
    }
}