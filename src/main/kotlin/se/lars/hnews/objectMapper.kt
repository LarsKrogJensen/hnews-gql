package se.lars.hnews

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import se.lars.hnews.types.DefaultStory
import se.lars.hnews.types.Story


val defaultMapper = ObjectMapper().apply {
    registerModule(Jdk8Module())
    registerModule(KotlinModule())

    val module = SimpleModule("CustomModel", Version.unknownVersion())
    val resolver = SimpleAbstractTypeResolver()
    resolver.addMapping(Story::class.java, DefaultStory::class.java)
    module.setAbstractTypes(resolver)
    registerModule(module)

    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}