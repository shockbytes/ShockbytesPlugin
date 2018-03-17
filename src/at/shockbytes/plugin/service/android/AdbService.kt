package at.shockbytes.plugin.service.android

import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
interface AdbService {

    /**
     * Tries to connect adb via Wifi to a currently USB-attached Android device.
     *
     * @param deviceIp Ip address of USB-attached Android device
     * @param port Port on which the connection should be established
     * @return A pair of connection message and connected device wrapped in a Single
     */
    fun connectToDevice(deviceIp: String, port: Int): Single<Pair<String, String>>

    /**
     * Tries to connect to a wearable device already connected to an Android phone via Bluetooth debugging.
     *
     * @param port Port on which the connection should be established
     * @return Connection message depending on the connection state wrapped in a Single
     */
    fun connectToWearable(port: Int): Single<String>

    /**
     * Disconnects from a already connected adb Wifi client and switches back to USB mode.
     *
     * @return Connection message depending on the connection state wrapped in a Single
     */
    fun disconnect(): Single<String>

    /**
     * Retrieves the IP address of an USB-attached Android phone.
     *
     * @return A pair of discovery message and probably retrieved IP
     */
    fun discoverDeviceIp(): Single<Pair<String, String?>>

    /**
     * Kills the adb server instance and restarts it immediately.
     *
     * @return Message depending on the state wrapped in a Single
     */
    fun restartAdbServer(): Single<String>

    /**
     * Starts the screen capturing process of adb.
     *
     * @return Output of screen recording process wrapped in a Single
     */
    fun startScreenCapturing(): Single<String>

    /**
     * Stops the screen capturing process of adb.
     *
     * @param filePath The given filepath where to store it, it will be returned as the second argument of the Pair
     * @return A pair of the capturing output and the given filepath wrapped in a Single
     */
    fun stopScreenCapturing(filePath: String): Single<Pair<String, String>>

}