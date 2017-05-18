package se.lars.hnews.services

import io.vertx.core.Vertx
import reactor.core.Disposable
import reactor.core.publisher.Flux
import se.lars.hnews.defaultMapper
import se.lars.kutil.loggerFor
import se.lars.sse.EventSourceStream
import javax.inject.Inject

class HackerNewsStreamService
@Inject
constructor(
    vertx: Vertx,
    val cache: IHackerNewsCache
) : IHackerNewsStreamService {
    private val eventSource: EventSourceStream = EventSourceStream(vertx, "hacker-news.firebaseio.com")
    private val log = loggerFor<HackerNewsStreamService>()
    private val mapper = defaultMapper
    private val streams: Map<StoryType, Flux<StoryUpdateEvent>>
    private val feedSynchronizers: List<Disposable>

    init {
        streams = StoryType.values().map { storyType ->
            storyType to this.createStream(storyType)
        }.toMap()

        feedSynchronizers = streams.entries.map { (storyType, stream) ->
            stream.subscribe({ event -> cache.updateStories(storyType, event.data) },
                             { ex -> log.error("Stream $storyType failed.", ex) },
                             { log.info("Stream $storyType completed") })
        }.toList()
    }

    fun createStream(storyType: StoryType): Flux<StoryUpdateEvent> {
        return eventSource.connect("/v0/${storyType.query}.json?sse=true")
            .filter { it.event == "put" }
            .map { mapper.readValue(it.data, StoryUpdateEvent::class.java) }
    }
}

data class StoryUpdateEvent(
    val path: String,
    val data: List<Int>
)