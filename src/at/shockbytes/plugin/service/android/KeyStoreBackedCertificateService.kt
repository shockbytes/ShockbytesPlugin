package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.model.SigningCertificate
import at.shockbytes.plugin.util.PluginUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.FileInputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.Certificate

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
class KeyStoreBackedCertificateService : CertificateService {

    private val separator = "-------------------------------------------------------------------------------------------------------${System.lineSeparator()}"

    override fun getDebugCertificate(keyStorePath: String): Single<String> {
        return grabCertificateInformation("Debug", keyStorePath, "androiddebugkey",
                "android".toCharArray(), "android".toCharArray())
    }

    override fun getCustomCertificate(certSigning: SigningCertificate): Single<String> {
        return grabCertificateInformation(certSigning.name, certSigning.keyStorePath, certSigning.alias,
                certSigning.keyStorePassword, certSigning.entryPassword)
    }

    private fun grabCertificateInformation(keyStoreName: String, keyStorePath: String, alias: String,
                                           keyStorePassword: CharArray, entryPassword: CharArray): Single<String> {

        return Single.fromCallable {
            try {

                val inStream = FileInputStream(keyStorePath)
                val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
                keystore.load(inStream, keyStorePassword)

                val entry = keystore.getEntry(alias, KeyStore.PasswordProtection(entryPassword)) as KeyStore.PrivateKeyEntry

                val sb = StringBuffer()
                sb.append("Fingerprints for $keyStoreName certificate located at:${System.lineSeparator()}$keyStorePath")
                sb.append("${System.lineSeparator()}${System.lineSeparator()}")
                sb.append("MD5:   \t${getCertFingerPrint("MD5", entry.certificate)}${System.lineSeparator()}")
                sb.append("SHA1:  \t${getCertFingerPrint("SHA1", entry.certificate)}${System.lineSeparator()}")
                sb.append("SHA256:\t${getCertFingerPrint("SHA-256", entry.certificate)}${System.lineSeparator()}")
                sb.append(separator)
                sb.toString()

            } catch (e: Exception) {
                e.printStackTrace()
                "Cannot access debug KeyStore located at: $keyStorePath${System.lineSeparator()}$separator"
            }
        }.subscribeOn(Schedulers.io())
    }

    @Throws(Exception::class)
    private fun getCertFingerPrint(mdAlg: String, cert: Certificate): String {
        val encCertInfo = cert.encoded
        val md = MessageDigest.getInstance(mdAlg)
        val digest = md.digest(encCertInfo)
        return PluginUtils.toHexString(digest)
    }
}