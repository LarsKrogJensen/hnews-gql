package se.lars.hnews.types

import com.fasterxml.jackson.annotation.JsonProperty

data class Comment(
    val id: String,
    @JsonProperty("parent")
    val parentId: String,
    @JsonProperty("time")
    val timeEpoc: Long,
    val text: String?,
    val by: String?,
    @JsonProperty("kids")
    val comments: List<Int>?
)