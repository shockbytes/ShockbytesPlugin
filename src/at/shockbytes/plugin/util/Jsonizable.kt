package at.shockbytes.plugin.util

import com.google.gson.JsonObject

interface Jsonizable {

    fun toJsonObject(): JsonObject
}