package at.shockbytes.plugin.service.dependency.model.api

import at.shockbytes.plugin.service.dependency.model.GradleDependency

class MavenCentralDependency : GradleDependencyConverter {

    var response: Response? = null

    override fun asGradleDependency(title: String, statement: String, query: String): GradleDependency {
        val item = response?.docs?.get(0)
        return GradleDependency(title, statement, item?.latestVersion
                ?: "", query, GradleDependency.EndPoint.MAVEN_CENTRAL)
    }

    class Response {

        var docs: List<ResponseItem>? = null

        class ResponseItem(val id: String, val g: String, val a: String, val latestVersion: String)

    }

}