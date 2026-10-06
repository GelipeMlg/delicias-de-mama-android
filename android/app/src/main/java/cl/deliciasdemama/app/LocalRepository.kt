package cl.deliciasdemama.app

import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Reemplazable por un repositorio conectado; solo guarda datos de prueba en este dispositivo. */
interface ShopRepository {
    suspend fun load(): ShopState
    suspend fun save(state: ShopState)
}
class LocalRepository(directory: File) : ShopRepository {
    private val file = AtomicFile(File(directory, "demo-v1.json"))
    override suspend fun load(): ShopState {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return ShopState()
        val root = JSONObject(file.readFully().toString(Charsets.UTF_8))
        require(root.getInt("version") in 1..4) { "Formato de datos incompatible." }
        val state = ShopState(
            root.getJSONArray("orders").objects().map { o -> Order(
                id=o.getString("id"), customer=o.getString("customer"), contact=o.getString("contact"),
                productId=o.getString("productId"), quantity=o.getInt("quantity"), date=o.getString("date"),
                time=o.getString("time"), flavor=o.getString("flavor"), filling=o.getString("filling"),
                theme=o.getString("theme"), restrictions=o.getString("restrictions"),
                status=Status.valueOf(o.getString("status")),
                quote=if(o.isNull("quote")) null else o.getLong("quote"),
                saleDate=if(o.isNull("saleDate")) null else o.getString("saleDate"),
                productName=o.optString("productName", ""),
                customerUserId=if(o.isNull("customerUserId")) null else o.getString("customerUserId")) },
            root.getJSONArray("days").objects().map { DayConfig(it.getString("date"),it.getInt("capacity"),it.getBoolean("blocked")) },
            root.getJSONArray("expenses").objects().map { Expense(it.getString("id"),it.getString("concept"),it.getLong("amount"),it.getString("date"),it.getString("category")) },
            if (root.has("products")) root.getJSONArray("products").objects().map {
                Product(it.getString("id"), it.getString("name"), it.getString("description"), it.getLong("price"), it.optBoolean("active", true), it.optBoolean("flavorsEnabled", false), it.optJSONArray("flavors")?.let { a -> (0 until a.length()).map { i -> a.getString(i) } } ?: emptyList())
            } else demoCatalog,
            if (root.has("sales")) root.getJSONArray("sales").objects().map {
                Sale(it.getString("id"), it.getString("concept"), it.getLong("amount"), it.getString("date"), if (it.isNull("orderId")) null else it.getString("orderId"))
            } else emptyList()
        )
        return state.copy(
            orders = state.orders.map { o -> if (o.productName.isBlank()) o.copy(productName = state.products.find { it.id == o.productId }?.name ?: "Producto retirado") else o },
            // Solo convertir ventas antiguas si el archivo no tiene aún un registro de ventas.
            // Una lista vacía en v2 es válida: no resucitar ventas eliminadas.
            sales = if (root.has("sales")) state.sales else state.orders.filter { it.status == Status.DELIVERED && it.quote != null && it.saleDate != null }.map {
                Sale(id = "venta-${it.id}", concept = "Pedido de ${it.customer}", amount = it.quote!!, date = it.saleDate!!, orderId = it.id)
            }
        )
    }
    override suspend fun save(state: ShopState) {
        val root = JSONObject().put("version",4)
            .put("products", JSONArray(state.products.map { JSONObject().put("id",it.id).put("name",it.name).put("description",it.description).put("price",it.price).put("active",it.active).put("flavorsEnabled",it.flavorsEnabled).put("flavors",JSONArray(it.flavors)) }))
            .put("sales", JSONArray(state.sales.map { JSONObject().put("id",it.id).put("concept",it.concept).put("amount",it.amount).put("date",it.date).put("orderId",it.orderId ?: JSONObject.NULL) }))
            .put("orders", JSONArray(state.orders.map { o -> JSONObject()
                .put("id",o.id).put("customer",o.customer).put("contact",o.contact).put("productId",o.productId).put("productName",o.productName).put("customerUserId",o.customerUserId ?: JSONObject.NULL)
                .put("quantity",o.quantity).put("date",o.date).put("time",o.time).put("flavor",o.flavor)
                .put("filling",o.filling).put("theme",o.theme).put("restrictions",o.restrictions)
                .put("status",o.status.name).put("quote",o.quote ?: JSONObject.NULL).put("saleDate",o.saleDate ?: JSONObject.NULL) }))
            .put("days",JSONArray(state.days.map { JSONObject().put("date",it.date).put("capacity",it.capacity).put("blocked",it.blocked) }))
            .put("expenses",JSONArray(state.expenses.map { JSONObject().put("id",it.id).put("concept",it.concept).put("amount",it.amount).put("date",it.date).put("category",it.category) }))
        val stream = file.startWrite()
        try {
            stream.write(root.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
}
