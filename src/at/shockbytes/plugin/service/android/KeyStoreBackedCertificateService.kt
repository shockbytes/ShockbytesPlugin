package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.model.CertificateParams
import at.shockbytes.plugin.util.HelperUtil
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

    private val separator = "----------------------------------------------------------------------\n"

    override fun getDebugCertificate(keyStorePath: String): Single<String> {
        return grabCertificateInformation(keyStorePath, "androiddebugkey",
                "android".toCharArray(), "android".toCharArray(), true)
    }

    override fun getCustomCertificate(certParams: CertificateParams): Single<String> {
        return grabCertificateInformation(certParams.keyStorePath, certParams.alias,
                certParams.keyStorePassword, certParams.entryPassword, false)
    }

    private fun grabCertificateInformation(keyStorePath: String, alias: String, keyStorePassword: CharArray,
                                           entryPassword: CharArray, isDebug: Boolean): Single<String> {

        return Single.fromCallable {
            try {

                val inStream = FileInputStream(keyStorePath)
                val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
                keystore.load(inStream, keyStorePassword)

                val entry = keystore.getEntry(alias,
                        KeyStore.PasswordProtection(entryPassword)) as KeyStore.PrivateKeyEntry

                val sb = StringBuffer()
                sb.append(if (isDebug)
                    "DEBUG CERTIFICATE FINGERPRINTS\nDebug certificate located at: "
                else
                    "CUSTOM CERTIFICATE FINGERPRINTS\nCustom certificate located at: ")
                sb.append(keyStorePath)
                sb.append("\n\n")
                sb.append("SHA1:\t${getCertFingerPrint("SHA1", entry.certificate)}\n")
                sb.append("MD5:\t${getCertFingerPrint("MD5", entry.certificate)}\n")
                sb.append(separator)
                sb.toString()

            } catch (e: Exception) {
                e.printStackTrace()
                "Cannot access debug KeyStore located at: $keyStorePath\n$separator"
            }
        }.subscribeOn(Schedulers.io())
    }

    @Throws(Exception::class)
    private fun getCertFingerPrint(mdAlg: String, cert: Certificate): String {
        val encCertInfo = cert.encoded
        val md = MessageDigest.getInstance(mdAlg)
        val digest = md.digest(encCertInfo)
        return HelperUtil.toHexString(digest)
    }
}