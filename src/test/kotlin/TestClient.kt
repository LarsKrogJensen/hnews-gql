
import io.vertx.core.Vertx
import se.lars.hnews.api.HackerNewsApi
import se.lars.hnews.services.StoryType

fun main(args: Array<String>) {
    val vertx = Vertx.vertx();
    val api = HackerNewsApi(vertx);

    api.stories(StoryType.Top).whenComplete { r, t ->
        if (t != null)
            t.printStackTrace()
        else {
            r.forEach {
                println("Id: " + it)
            }
           api.story(r[0]).whenComplete{ r2, _ ->
               println("r2: " + r2)
            }
        }
    }
}