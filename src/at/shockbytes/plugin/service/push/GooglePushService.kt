package at.shockbytes.plugin.service.push

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GooglePushService(private val apiKey: String) : PushService {

    override fun isNotificationDelivered(inStream: InputStream): Boolean {

        // Get response
        val response = StringBuilder()
        val reader = BufferedReader(InputStreamReader(inStream))

        reader.useLines {
            it.forEach { line ->
                response.append(line)
                response.append("\n")
            }
        }

        // Response is a JSON formatted string, so use prefix 'success' to
        // retrieve delivery status, without unwrapping string into JSONObject
        val s = response.toString().trim { it <= ' ' }

        var idxSuccess = s.indexOf(PREFIX_SUCCESS)
        if (idxSuccess < 0) {
            return false
        }

        idxSuccess += PREFIX_SUCCESS.length
        val idxSuccessEnd = s.indexOf(",", idxSuccess)
        if (idxSuccessEnd < idxSuccess) {
            return false
        }

        val sub = s.substring(idxSuccess, idxSuccessEnd).trim { it <= ' ' }
        return sub == "1"
    }

    override fun sendToDevice(deviceToken: String, data: Pushable): Boolean {
        return sendToDevice(deviceToken, data.pushTitle, data.pushBody)
    }

    override fun sendToDevice(deviceToken: String, title: String, body: String): Boolean {

        try {

            val url = URL(API_URL_FCM)
            val conn = url.openConnection() as HttpURLConnection

            conn.useCaches = false
            conn.doInput = true
            conn.doOutput = true

            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "key=" + apiKey)
            conn.setRequestProperty("Content-Type", "application/json")

            val json = com.google.gson.JsonObject()

            json.addProperty("to", deviceToken.trim { it <= ' ' })
            json.addProperty("priority", "high")
            val info = com.google.gson.JsonObject()
            info.addProperty("title", title)
            info.addProperty("body", body)
            info.addProperty("sound", "default")
            info.addProperty("vibrate", true)
            json.add("notification", info)

            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(json.toString())
            wr.flush()

            return isNotificationDelivered(conn.inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    companion object {

        private const val PREFIX_SUCCESS = "\"success\":"
        private const val API_URL_FCM = "https://fcm.googleapis.com/fcm/send"
    }

}
