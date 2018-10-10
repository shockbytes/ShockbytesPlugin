package at.shockbytes.plugin.util

import at.shockbytes.plugin.model.SigningCertificate
import at.shockbytes.plugin.service.dependency.model.GradleDependency
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.InputStream
import java.io.PrintWriter

/**
 * Author:  Martin Macheiner
 * Date:    27.02.2018
 */
object ConfigManager {

    fun loadCustomCertificates(): List<SigningCertificate> {

        val certAsJson = loadFileContent("/sensitive/custom_certificates.json")
        val jsonArray = JsonParser().parse(certAsJson).asJsonArray

        return jsonArray.mapTo(mutableListOf()) {

            val jsonObject = it.asJsonObject

            val name = jsonObject.get("name").asString
            val icon = jsonObject.get("icon").asString
            val keyStorePath = jsonObject.get("path").asString
            val alias = jsonObject.get("alias").asString
            val keyStorePassword = jsonObject.get("keystore_password").asString.toCharArray()
            val entryPassword = jsonObject.get("entry_password").asString.toCharArray()

            SigningCertificate(name, icon, keyStorePath, alias, keyStorePassword, entryPassword)
        }
    }

    fun loadFCMApiKey(): String {
        return loadFileContent("/sensitive/fcm_api_key.txt")
    }

    fun loadWorkspaceLocation(): String {
        return loadFileContent("/sensitive/workspace_location.txt")
    }

    fun loadGradleDependencies(): String {
        return loadFileContent("/gradle_dependencies.json")
    }

    fun storeGradleDependencies(gradleStatements: List<GradleDependency>): Completable {
        return Completable.fromAction {

            val jsonObject = JsonParser().parse(loadGradleDependencies()).asJsonObject
            jsonObject.remove("dependencies")

            val deps = JsonArray()
            val gson = GsonBuilder().setPrettyPrinting().create()
            gradleStatements.map { it.toJsonObject() }.forEach { deps.add(it) }
            jsonObject.add("dependencies", deps)

            val formatted = gson.toJson(jsonObject)
            PrintWriter(File(javaClass.getResource("/gradle_dependencies.json").path)).use { writer ->
                writer.print(formatted)
            }

        }.subscribeOn(Schedulers.io())
    }

    fun loadDebugCertificatePath(): String {
        return System.getProperty("user.home") + "/.android/debug.keystore"
    }

    fun getTemplateFile(filename: String): File {
        return File(getTemplateFileAsString(filename))
    }

    fun getTemplateFileContent(filename: String): String = loadFileContent(getTemplateFileAsString(filename))

    // -------------------------------------------------------------------------------------

    private fun loadFileContent(file: String): String {
        val inStream: InputStream? = javaClass.getResourceAsStream(file)
        return if (inStream != null) { IOUtils.toString(inStream, "UTF-8")?.trim() ?: "" } else ""
    }

    private fun getTemplateFileAsString(filename: String): String {
        return "/templates/$filename"
    }

}