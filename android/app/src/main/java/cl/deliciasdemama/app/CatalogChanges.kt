package cl.deliciasdemama.app

object CatalogChanges {
    fun removed(previous: List<Product>, next: List<Product>): List<Product> =
        previous.filter { old -> next.none { it.id == old.id } }
    fun between(previous: List<Product>, next: List<Product>): List<Pair<Product?, Product>> {
        require(next.map { it.id }.distinct().size == next.size) { "Producto duplicado." }
        val before = previous.associateBy { it.id }
        val after = next.associateBy { it.id }
        val edits = next.filter { before[it.id] != it }.map { before[it.id] to it }
        // La baja es lógica para conservar referencias e historial en la nube.
        val removed = previous.filter { it.id !in after && it.active }.map { it to it.copy(active = false) }
        return edits + removed
    }
}
