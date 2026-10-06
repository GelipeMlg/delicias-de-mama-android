package cl.deliciasdemama.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Tokens cifrados con una clave no exportable del Android Keystore; nunca guarda contraseñas. */
class SecureSessionStore(context: Context) : SessionManager {
    private val prefs = context.getSharedPreferences("session-v2", Context.MODE_PRIVATE)
    var remember: Boolean
        get() = prefs.getBoolean("remember", false)
        set(value) { prefs.edit().putBoolean("remember",value).apply(); if(!value) prefs.edit().remove("session").remove("iv").apply() }
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (ks.getKey("delicias-session",null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("delicias-session",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    override suspend fun saveSession(session: UserSession) {
        if(!remember) return
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE,key()) }
        val encrypted = cipher.doFinal(Json.encodeToString(UserSession.serializer(),session).toByteArray(Charsets.UTF_8))
        prefs.edit().putString("iv",Base64.encodeToString(cipher.iv,Base64.NO_WRAP))
            .putString("session",Base64.encodeToString(encrypted,Base64.NO_WRAP)).commit()
    }
    override suspend fun loadSession(): UserSession? {
        if(!remember) return null
        return try {
            val data = prefs.getString("session",null) ?: return null
            val iv = prefs.getString("iv",null) ?: return null
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP))) }
            Json.decodeFromString(UserSession.serializer(),cipher.doFinal(Base64.decode(data,Base64.NO_WRAP)).toString(Charsets.UTF_8))
        } catch (_: Exception) { deleteSession(); null }
    }
    override suspend fun deleteSession() { prefs.edit().remove("session").remove("iv").commit() }
}
