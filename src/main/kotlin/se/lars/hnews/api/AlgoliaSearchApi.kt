package se.lars.hnews.api

import com.fasterxml.jackson.annotation.JsonProperty
import io.vertx.core.Vertx
import se.lars.hnews.types.Story
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class AlgoliaSearchApi @Inject constructor(
    vertx: Vertx
) : HttpClientBaseApi(vertx, "hn.algolia.com"), ISearchApi {

    override fun search(query: String): CompletableFuture<List<Story>> {
        val thenApply: CompletableFuture<List<Story>> = invokeQuery<AlgoliaSearchResult>("/api/v1/search_by_date?query=$query&tags=story")
            .thenApply { (hits) ->
                hits
            }

        return thenApply;
    }
}

data class AlgoliaSearchResult
(
    val hits: List<AlgoliaStory>
)

data class AlgoliaStory(
    @JsonProperty("objectID")
    override val id: String,
    @JsonProperty("author")
    override val by: String,
    @JsonProperty("num_comments")
    override val descendants: Int,
    @JsonProperty("points")
    override val score: Int,
    @JsonProperty("created_at_i")
    override val timeEpoc: Long,
    @JsonProperty("title")
    override val title: String,
    override val text: String?,
    override val url: String?,
    override val comments: List<Int>?
) : Story 