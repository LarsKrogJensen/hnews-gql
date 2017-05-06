package se.lars.hnews.services

import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.kutil.asCompleted
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class HeapHackerNewsCache : IHackerNewsCache {
    private val stories: ConcurrentHashMap<Int, Story> = ConcurrentHashMap()
    private val comments: ConcurrentHashMap<Int, Comment> = ConcurrentHashMap()

    override fun story(id: Int, loader: (Int) -> CompletableFuture<Story>): CompletableFuture<Story> {
        val story = stories[id]
        if (story != null)
            return story.asCompleted()

        val loader1 = loader(id)
        loader1.thenAccept{ stories.put(id, it)}

        return loader1
    }

    override fun stories(ids: List<Int>, loader: (Int) -> CompletableFuture<Story>): CompletableFuture<List<Story>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun comments(ids: List<Int>, loader: (Int) -> CompletableFuture<Comment>): CompletableFuture<List<Comment>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun topStories(loader: () -> CompletableFuture<List<Int>>): CompletableFuture<List<Int>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun comment(id: Int, loader: (Int) -> CompletableFuture<Comment>): CompletableFuture<Comment> {
        return comments[id].asCompleted()
    }
}