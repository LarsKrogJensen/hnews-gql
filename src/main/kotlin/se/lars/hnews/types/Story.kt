package se.lars.hnews.types

interface Story {
    val id: String
    val by: String
    val descendants: Int
    val score: Int
    val timeEpoc: Long
    val title: String?
    val text: String?
    val url: String?
    val comments: List<Int>?
}

