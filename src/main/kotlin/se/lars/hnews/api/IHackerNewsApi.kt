package se.lars.hnews.api

import se.lars.hnews.types.Story
import java.util.concurrent.CompletableFuture

interface IHackerNewsApi {
    fun topStories(): CompletableFuture<List<Int>>
    fun story(id: Int) : CompletableFuture<Story>
}