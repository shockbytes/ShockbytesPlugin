package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.model.CertificateParams
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
interface CertificateService {

    /**
     *
     * @param keyStorePath
     * @return
     */
    fun getDebugCertificate(keyStorePath: String): Single<String>

    /**
     *
     * @param certParams
     * @return
     */
    fun getCustomCertificate(certParams: CertificateParams): Single<String>

}