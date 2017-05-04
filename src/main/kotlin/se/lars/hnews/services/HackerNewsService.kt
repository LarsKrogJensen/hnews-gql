package se.lars.hnews.services

import se.lars.hnews.api.IHackerNewsApi
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.awaitAll
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsService @Inject constructor(private val api: IHackerNewsApi) : IHackerNewsService {
    override fun topStories(): CompletableFuture<List<Story>> {
        val promise = CompletableFuture<List<Story>>()
        return api.topStories().thenApply { storyIds -> storyIds.map { id -> story(id) } }
                .whenComplete { result, ex ->
                    result.awaitAll()
                }
        return promise
    }

    override fun comments(ids: List<Int>): CompletableFuture<List<Comment>> {
        return ids.map { comment(it) }.awaitAll()
    }

    override fun user(id: String): CompletableFuture<User> {
        return api.user(id)
    }

    private fun story(id: Int): CompletableFuture<Story> {
        return api.story(id)
    }

    private fun comment(id: Int): CompletableFuture<Comment> {
        return api.comment(id)
    }
}