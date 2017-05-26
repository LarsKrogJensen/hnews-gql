package se.lars.hnews.services

import reactor.core.publisher.Flux
import se.lars.hnews.api.IHackerNewsApi
import se.lars.hnews.api.ISearchApi
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.loggerFor
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsService
@Inject constructor(
    private val api: IHackerNewsApi,
    private val searchApi: ISearchApi,
    private val cache: IHackerNewsCache
) : IHackerNewsService {

    private val log = loggerFor<HackerNewsService>()
    private val timeSource = Flux.interval(Duration.ofMillis(1000)).map { LocalDateTime.now() }.doOnSubscribe {
        log.info("Time source onSubscribe")
    }.doOnCancel {
        log.info("Time source onCancel")
    }

    override fun stories(type: StoryType, first: Int): CompletableFuture<List<Story>> {
        return cache.stories(type, api::stories)
            .thenCompose { storyIds -> cache.stories(storyIds.take(first), api::story) }
    }

    override fun search(query: String) = searchApi.search(query)

    override fun comments(ids: List<Int>): CompletableFuture<List<Comment>> {
        return cache.comments(ids, api::comment)
    }

    override fun time(): Flux<LocalDateTime> {
        return timeSource
    }

    override fun user(id: String): CompletableFuture<User> {
        return cache.user(id, api::user)
    }

    override fun story(id: Int): CompletableFuture<Story> {
        return cache.story(id, api::story)
    }

    private fun comment(id: Int): CompletableFuture<Comment> {
        return cache.comment(id, api::comment)
    }
}