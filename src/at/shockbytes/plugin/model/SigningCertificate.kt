package at.shockbytes.plugin.model

import java.util.Arrays


data class SigningCertificate(val name: String,
                              val icon: String,
                              val keyStorePath: String,
                              val alias: String,
                              val keyStorePassword: CharArray,
                              val entryPassword: CharArray ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SigningCertificate

        if (name != other.name) return false
        if (icon != other.icon) return false
        if (keyStorePath != other.keyStorePath) return false
        if (alias != other.alias) return false
        if (!Arrays.equals(keyStorePassword, other.keyStorePassword)) return false
        if (!Arrays.equals(entryPassword, other.entryPassword)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + keyStorePath.hashCode()
        result = 31 * result + alias.hashCode()
        result = 31 * result + Arrays.hashCode(keyStorePassword)
        result = 31 * result + Arrays.hashCode(entryPassword)
        return result
    }
}