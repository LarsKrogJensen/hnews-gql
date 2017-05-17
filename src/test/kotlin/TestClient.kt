import io.vertx.core.Vertx
import se.lars.sse.EventSourceStream

fun main(args: Array<String>) {
    val vertx = Vertx.vertx();
//    val api = HackerNewsApi(vertx);
//
//    api.stories(StoryType.Top).whenComplete { r, t ->
//        if (t != null)
//            t.printStackTrace()
//        else {
//            r.forEach {
//                println("Id: " + it)
//            }
//           api.story(r[0]).whenComplete{ r2, _ ->
//               println("r2: " + r2)
//            }
//        }
//    }

//    val options = HttpClientOptions().apply {
//        defaultHost = "hacker-news.firebaseio.com"
//        defaultPort = 443
//        isSsl = true
//    }
//
//    val mapper = defaultMapper
//    val source = IEventSource.create(vertx, options)
//    source.onClose {
//        println("On Close")
//    }
//
//    source.onMessage {
//        println("PUT: ---------------------")
//        println(it)
//        if (it.event == "put") {
//            handleStories(mapper.readValue(it.data, StoryUpdateEvent::class.java))
//        }
//        println()
//
//    }
//
//    source.connect("/v0/topstories.json?sse=true") {
////    source.connect("/v0/updates.json?sse=true") {
//        println("On Connect")
//    }

    EventSourceStream(vertx, host = "hacker-news.firebaseio.com")
        .connect("/v0/topstories.json?sse=true")
        .filter { it.event == "put" }
        .subscribe({ event ->
                       println(event)
                   },
                   { ex ->
                       println(ex)
                   },
                   {
                       println("completed")
                   })
}

fun handleStories(updateEvent: StoryUpdateEvent) {
    println(updateEvent)
}

data class StoryUpdateEvent(
    val path: String,
    val data: List<String>
)