package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.model.SigningCertificate
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
interface CertificateService {

    /**
     * Retrieve fingerprint information of the debug certificate.
     *
     * @param keyStorePath Path to the debug keystore (as it may be dependent of the underlying OS)
     * @return String representation of the retrieved fingerprints
     */
    fun getDebugCertificate(keyStorePath: String): Single<String>

    /**
     * Retrieve fingerprint information of the custom certificate.
     *
     * @param certSigning parameters for accessing the keystore wrapped into a {@link SigningCertificate} object
     * @return String representation of the retrieved fingerprints
     */
    fun getCustomCertificate(certSigning: SigningCertificate): Single<String>

}