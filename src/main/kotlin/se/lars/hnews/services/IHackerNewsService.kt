package se.lars.hnews.services

import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import java.util.concurrent.CompletableFuture

interface IHackerNewsService {
    fun stories(type: StoryType, first: Int): CompletableFuture<List<Story>>
    fun story(id: Int): CompletableFuture<Story>
    fun comments(ids: List<Int>): CompletableFuture<List<Comment>>
    fun user(id: String): CompletableFuture<User>
    fun search(query: String): CompletableFuture<List<Story>>
}