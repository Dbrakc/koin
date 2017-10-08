package org.koin

import org.koin.core.bean.BeanRegistry
import org.koin.core.instance.InstanceResolver
import org.koin.core.property.PropertyResolver
import org.koin.dsl.context.Context
import org.koin.dsl.module.Module

/**
 * Koin Context Builder
 * @author - Arnaud GIULIANI
 */
class Koin {
    val beanRegistry = BeanRegistry()
    val propertyResolver = PropertyResolver()
    val instanceResolver = InstanceResolver()

    /**
     * Inject properties to context
     */
    fun properties(props: Map<String, Any>): Koin {
        propertyResolver.addAll(props)
        return this
    }

    /**
     * load given list of module instances into current koin context
     */
    fun <T : Module> build(modules: List<T>): KoinContext {
        val koinContext = KoinContext(beanRegistry, propertyResolver, instanceResolver)
        modules.forEach { module ->
            module.koinContext = koinContext
            val context = module.context()
            registerDefinitions(context)
        }
        return koinContext
    }

    /**
     * Register context definitions & subContexts
     */
    private fun registerDefinitions(context: Context) {
        val scopeClass = context.scope

        // Create or reuse scope context
        val scope = instanceResolver.findOrCreateScope(scopeClass, context.parentScope)

        // Add definitions
        context.definitions.forEach { definition -> beanRegistry.declare(definition, scope) }

        // Check sub contexts
        context.subContexts.forEach { subContext -> registerDefinitions(subContext) }
    }

    /**
     * load given module instances into current koin context
     */
    fun <T : Module> build(vararg modules: T): KoinContext = build(modules.asList())

    /**
     * load directly Koin context with no modules
     */
    fun build(): KoinContext = KoinContext(beanRegistry, propertyResolver, instanceResolver)
}