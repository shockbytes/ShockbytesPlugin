package at.shockbytes.plugin.service.dependency.model.api

import at.shockbytes.plugin.service.dependency.model.GradleDependency

interface GradleDependencyConverter {

    fun asGradleDependency(title: String, statement: String, query: String): GradleDependency
}