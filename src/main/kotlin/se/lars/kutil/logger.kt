package se.lars.kutil

import org.slf4j.LoggerFactory

inline fun <reified T: Any> loggerFor() = LoggerFactory.getLogger(T::class.java)!!