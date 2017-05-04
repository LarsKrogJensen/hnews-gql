package se.lars.hnews.api

import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import java.util.concurrent.CompletableFuture

interface IHackerNewsApi {
    fun topStories(): CompletableFuture<List<Int>>
    fun story(id: Int) : CompletableFuture<Story>
    fun comment(id: Int): CompletableFuture<Comment>
    fun user(id: String): CompletableFuture<User>
}