package cl.deliciasdemama.app

val orderProgress = listOf(Status.REQUESTED, Status.QUOTED, Status.CONFIRMED, Status.DELIVERED)

fun Order.isOpen() = status != Status.DELIVERED && status != Status.CANCELLED

fun requireCurrentQuote(current: Order, reviewed: Order) {
    require(current.id == reviewed.id && current.status == Status.QUOTED &&
        reviewed.status == Status.QUOTED && current.quote != null &&
        current.quote == reviewed.quote && current.revision == reviewed.revision) {
        "La cotización cambió. Revisa el pedido actualizado antes de aceptar."
    }
}

fun Status.nextStep(): String = when (this) {
    Status.REQUESTED -> "La dueña revisará tu solicitud y enviará el precio total."
    Status.QUOTED -> "Revisa el detalle y acepta la cotización para confirmar tu pedido."
    Status.CONFIRMED -> "Tu pedido está confirmado para la fecha y hora acordadas."
    Status.DELIVERED -> "Pedido entregado. Gracias por elegir Delicias de Mamá."
    Status.CANCELLED -> "Este pedido fue cancelado y su cupo quedó liberado."
}
