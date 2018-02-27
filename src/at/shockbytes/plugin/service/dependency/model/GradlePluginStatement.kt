package at.shockbytes.plugin.service.dependency.model

/**
 * Author:  Mescht
 * Date:    21.02.2017
 */
class GradlePluginStatement(var title: String, var statement: String, var version: String,
                            var isApplyTop: Boolean, var applyName: String) {

    val qualifiedStatement: String
        get() = statement.replace("_VERSION_", version)

    val qualifiedTitle: String
        get() = "$title $version"
}
