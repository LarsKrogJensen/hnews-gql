package se.lars.hnews

import graphql.GraphQLError
import graphql.execution.instrumentation.NoOpInstrumentation
import graphql.newGraphQL
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import se.lars.hnews.services.IHackerNewsService
import se.lars.kutil.jsonObject
import se.lars.kutil.loggerFor
import se.lars.kutil.thenOn
import java.nio.charset.Charset

abstract class GraphQLHandlerBase(val hackerNews: IHackerNewsService) : Handler<RoutingContext> {

    val log = loggerFor<GraphQLHandlerBase>()

    protected fun executeGraphQL(jsonText: String, handler: (JsonObject) -> Unit): Unit {
        log.info("Query: \n" + jsonText)
        log.info("Charset: ${Charset.defaultCharset().name()}")
        // be a bit more forgiving
        val body = jsonText.replace('\n', ' ').replace('\t', ' ')

        // Validate json input
        val json: JsonObject = try {
            JsonObject(body)
        } catch(e: Exception) {
            handler(jsonObject("errors" to "Invalid Json format"))
            return
        }


        val graphQL = newGraphQL {
            schema = hackeNewsSchema
            instrumentation = NoOpInstrumentation.INSTANCE
        }

        val variables = json.getValue("variables").let {
            when (it) {
                is JsonObject -> it.map
                else          -> emptyMap<String, Any>()
            }
        }

        val query = json.getString("query")
        val operation: String? = json.getString("operationName")

        val context = RequestContext(hackerNews)

        graphQL.execute(query, operation, context, variables)
            .thenOn(Vertx.currentContext())
            .thenAccept { result ->
                val jsonResponse = if (result.succeeded()) {
                    jsonObject("data" to result.data())
                } else {
                    jsonObject("errors" to deccodeErrors(result.errors))
                }
                handler(jsonResponse)
            }
            .exceptionally {
                log.error("Execution error", it)
                handler(jsonObject("errors" to "Internal Server Error"))
                null
            }
    }

    fun deccodeErrors(errors: List<GraphQLError>): Any? {
        return errors
    }
}