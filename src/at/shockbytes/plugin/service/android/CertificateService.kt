package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.model.CertificateParams
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
     * @param certParams parameters for accessing the keystore wrapped into a {@link CertificateParams} object
     * @return String representation of the retrieved fingerprints
     */
    fun getCustomCertificate(certParams: CertificateParams): Single<String>

}