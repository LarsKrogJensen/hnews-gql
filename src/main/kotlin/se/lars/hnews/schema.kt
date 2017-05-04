package se.lars.hnews

import graphql.GraphQLStringNonNull
import graphql.schema.*
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.asCompleted


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
    field<String> {
        name = "url"
        fetcher { env -> env.source<Story>().text.asCompleted() }
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
    field<String> {
        name = "url"
        fetcher { env -> env.source<Story>().text.asCompleted() }
    }
    field<List<Comment>> {
        name = "comments"
        type = GraphQLList(commentType)
        fetcher { env ->
            val commentIds = env.source<Story>().comments
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
    fetcher = { env ->
        env.context<RequestContext>().hackerNews.topStories()
    }
}

val hackeNewsSchema = newSchema {
    query = newObject {
        name = "QueryType"
        fields += topStoriesQuery
    }
}
