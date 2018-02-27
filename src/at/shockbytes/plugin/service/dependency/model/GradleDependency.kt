package at.shockbytes.plugin.service.dependency.model

data class GradleDependency(var title: String, var statement: String, var version: String,
                            var query: String, var endPoint: EndPoint) {

    enum class EndPoint {
        MAVEN_CENTRAL, JITPACK_IO, GOOGLE, CLOJARS
    }

    val qualifiedTitle: String
        get() = "$title $version"

    val qualifiedStatement: String
        get() = statement.replace("_VERSION_", version)

}