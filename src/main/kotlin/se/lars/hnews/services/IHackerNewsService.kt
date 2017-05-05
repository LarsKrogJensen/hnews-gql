package se.lars.hnews.services

import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import java.util.concurrent.CompletableFuture

interface IHackerNewsService {
    fun topStories(first: Int): CompletableFuture<List<Story>>
    fun comments(ids: List<Int>): CompletableFuture<List<Comment>>
    fun user(id: String): CompletableFuture<User>
}