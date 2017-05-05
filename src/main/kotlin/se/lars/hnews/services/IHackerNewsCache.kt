package se.lars.hnews.services

import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import java.util.concurrent.CompletableFuture

interface IHackerNewsCache {
    fun topStories(loader: () -> CompletableFuture<List<Int>>) : CompletableFuture<List<Int>>
    fun story(id: Int, loader: (Int) -> CompletableFuture<Story>) : CompletableFuture<Story>
    fun comment(id: Int, loader: (Int) -> CompletableFuture<Comment>) : CompletableFuture<Comment>
}