package cl.deliciasdemama.app

data class Identity(val id: String, val email: String, val owner: Boolean)

/** La identidad procede de Auth y el permiso de una RPC del servidor, nunca de un formulario. */
object AccessRules {
    fun requireOwner(identity: Identity?) {
        require(identity?.owner == true) { "Necesitas una sesión verificada de la dueña." }
    }
    fun requireOrder(state: ShopState, identity: Identity?, orderId: String) {
        require(identity != null) { "Inicia sesión para gestionar tu pedido." }
        val order = state.orders.find { it.id == orderId }
        require(order != null && (identity.owner || order.customerUserId == identity.id)) {
            "No tienes permiso para gestionar este pedido."
        }
    }
    fun visible(state: ShopState, identity: Identity?): ShopState = if (identity?.owner == true) state else state.copy(
        orders = if (identity == null) emptyList() else state.orders.filter { it.customerUserId == identity.id },
        products = state.products.filter { it.active }, expenses = emptyList(), sales = emptyList()
    )
    fun validatePassword(password: String) {
        require(password.length >= 12 && password.toByteArray(Charsets.UTF_8).size <= 72) {
            "Usa al menos 12 caracteres y un máximo de 72 bytes para la contraseña."
        }
    }
}
