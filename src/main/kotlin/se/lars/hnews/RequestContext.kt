package se.lars.hnews

import se.lars.hnews.services.IHackerNewsService

class RequestContext(
        val hackerNews: IHackerNewsService
)