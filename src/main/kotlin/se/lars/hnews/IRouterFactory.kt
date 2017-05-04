package se.lars.hnews

import io.vertx.ext.web.Router

interface IRouterFactory {
    fun router() : Router
}