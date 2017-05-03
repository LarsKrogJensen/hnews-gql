package se.lars.kutil



import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun jsonObject(vararg fields: Pair<String, Any?>): JsonObject = JsonObject(mapOf(*fields))
fun jsonObject(fields: Iterable<Pair<String, Any?>>): JsonObject = JsonObject(mapOf(*fields.toList().toTypedArray()))
fun jsonArray(vararg values: Any?): JsonArray = JsonArray(listOf(*values))
fun jsonArray(values: Iterable<Any?>): JsonArray = JsonArray(listOf(*values.toList().toTypedArray()))

object Json

fun Json.object_(vararg fields: Pair<String, Any?>): JsonObject = jsonObject(*fields)
fun Json.object_(fields: Iterable<Pair<String, Any?>>): JsonObject = jsonObject(*fields.toList().toTypedArray())
fun Json.object_(fields: Map<String, Any?>): JsonObject = JsonObject(fields)
fun Json.array_(vararg values: Any?): JsonArray = jsonArray(*values)
fun Json.array_(values: Iterable<Any?>): JsonArray = jsonArray(*values.toList().toTypedArray())
fun Json.array_(values: List<Any?>): JsonArray = JsonArray(values)

inline fun <T> json_(block: Json.() -> T): T = Json.block()

//@Suppress("UNCHECKED_CAST") fun <T> JsonObject.get(key: String): T = getValue(key) as T

fun JsonObject.resolveInt(path: String) : Int? {
    if (this.containsKey(path))
        return this.getInteger(path)

    return traversePath(this, path.split("."))
}

fun JsonObject.resolveBool(path: String) : Boolean? {
    if (this.containsKey(path))
        return this.getBoolean(path)

    return traversePath(this, path.split("."))
}

private fun <T :Any> traversePath(jsonObject: JsonObject, path: List<String>): T? {
    if (path.size > 1) {
        return traversePath<T>(jsonObject.getJsonObject(path[0]), path.drop(1))
    }

    @Suppress("UNCHECKED_CAST")
    if (jsonObject.containsKey(path[0])) {
        return jsonObject.getValue(path[0]) as T
    }

    return null
}
