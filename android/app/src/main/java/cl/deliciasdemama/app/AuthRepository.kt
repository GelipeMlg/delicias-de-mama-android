package cl.deliciasdemama.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.Realtime
import android.content.Context
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

interface AuthRepository {
    suspend fun signIn(email: String, password: String, remember: Boolean = false): Identity
    suspend fun restore(): Identity?
    suspend fun register(email: String, password: String)
    suspend fun verify(): Identity
    suspend fun signOut()
}

/** Solo clave publicable. Bcrypt y la comprobación de contraseñas se ejecutan en Supabase Auth. */
object SupabaseConnection {
    lateinit var sessions: SecureSessionStore
        private set
    fun initialize(context: Context) { if(!::sessions.isInitialized) sessions = SecureSessionStore(context) }
    val client by lazy { createSupabaseClient(
        supabaseUrl = "https://ivxeabovtxetvvxgyywa.supabase.co",
        supabaseKey = "sb_publishable_uFkP3zHRfuaTpgTMQuu32w_CDLNUkow"
    ) {
        install(Auth) { sessionManager = sessions; autoLoadFromStorage = true; autoSaveToStorage = true; alwaysAutoRefresh = true }
        install(Postgrest)
        install(io.github.jan.supabase.storage.Storage)
        install(Realtime)
    } }
}

class SupabaseAuthRepository : AuthRepository {
    private val client = SupabaseConnection.client

    override suspend fun signIn(email: String, password: String, remember: Boolean): Identity {
        client.auth.awaitInitialization()
        client.auth.clearSession()
        SupabaseConnection.sessions.remember = remember
        try {
            client.auth.signInWith(Email) { this.email = email; this.password = password }
            return verify()
        } catch (error: Exception) {
            client.auth.clearSession()
            throw error
        }
    }

    override suspend fun restore(): Identity? {
        client.auth.awaitInitialization()
        return if(client.auth.currentSessionOrNull() != null) verify() else null
    }

    override suspend fun verify(): Identity {
        val user = client.auth.retrieveUserForCurrentSession()
        // RPC sin parámetros: utiliza auth.uid(). Ningún cliente puede elegir otro UUID o asignar un rol.
        val isOwner = client.postgrest.rpc("es_duena").decodeAs<Boolean>()
        return Identity(user.id, user.email.orEmpty(), isOwner)
    }

    override suspend fun register(email: String, password: String) {
        AccessRules.validatePassword(password)
        client.auth.awaitInitialization()
        try { client.auth.signUpWith(Email) { this.email = email; this.password = password } }
        finally { client.auth.clearSession() }
    }

    override suspend fun signOut() {
        try { NotificationSupport.unregister(); client.auth.signOut() }
        finally { client.auth.clearSession(); SupabaseConnection.sessions.remember = false; NotificationSupport.clearIdentity() }
    }
}
