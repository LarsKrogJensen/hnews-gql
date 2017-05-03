
import io.vertx.core.Vertx
import se.lars.hnews.api.HackerNewsApi

fun main(args: Array<String>) {
    val vertx = Vertx.vertx();
    val api = HackerNewsApi(vertx);

    api.topStories().whenComplete { r, t ->
        if (t != null)
            t.printStackTrace()
        else {
            r.forEach {
                println("Id: " + it)
            }
           api.story(r[0]).whenComplete{r2, t2 ->
               println("r2: " + r2)
            }
        }
    }
}