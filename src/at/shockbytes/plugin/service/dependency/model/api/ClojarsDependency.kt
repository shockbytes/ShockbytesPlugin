package at.shockbytes.plugin.service.dependency.model.api

import at.shockbytes.plugin.service.dependency.model.GradleDependency

class ClojarsDependency(private var latest_release: String) : GradleDependencyConverter {

    override fun asGradleDependency(title: String, statement: String, query: String): GradleDependency {
        return GradleDependency(title, statement, latest_release, query, GradleDependency.EndPoint.CLOJARS)
    }

}