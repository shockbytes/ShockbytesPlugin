package at.shockbytes.plugin.util

import at.shockbytes.plugin.model.CertificateParams
import com.google.gson.JsonParser
import org.apache.commons.io.IOUtils
import java.io.InputStream

/**
 * Author:  Martin Macheiner
 * Date:    27.02.2018
 */
object ShockConfig {

    fun loadCustomCertificate(): CertificateParams {

        val inStream: InputStream = javaClass.getResourceAsStream("/custom_certificate.json")
        val certAsJson = IOUtils.toString(inStream, "UTF-8")
        inStream.close()

        val jsonObject = JsonParser().parse(certAsJson).asJsonObject

        val keyStorePath = jsonObject.get("path").asString
        val alias = jsonObject.get("alias").asString
        val keyStorePassword = jsonObject.get("keystore_password").asString.toCharArray()
        val entryPassword = jsonObject.get("entry_password").asString.toCharArray()

        return CertificateParams(keyStorePath, alias, keyStorePassword, entryPassword)
    }

    fun loadFCMApiKey(): String {
        return loadFileContent("/fcm_api_key.txt")
    }

    fun loadWorkspaceLocation(): String {
        return loadFileContent("/workspace_location.txt")
    }

    private fun loadFileContent(file: String): String {
        val inStream: InputStream = javaClass.getResourceAsStream(file)
        val content = IOUtils.toString(inStream, "UTF-8").trim()
        inStream.close()
        return content
    }
}