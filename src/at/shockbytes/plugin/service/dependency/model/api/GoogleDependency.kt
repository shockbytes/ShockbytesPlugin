package at.shockbytes.plugin.service.dependency.model.api

import at.shockbytes.plugin.service.dependency.model.GradleDependency

class GoogleDependency(private var version: String) : GradleDependencyConverter {

    override fun asGradleDependency(title: String, statement: String, query: String): GradleDependency {
        return GradleDependency(title, statement, version, query, GradleDependency.EndPoint.GOOGLE)
    }

}