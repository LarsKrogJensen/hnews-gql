package se.lars.hnews.types

import com.fasterxml.jackson.annotation.JsonProperty

data class DefaultStory(
    override val id: String,
    override val by: String,
    override val descendants: Int,
    override val score: Int,
    @JsonProperty("time")
    override val timeEpoc: Long,
    override val title: String?,
    override val text: String?,
    override val url: String?,
    @JsonProperty("kids")
    override val comments: List<Int>? = emptyList()
) : Story