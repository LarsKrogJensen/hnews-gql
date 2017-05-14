package se.lars.hnews.services

enum class StoryType(val query: String) {
    Top ("topstories"),
    New("newstories"),
    Best("beststories"),
    Show("showstories"),
    Ask("askstories"),
    Job("jobstories"),

}