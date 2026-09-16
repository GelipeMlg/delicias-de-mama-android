package cl.deliciasdemama.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class UiState(val shop: ShopState = ShopState(), val loading: Boolean = true,
                   val busy: Boolean = false, val loadFailed: Boolean = false, val message: String? = null)
class ShopViewModel(private val repository: ShopRepository) : ViewModel() {
    private val mutable = MutableStateFlow(UiState())
    val state = mutable.asStateFlow()
    private val mutex = Mutex()
    init { viewModelScope.launch {
        try { mutable.value = UiState(shop=withContext(Dispatchers.IO) { repository.load() }, loading=false) }
        catch (e: Exception) { mutable.value=UiState(loading=false,loadFailed=true,message="No se pudieron leer los datos locales. Se conservaron sin sobrescribirlos.") }
    } }
    fun dismiss() { mutable.value=mutable.value.copy(message=null) }
    fun act(success: String, onSaved: () -> Unit = {}, operation: (ShopState) -> ShopState) {
        if (mutable.value.loading || mutable.value.busy || mutable.value.loadFailed) return
        mutable.value=mutable.value.copy(busy=true,message=null)
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val next = operation(mutable.value.shop)
                    withContext(Dispatchers.IO) { repository.save(next) }
                    mutable.value=mutable.value.copy(shop=next,busy=false,message=success)
                    onSaved()
                } catch (e: IllegalArgumentException) {
                    mutable.value=mutable.value.copy(busy=false,message=e.message ?: "Revisa los datos ingresados.")
                } catch (e: java.time.DateTimeException) {
                    mutable.value=mutable.value.copy(busy=false,message="Revisa la fecha y hora: usa HH:mm, por ejemplo 15:30.")
                } catch (e: Exception) {
                    mutable.value=mutable.value.copy(busy=false,message="No se pudo guardar. El cambio no se aplicó; inténtalo nuevamente.")
                }
            }
        }
    }
}
