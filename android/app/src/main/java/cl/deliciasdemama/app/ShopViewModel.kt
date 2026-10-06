package cl.deliciasdemama.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class UiState(val shop: ShopState = ShopState(), val loading: Boolean = true,
    val busy: Boolean = false, val refreshing: Boolean = false, val loadFailed: Boolean = false, val message: String? = null,
    val identity: Identity? = null, val occupancy: Map<String, Int> = emptyMap())

class ShopViewModel(private val repository: ShopRepository, private val auth: AuthRepository) : ViewModel() {
    private val mutable = MutableStateFlow(UiState())
    val state = mutable.asStateFlow()
    private val mutex = Mutex()
    private var full = ShopState()

    init { viewModelScope.launch {
        try {
            val identity = withTimeout(15000) { auth.restore() }
            mutable.value = mutable.value.copy(identity=identity)
            full = withTimeout(20000) { withContext(Dispatchers.IO) { repository.load() } }
            publish(mutable.value.copy(loading = false))
            mutable.value.identity?.let { NotificationSupport.bind(it.id) }
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e
            mutable.value = UiState(loading = false, loadFailed = true,
                message = "No se pudo cargar el catálogo de Supabase. Revisa la conexión y desliza hacia abajo para actualizar.")
        }
    } }
    init {
        if(repository is ConnectedRepository) {
            repository.observeChanges(viewModelScope) { refresh(silent=true) }
            viewModelScope.launch { while(isActive) { delay(30000); refresh(silent=true) } }
        }
    }

    private fun publish(value: UiState = mutable.value) {
        mutable.value = value.copy(shop = AccessRules.visible(full, value.identity),
            occupancy = if(repository is ConnectedRepository) full.occupancy else full.orders.filter { it.status != Status.CANCELLED }.groupingBy { it.date }.eachCount())
    }
    fun manualRefresh() = refresh()
    fun dismiss() { mutable.value = mutable.value.copy(message = null) }

    fun refresh(silent: Boolean = false) {
        if (mutable.value.busy || mutable.value.loading || mutable.value.refreshing) return
        mutable.value = mutable.value.copy(refreshing = true, message = if(silent) mutable.value.message else null)
        viewModelScope.launch { mutex.withLock {
            try {
                val expected = mutable.value.identity
                val verified = withTimeout(15000) { if(expected != null) auth.verify() else auth.restore() }
                val loaded = withTimeout(20000) { withContext(Dispatchers.IO) { repository.load() } }
                if (mutable.value.identity?.id != expected?.id) return@withLock
                full = loaded
                publish(mutable.value.copy(identity = verified, refreshing = false, loadFailed = false,
                    message = if(silent) mutable.value.message else "Datos actualizados."))
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                publish(mutable.value.copy(refreshing = false,
                    message = if(silent) mutable.value.message else "No se pudo actualizar. Revisa tu conexión y vuelve a deslizar."))
            }
        } }
    }

    fun login(email: String, password: String, ownerPortal: Boolean, remember: Boolean, done: () -> Unit) {
        if (mutable.value.busy || mutable.value.loading) return
        mutable.value = mutable.value.copy(busy = true, message = null)
        viewModelScope.launch { mutex.withLock {
            try {
                require(email.isNotBlank() && password.isNotEmpty())
                val identity = withTimeout(20000) { auth.signIn(email.trim(), password, remember) }
                if (ownerPortal && !identity.owner) {
                    runCatching { withTimeout(5000) { auth.signOut() } }
                    publish(mutable.value.copy(identity = null, busy = false,
                        message = "Esta cuenta no tiene permiso para entrar al panel de la dueña."))
                } else {
                    full = withTimeout(20000) { withContext(Dispatchers.IO) { repository.load() } }
                    publish(mutable.value.copy(identity = identity, busy = false, loadFailed = false, message = "Sesión verificada con Supabase."))
                    NotificationSupport.bind(identity.id)
                    done()
                }
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                runCatching { withTimeout(5000) { auth.signOut() } }
                publish(mutable.value.copy(identity = null, busy = false,
                    message = "No se pudo iniciar sesión. Revisa tus credenciales, la confirmación del correo y tu conexión."))
            }
        } }
    }

    fun register(email: String, password: String, done: () -> Unit) {
        if (mutable.value.busy || mutable.value.loading) return
        mutable.value = mutable.value.copy(busy = true, message = null)
        viewModelScope.launch { mutex.withLock {
            try {
                require(email.contains('@')) { "Escribe un correo válido." }
                AccessRules.validatePassword(password)
                withTimeout(20000) { auth.register(email.trim(), password) }
                publish(mutable.value.copy(identity = null, busy = false,
                    message = "Solicitud recibida. Revisa tu correo para confirmar la cuenta y luego inicia sesión. El registro no concede permisos de dueña."))
                done()
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                publish(mutable.value.copy(identity = null, busy = false,
                    message = if(e is IllegalArgumentException) e.message else "No se pudo completar el registro. Revisa los datos y vuelve a intentarlo más tarde."))
            }
        } }
    }

    fun logout() {
        NotificationSupport.clearIdentity()
        // Ocultar datos privados inmediatamente, incluso si la invalidación remota tarda o falla.
        publish(mutable.value.copy(identity = null, busy = true, message = null))
        viewModelScope.launch { mutex.withLock {
            runCatching { withTimeout(5000) { auth.signOut() } }
            publish(mutable.value.copy(identity = null, busy = false, message = "Sesión cerrada. Inicia sesión para volver a entrar."))
        } }
    }

    /** Toda operación administrativa vuelve a verificar el permiso remoto. */
    fun act(success: String, onSaved: () -> Unit = {}, operation: (ShopState) -> ShopState) =
        mutate(success, onSaved) { identity, current -> AccessRules.requireOwner(identity); operation(current) }

    fun requestOrder(order: Order, onSaved: () -> Unit) = mutate("Solicitud enviada a la dueña", onSaved) { identity, current ->
        ShopRules.request(current, order.copy(customerUserId = identity.id))
    }
    fun confirmOrder(reviewed: Order, onSaved: () -> Unit = {}) = mutate("Pedido confirmado", onSaved) { identity, current ->
        AccessRules.requireOrder(current, identity, reviewed.id)
        val latest = current.orders.find { it.id == reviewed.id }
            ?: throw IllegalArgumentException("El pedido ya no está disponible. Actualiza tus pedidos.")
        requireCurrentQuote(latest, reviewed)
        ShopRules.confirm(current, reviewed.id)
    }
    fun cancelOrder(id: String) = mutate("Pedido cancelado") { identity, current ->
        AccessRules.requireOrder(current, identity, id); ShopRules.cancel(current, id)
    }

    fun saveProduct(product: Product, photo: android.net.Uri?, context: android.content.Context, onSaved: () -> Unit) =
        mutate("Producto guardado en Supabase", onSaved) { identity, current ->
            AccessRules.requireOwner(identity)
            // Valida los campos antes de transferir la fotografía.
            val validated = ShopRules.product(current, product).products.first { it.id == product.id }
            val url = if (photo != null) withTimeout(60000) {
                ProductPhotos.upload(context.applicationContext, photo, product.id)
            } else product.imageUrl
            ShopRules.product(current, validated.copy(imageUrl = url))
        }

    private fun mutate(success: String, onSaved: () -> Unit = {}, operation: suspend (Identity, ShopState) -> ShopState) {
        if (mutable.value.loading || mutable.value.busy || mutable.value.refreshing || mutable.value.loadFailed) return
        val expected = mutable.value.identity
        if (expected == null) { mutable.value = mutable.value.copy(message = "Inicia sesión para continuar."); return }
        mutable.value = mutable.value.copy(busy = true, message = null)
        viewModelScope.launch { mutex.withLock {
            val verified = try { withTimeout(15000) { auth.verify() } } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                publish(mutable.value.copy(identity = null, busy = false, message = "No se pudo verificar la sesión. Inicia sesión nuevamente."))
                return@withLock
            }
            if (verified.id != expected.id || mutable.value.identity?.id != expected.id) {
                publish(mutable.value.copy(identity = null, busy = false, message = "La sesión cambió. El cambio no se aplicó."))
                return@withLock
            }
            publish(mutable.value.copy(identity = verified))
            try {
                val next = operation(verified, full)
                withTimeout(20000) { withContext(Dispatchers.IO) { repository.save(next) } }
                full = withTimeout(20000) { withContext(Dispatchers.IO) { repository.load() } }
                publish(mutable.value.copy(busy = false, message = success))
                onSaved()
            } catch (e: IllegalArgumentException) {
                publish(mutable.value.copy(busy = false, message = e.message ?: "Revisa los datos."))
            } catch (e: java.time.DateTimeException) {
                publish(mutable.value.copy(busy = false, message = "Usa fecha AAAA-MM-DD y hora HH:mm."))
            } catch (e: Exception) {
                if(e is CancellationException && e !is TimeoutCancellationException) throw e
                publish(mutable.value.copy(busy = false, loadFailed = true,
                    message = "No se pudo confirmar el resultado. Desliza hacia abajo para actualizar antes de volver a guardar; el cambio podría haberse aplicado."))
            }
        } }
    }
}
