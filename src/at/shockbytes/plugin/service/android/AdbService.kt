package at.shockbytes.plugin.service.android

import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
interface AdbService {

    /**
     * @param deviceIp
     * @param port
     * @return
     */
    fun connectToDevice(deviceIp: String, port: Int): Single<Pair<String, String>>

    /**
     *
     * @param port
     * @return
     */
    fun connectToWearable(port: Int): Single<String>

    /**
     *
     * @return
     */
    fun disconnect(): Single<String>

    /**
     *
     *
     * @return
     */
    fun discoverDeviceIp(): Single<Pair<String, String?>>

    /**
     *
     * @return
     */
    fun restartAdbServer(): Single<String>

}