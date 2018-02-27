package at.shockbytes.plugin.service.push

import java.io.InputStream

interface PushService {

    fun isNotificationDelivered(inStream: InputStream): Boolean

    fun sendToDevice(deviceToken: String, data: Pushable): Boolean

    fun sendToDevice(deviceToken: String, title: String, body: String): Boolean

}
