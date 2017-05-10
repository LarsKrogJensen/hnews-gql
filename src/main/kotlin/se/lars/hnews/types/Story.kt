package se.lars.hnews.types

import com.fasterxml.jackson.annotation.JsonProperty

data class Story(
    val id: String,
    val by: String,
    val descendants: Int,
    val score: Int,
    @JsonProperty("time")
    val timeEpoc: Long,
    val title: String?,
    val text: String?,
    val url: String?,
    @JsonProperty("kids")
    val comments: List<Int>? = emptyList()
)