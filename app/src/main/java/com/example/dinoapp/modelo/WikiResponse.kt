package com.example.dinoapp.modelo

data class WikiResponse(
    val title: String,
    val extract: String,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String
)