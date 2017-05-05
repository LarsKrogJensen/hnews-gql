package se.lars.hnews.services

import se.lars.hnews.api.IHackerNewsApi
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.awaitAll
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsService
@Inject constructor(
    private val api: IHackerNewsApi,
    private val cache: IHackerNewsCache
) : IHackerNewsService {

    override fun topStories(first: Int): CompletableFuture<List<Story>> {
        return cache.topStories(api::topStories)
            .thenCompose { storyIds -> storyIds.take(first).map { id -> story(id) }.awaitAll() }
    }

    override fun comments(ids: List<Int>): CompletableFuture<List<Comment>> {
        return ids.map { comment(it) }.awaitAll()
    }

    override fun user(id: String): CompletableFuture<User> {
        return api.user(id)
    }

    private fun story(id: Int): CompletableFuture<Story> {
        return cache.story(id, api::story)
    }

    private fun comment(id: Int): CompletableFuture<Comment> {
        return cache.comment(id, api::comment)
    }
}