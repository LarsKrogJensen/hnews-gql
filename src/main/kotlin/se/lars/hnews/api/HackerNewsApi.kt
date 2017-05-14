package se.lars.hnews.api

import io.vertx.core.Vertx
import se.lars.hnews.services.StoryType
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsApi
@Inject constructor(
    vertx: Vertx
) : HttpClientBaseApi(vertx, "hacker-news.firebaseio.com"), IHackerNewsApi {

    override fun stories(type: StoryType): CompletableFuture<List<Int>> {
        return invokeQuery("/v0/${type.query}.json")
    }

    override fun story(id: Int): CompletableFuture<Story> {
        return invokeQuery("/v0/item/$id.json")
    }

    override fun comment(id: Int): CompletableFuture<Comment> {
        return invokeQuery("/v0/item/$id.json")
    }

    override fun user(id: String): CompletableFuture<User> {
        return invokeQuery("/v0/user/$id.json")
    }

}