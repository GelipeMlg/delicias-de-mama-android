package cl.deliciasdemama.app

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Product(val id: String, val name: String, val description: String, val price: Long, val active: Boolean = true,
                   val flavorsEnabled: Boolean = false, val flavors: List<String> = emptyList(),
                   val fillings: List<String> = emptyList(), val servings: List<Int> = emptyList(),
                   val imageUrl: String = "")
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
    val status: Status = Status.REQUESTED, val quote: Long? = null, val saleDate: String? = null,
    val productName: String = "", val customerUserId: String? = null,
    val servings: Int? = null, val revision: Int = 0
)
data class DayConfig(val date: String, val capacity: Int, val blocked: Boolean)
data class Expense(val id: String = UUID.randomUUID().toString(), val concept: String,
                   val amount: Long, val date: String, val category: String)
data class Sale(val id: String = UUID.randomUUID().toString(), val concept: String,
                val amount: Long, val date: String, val orderId: String? = null)
data class ShopState(val orders: List<Order> = emptyList(), val days: List<DayConfig> = emptyList(),
                     val expenses: List<Expense> = emptyList(), val products: List<Product> = demoCatalog,
                     val sales: List<Sale> = emptyList(), val occupancy: Map<String, Int> = emptyMap())

/** Reglas de DEMOSTRACION. Revalidar en servidor antes de usar datos reales. */
object ShopRules {
    fun day(state: ShopState, date: String) = state.days.find { it.date == date } ?: DayConfig(date, 3, false)
    fun used(state: ShopState, date: String) = state.occupancy[date] ?: state.orders.count {
        it.date == date && it.status != Status.CANCELLED
    }
    fun available(state: ShopState, date: String): Boolean {
        val config = day(state, date)
        return !config.blocked && used(state, date) < config.capacity
    }
    fun request(state: ShopState, order: Order, today: LocalDate = LocalDate.now()): ShopState {
        require(order.customer.isNotBlank()) { "Escribe tu nombre." }
        require(order.contact.isNotBlank()) { "Escribe un teléfono o correo de contacto." }
        val product = state.products.find { it.id == order.productId && it.active }
        require(product != null) { "Selecciona un producto disponible." }
        require(if (product.flavorsEnabled) order.flavor in product.flavors else order.flavor.isBlank()) {
            "Selecciona un solo sabor disponible."
        }
        require(if (product.flavorsEnabled && product.fillings.isNotEmpty()) order.filling in product.fillings else order.filling.isBlank()) {
            "Selecciona un solo relleno disponible."
        }
        require(if (product.servings.isNotEmpty()) order.servings in product.servings else order.servings == null) {
            "Selecciona una cantidad de personas disponible."
        }
        require(state.orders.none { it.id == order.id }) { "El pedido ya existe." }
        require(order.quantity in 1..100) { "La cantidad debe estar entre 1 y 100." }
        require(!LocalDate.parse(order.date).isBefore(today)) { "Selecciona una fecha actual o futura." }
        LocalTime.parse(order.time)
        require(available(state, order.date)) { "La fecha está bloqueada o sin cupos." }
        return state.copy(orders = state.orders + order.copy(status = Status.REQUESTED, quote = null, saleDate = null, productName = product.name))
    }
    fun quote(state: ShopState, id: String, amount: Long): ShopState {
        require(amount in 1..999999999) { "Ingresa un importe válido en pesos, sin puntos ni decimales." }
        return change(state, id) {
            require(it.status in setOf(Status.REQUESTED, Status.QUOTED, Status.CONFIRMED)) { "No puedes cambiar la cotización de un pedido cerrado. Corrige su venta en Finanzas." }
            it.copy(status = Status.QUOTED, quote = amount)
        }
    }
    fun confirm(state: ShopState, id: String, today: LocalDate = LocalDate.now()): ShopState = change(state, id) {
        require(it.status == Status.QUOTED && it.quote != null) { "Primero se necesita una cotización." }
        require(!LocalDate.parse(it.date).isBefore(today)) { "La fecha del pedido ya pasó." }
        val config = day(state, it.date)
        require(!config.blocked && used(state, it.date) <= config.capacity) { "La fecha está bloqueada o sin cupos. Solicita otra fecha." }
        it.copy(status = Status.CONFIRMED)
    }
    fun deliver(state: ShopState, id: String, today: LocalDate = LocalDate.now()): ShopState {
        val order = state.orders.find { it.id == id }
        require(order != null) { "El pedido no existe." }
        require(order.status == Status.CONFIRMED && order.quote != null) { "Solo puedes entregar pedidos confirmados." }
        val next = change(state, id) { it.copy(status = Status.DELIVERED, saleDate = today.toString()) }
        return next.copy(sales = next.sales + Sale(concept = "Pedido de ${order.customer}", amount = order.quote, date = today.toString(), orderId = id))
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
        require(expense.category in setOf("insumos", "costo_fijo", "costo_variable", "otro")) { "Selecciona una categoría válida." }
        return state.copy(expenses = state.expenses.filterNot { it.id == expense.id } + expense.copy(concept = expense.concept.trim()))
    }
    fun deleteExpense(state: ShopState, id: String): ShopState {
        require(state.expenses.any { it.id == id }) { "El gasto no existe." }
        return state.copy(expenses = state.expenses.filterNot { it.id == id })
    }
    fun sale(state: ShopState, sale: Sale): ShopState {
        require(sale.concept.isNotBlank()) { "Escribe el concepto de la venta." }
        require(sale.amount in 1..999999999) { "Ingresa un importe positivo, sin puntos." }
        LocalDate.parse(sale.date)
        val previous = state.sales.find { it.id == sale.id }
        require(sale.orderId == previous?.orderId) { "El vínculo de una venta con su pedido no se modifica." }
        return state.copy(sales = state.sales.filterNot { it.id == sale.id } + sale.copy(concept = sale.concept.trim()))
    }
    fun deleteSale(state: ShopState, id: String): ShopState {
        require(state.sales.any { it.id == id }) { "La venta no existe." }
        return state.copy(sales = state.sales.filterNot { it.id == id })
    }
    fun product(state: ShopState, product: Product): ShopState {
        require(product.name.isNotBlank()) { "Escribe el nombre del producto." }
        require(product.description.isNotBlank()) { "Agrega una descripción." }
        require(product.price in 0..999999999) { "El precio debe ser cero o positivo, sin puntos." }
        val flavors = product.flavors.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
        require(!product.flavorsEnabled || flavors.isNotEmpty()) { "Agrega al menos un sabor disponible o desactiva el modo de pastel." }
        require(flavors.size <= 30 && flavors.all { it.length <= 80 && !it.contains(" · ") }) { "Usa hasta 30 sabores de máximo 80 caracteres, sin el separador ·." }
        val fillings = product.fillings.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
        require(fillings.size <= 30 && fillings.all { it.length <= 80 && !it.contains(" · ") }) { "Usa hasta 30 rellenos de máximo 80 caracteres." }
        require(product.servings.size <= 30 && product.servings.all { it in 1..1000 }) { "Usa hasta 30 opciones de personas, entre 1 y 1000." }
        return state.copy(products = state.products.filterNot { it.id == product.id } + product.copy(name = product.name.trim(), description = product.description.trim(), flavors = if(product.flavorsEnabled) flavors else emptyList(), fillings = if(product.flavorsEnabled) fillings else emptyList(), servings = product.servings.distinct().sorted()))
    }
    fun deleteProduct(state: ShopState, id: String): ShopState {
        require(state.products.any { it.id == id }) { "El producto no existe." }
        // Los pedidos conservan su referencia y nombre aunque el producto se retire.
        return if (state.orders.any { it.productId == id }) state.copy(products = state.products.map { if (it.id == id) it.copy(active = false) else it })
        else state.copy(products = state.products.filterNot { it.id == id })
    }
    private fun change(state: ShopState, id: String, block: (Order) -> Order): ShopState {
        require(state.orders.any { it.id == id }) { "El pedido no existe." }
        return state.copy(orders = state.orders.map { if (it.id == id) block(it) else it })
    }
}
