package se.lars.guice

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder

abstract class ModuleBase : AbstractModule() {

    protected inline fun <reified T : Any> bind(): AnnotatedBindingBuilder<T> {
        return this.bind(T::class.java)!!
    }

    inline fun <reified T : Any> AnnotatedBindingBuilder<in T>.to() = to(T::class.java)!!
    fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)
}

