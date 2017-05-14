package se.lars.hnews.api

import se.lars.hnews.types.Story
import java.util.concurrent.CompletableFuture

interface ISearchApi {
    fun search(query: String): CompletableFuture<List<Story>>
}