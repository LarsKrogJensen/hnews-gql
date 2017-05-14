package se.lars.hnews

import graphql.GraphQLInt
import graphql.GraphQLStringNonNull
import graphql.schema.*
import se.lars.hnews.services.StoryType
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.asCompleted
import se.lars.kutil.succeeded


private val userType = newObject {
    name = "User"
    field<String> {
        name = "id"
        type = GraphQLStringNonNull
        fetcher { env -> env.source<User>().id.asCompleted() }
    }
    field<String> {
        name = "about"
        fetcher { env -> env.source<User>().about.asCompleted() }
    }
}

private val commentType = newObject {
    name = "Comment"
    field<String> {
        name = "id"
        type = GraphQLStringNonNull
        fetcher { env -> env.source<Comment>().id.asCompleted() }
    }
    field<String> {
        name = "text"
        fetcher { env -> env.source<Comment>().text.asCompleted() }
    }
    field<Long> {
        name = "time"
        fetcher { env -> env.source<Comment>().timeEpoc.asCompleted() }
    }
    field<List<Comment>> {
        name = "comments"
        type = GraphQLList(GraphQLTypeReference("Comment"))
        fetcher { env ->
            val commentIds = env.source<Comment>().comments ?: emptyList()
            env.context<RequestContext>().hackerNews.comments(commentIds)
        }
    }
    field<User> {
        name = "by"
        type = userType
        fetcher { env ->
            val comment = env.source<Comment>()
            if (comment.by != null)
                env.context<RequestContext>().hackerNews.user(comment.by)
            else
                succeeded(null)
        }
    }
}

private val storyType = newObject {
    name = "Story"
    field<String> {
        name = "id"
        type = GraphQLStringNonNull
        fetcher { env -> env.source<Story>().id.asCompleted() }
    }
    field<String> {
        name = "title"
        fetcher { env -> env.source<Story>().title.asCompleted() }
    }
    field<String> {
        name = "text"
        fetcher { env -> env.source<Story>().text.asCompleted() }
    }
    field<Long> {
        name = "time"
        fetcher { env -> env.source<Story>().timeEpoc.asCompleted() }
    }
    field<String> {
        name = "url"
        fetcher { env -> env.source<Story>().url.asCompleted() }
    }
    field<Int> {
        name = "score"
        fetcher { env -> env.source<Story>().score.asCompleted() }
    }
    field<Int> {
        name = "descendants"
        fetcher { env -> env.source<Story>().descendants.asCompleted() }
    }
    field<List<Comment>> {
        name = "comments"
        type = GraphQLList(commentType)
        fetcher { env ->
            val commentIds = env.source<Story>().comments ?: emptyList()
            env.context<RequestContext>().hackerNews.comments(commentIds)
        }
    }
    field<User> {
        name = "by"
        type = GraphQLNonNull(userType)
        fetcher { env ->
            env.context<RequestContext>().hackerNews.user(env.source<Story>().by)
        }
    }
}

private val topStoriesQuery = newField<List<Story>> {
    name = "topStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of top stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.Top, env.argument<Int>("first")!!)
    }
}

private val bestStoriesQuery = newField<List<Story>> {
    name = "bestStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of best stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.Best, env.argument<Int>("first")!!)
    }
}

private val newStoriesQuery = newField<List<Story>> {
    name = "newStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of new stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.New, env.argument<Int>("first")!!)
    }
}

private val askStoriesQuery = newField<List<Story>> {
    name = "askStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of ask stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.Ask, env.argument<Int>("first")!!)
    }
}


private val showStoriesQuery = newField<List<Story>> {
    name = "showStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of show stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.Show, env.argument<Int>("first")!!)
    }
}


private val jobStoriesQuery = newField<List<Story>> {
    name = "jobStories"
    type = GraphQLList(storyType)
    argument {
        name = "first"
        description = "Show the first number of job stories"
        type = GraphQLInt
        defaultValue = 10
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.stories(StoryType.Job, env.argument<Int>("first")!!)
    }
}

private val searchQuery = newField<List<Story>> {
    name = "search"
    type = GraphQLList(storyType)
    argument {
        name = "query"
        description = "Search query"
        type = GraphQLStringNonNull
    }
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.search(env.argument<String>("query")!!)
    }
}


private val storyQuery = newField<Story> {
    name = "story"
    type = storyType
    argument {
        name = "id"
        description = "Story identifier"
        type = GraphQLStringNonNull
    }
    fetcher = { env ->
        val id = env.argument<String>("id")!!.toInt()
        env.context<RequestContext>().hackerNews.story(id)
    }
}

val hackeNewsSchema = newSchema {
    query = newObject {
        name = "QueryType"
        fields += topStoriesQuery
        fields += newStoriesQuery
        fields += bestStoriesQuery
        fields += askStoriesQuery
        fields += showStoriesQuery
        fields += jobStoriesQuery
        fields += storyQuery
        fields += searchQuery
    }
}
