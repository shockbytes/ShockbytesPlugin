package at.shockbytes.plugin.service.dependency.model

import at.shockbytes.plugin.util.Jsonizable
import com.google.gson.JsonObject

data class GradleDependency(var title: String, var statement: String, var version: String,
                            var query: String, var endPoint: EndPoint): Jsonizable {

    enum class EndPoint {
        MAVEN_CENTRAL, JITPACK_IO, GOOGLE, CLOJARS
    }

    val qualifiedTitle: String
        get() = "$title $version"

    val qualifiedStatement: String
        get() = statement.replace("_VERSION_", version)

    override fun toJsonObject(): JsonObject {

        val json = JsonObject()
        json.addProperty("title", title)
        json.addProperty("statement", statement)
        json.addProperty("version", version)
        json.addProperty("query", query)
        json.addProperty("endpoint", endPoint.name.toLowerCase())
        return json
    }

}