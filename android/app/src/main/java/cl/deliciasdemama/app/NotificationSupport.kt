package cl.deliciasdemama.app

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.*

class DeliciasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseConnection.initialize(this)
        NotificationSupport.initialize(this)
    }
}
object NotificationSupport {
    private lateinit var app: Context
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private val prefs get()=app.getSharedPreferences("notifications-v2",Context.MODE_PRIVATE)
    private val manager get()=app.getSystemService(NotificationManager::class.java)
    fun initialize(context: Context) {
        app=context.applicationContext
        if(!SupabaseConnection.sessions.remember) clearIdentity()
        manager.createNotificationChannel(NotificationChannel("pedidos","Pedidos y cotizaciones",NotificationManager.IMPORTANCE_HIGH))
    }
    fun requestPermission(activity: Activity) {
        if(Build.VERSION.SDK_INT>=33 && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED && !prefs.getBoolean("permissionAsked",false)) {
            prefs.edit().putBoolean("permissionAsked",true).apply()
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),80)
        }
    }
    fun bind(userId: String) {
        if(prefs.getString("userId",null)!=userId) manager.cancelAll()
        prefs.edit().putString("userId",userId).apply()
        if(FirebaseApp.getApps(app).isEmpty()) return
        scope.launch {
            runCatching {
                val token=FirebaseMessaging.getInstance().token.await()
                register(token,userId)
            }
        }
    }
    private suspend fun register(token: String,userId: String) {
        if(prefs.getString("userId",null)!=userId) return
        SupabaseConnection.client.postgrest.rpc("registrar_dispositivo_app",buildJsonObject { put("token",token);put("persistente",SupabaseConnection.sessions.remember) })
        prefs.edit().putString("token",token).apply()
    }
    fun tokenChanged(token: String) {
        val id=prefs.getString("userId",null) ?: return
        scope.launch { runCatching { SupabaseConnection.client.auth.awaitInitialization(); register(token,id) } }
    }
    suspend fun unregister() {
        val token=prefs.getString("token",null) ?: return
        SupabaseConnection.client.postgrest.rpc("retirar_dispositivo_app",buildJsonObject { put("token",token) })
        prefs.edit().remove("token").apply()
    }
    fun clearIdentity() { prefs.edit().remove("userId").apply(); manager.cancelAll() }
    @Synchronized fun show(id: String,userId: String,title: String,body: String) {
        if(prefs.getString("userId",null)!=userId) return
        if(Build.VERSION.SDK_INT>=33 && app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
        val seen=prefs.getStringSet("seen",emptySet()).orEmpty()
        if(id in seen) return
        val open=PendingIntent.getActivity(app,id.hashCode(),Intent(app,MainActivity::class.java).putExtra("openOrders",true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(id.hashCode(),NotificationCompat.Builder(app,"pedidos").setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(body).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setAutoCancel(true).setContentIntent(open).build())
        prefs.edit().putStringSet("seen",(seen.toList().takeLast(199)+id).toSet()).apply()
    }
}
class DeliciasMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { NotificationSupport.tokenChanged(token) }
    override fun onMessageReceived(message: RemoteMessage) {
        val d=message.data
        NotificationSupport.show(d["eventId"] ?: return,d["userId"] ?: return,d["title"] ?: "Tu pedido",d["body"] ?: "Hay novedades en tu pedido.")
    }
}
