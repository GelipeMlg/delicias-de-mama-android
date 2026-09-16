package cl.deliciasdemama.app

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Product(val id: String, val name: String, val description: String, val price: Long)
val demoCatalog = listOf(
    Product("torta", "Torta de celebración", "Bizcocho y relleno a elección. Decoración personalizada.", 25000),
    Product("coctel", "Coctelería dulce", "Surtido para compartir en reuniones y celebraciones.", 18000),
    Product("cupcakes", "Cupcakes decorados", "Una propuesta de sabores y colores para tu ocasión.", 12000)
)
enum class Status(val label: String) {
    REQUESTED("Solicitado"), QUOTED("Cotizado"), CONFIRMED("Confirmado"),
    DELIVERED("Entregado"), CANCELLED("Cancelado")
}
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val customer: String, val contact: String, val productId: String,
    val quantity: Int, val date: String, val time: String,
    val flavor: String, val filling: String, val theme: String, val restrictions: String,
    val status: Status = Status.REQUESTED, val quote: Long? = null, val saleDate: String? = null
)
data class DayConfig(val date: String, val capacity: Int, val blocked: Boolean)
data class Expense(val id: String = UUID.randomUUID().toString(), val concept: String,
                   val amount: Long, val date: String, val category: String)
data class ShopState(val orders: List<Order> = emptyList(), val days: List<DayConfig> = emptyList(),
                     val expenses: List<Expense> = emptyList())

/** Reglas de DEMOSTRACION. Revalidar en servidor antes de usar datos reales. */
object ShopRules {
    fun day(state: ShopState, date: String) = state.days.find { it.date == date } ?: DayConfig(date, 3, false)
    fun used(state: ShopState, date: String) = state.orders.count {
        it.date == date && it.status in setOf(Status.CONFIRMED, Status.DELIVERED)
    }
    fun available(state: ShopState, date: String): Boolean {
        val config = day(state, date)
        return !config.blocked && used(state, date) < config.capacity
    }
    fun request(state: ShopState, order: Order, today: LocalDate = LocalDate.now()): ShopState {
        require(order.customer.isNotBlank()) { "Escribe un nombre para el pedido de prueba." }
        require(order.contact.isNotBlank()) { "Escribe un contacto ficticio para la demostración." }
        require(demoCatalog.any { it.id == order.productId }) { "Selecciona un producto." }
        require(order.quantity in 1..100) { "La cantidad debe estar entre 1 y 100." }
        require(!LocalDate.parse(order.date).isBefore(today)) { "Selecciona una fecha actual o futura." }
        LocalTime.parse(order.time)
        require(available(state, order.date)) { "La fecha está bloqueada o sin cupos." }
        return state.copy(orders = state.orders + order.copy(status = Status.REQUESTED, quote = null, saleDate = null))
    }
    fun quote(state: ShopState, id: String, amount: Long): ShopState {
        require(amount in 1..999999999) { "Ingresa un importe válido en pesos, sin puntos ni decimales." }
        return change(state, id) {
            require(it.status == Status.REQUESTED) { "Este pedido ya no espera cotización." }
            it.copy(status = Status.QUOTED, quote = amount)
        }
    }
    fun confirm(state: ShopState, id: String, today: LocalDate = LocalDate.now()): ShopState = change(state, id) {
        require(it.status == Status.QUOTED && it.quote != null) { "Primero se necesita una cotización." }
        require(!LocalDate.parse(it.date).isBefore(today)) { "La fecha del pedido ya pasó." }
        require(available(state, it.date)) { "Ya no quedan cupos. Solicita otra fecha." }
        it.copy(status = Status.CONFIRMED)
    }
    fun deliver(state: ShopState, id: String, today: LocalDate = LocalDate.now()): ShopState = change(state, id) {
        require(it.status == Status.CONFIRMED) { "Solo puedes entregar pedidos confirmados." }
        it.copy(status = Status.DELIVERED, saleDate = today.toString())
    }
    fun cancel(state: ShopState, id: String): ShopState = change(state, id) {
        require(it.status !in setOf(Status.DELIVERED, Status.CANCELLED)) { "El pedido ya está cerrado." }
        it.copy(status = Status.CANCELLED)
    }
    fun configure(state: ShopState, config: DayConfig): ShopState {
        LocalDate.parse(config.date)
        require(config.capacity in 0..100) { "La capacidad debe estar entre 0 y 100." }
        require(config.capacity >= used(state, config.date)) { "La capacidad no puede ser menor a los pedidos comprometidos." }
        return state.copy(days = state.days.filterNot { it.date == config.date } + config)
    }
    fun expense(state: ShopState, expense: Expense): ShopState {
        require(expense.concept.isNotBlank()) { "Escribe el concepto del gasto." }
        require(expense.amount in 1..999999999) { "El importe debe ser positivo y sin separadores." }
        LocalDate.parse(expense.date)
        return state.copy(expenses = state.expenses + expense)
    }
    private fun change(state: ShopState, id: String, block: (Order) -> Order): ShopState {
        require(state.orders.any { it.id == id }) { "El pedido no existe." }
        return state.copy(orders = state.orders.map { if (it.id == id) block(it) else it })
    }
}
