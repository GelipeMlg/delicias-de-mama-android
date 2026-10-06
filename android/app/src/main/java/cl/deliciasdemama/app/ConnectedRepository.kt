package cl.deliciasdemama.app

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.json.JSONArray
import org.json.JSONObject

/** Todo el estado comercial procede del servidor. No sube el archivo demo-v1.json. */
class ConnectedRepository : ShopRepository {
    private val client = SupabaseConnection.client
    private var snapshot = ShopState(products=emptyList())
    override suspend fun load(): ShopState {
        val root = JSONObject(client.postgrest.rpc("estado_app").data)
        val photos = JSONObject(client.postgrest.rpc("fotos_catalogo_app").data)
        val state = ShopState(
            products=root.getJSONArray("products").objects().map { p -> Product(p.getString("id"),p.getString("name"),p.getString("description"),p.getLong("price"),p.getBoolean("active"),p.getBoolean("flavorsEnabled"),p.strings("flavors"),p.strings("fillings"),p.getJSONArray("servings").let { a -> (0 until a.length()).map { a.getInt(it) } },p.optString("imageUrl", "")) },
            orders=root.getJSONArray("orders").objects().map { o -> Order(
                id=o.getString("id"),customer=o.getString("customer"),contact=o.getString("contact"),productId=o.getString("productId"),
                quantity=o.getInt("quantity"),date=o.getString("date"),time=o.getString("time"),flavor=o.getString("flavor"),filling=o.getString("filling"),
                theme=o.getString("theme"),restrictions=o.getString("restrictions"),status=Status.valueOf(o.getString("status")),
                quote=if(o.isNull("quote")) null else o.getLong("quote"),saleDate=if(o.isNull("saleDate")) null else o.getString("saleDate"),
                productName=o.getString("productName"),customerUserId=o.optString("customerUserId"),servings=if(o.isNull("servings")) null else o.getInt("servings"),revision=o.getInt("revision")) },
            days=root.getJSONArray("days").objects().map { DayConfig(it.getString("date"),it.getInt("capacity"),it.getBoolean("blocked")) },
            expenses=root.getJSONArray("expenses").objects().map { Expense(it.getString("id"),it.getString("concept"),it.getLong("amount"),it.getString("date"),it.getString("category")) },
            sales=root.getJSONArray("sales").objects().map { Sale(it.getString("id"),it.getString("concept"),it.getLong("amount"),it.getString("date"),if(it.isNull("orderId")) null else it.getString("orderId")) },
            occupancy=root.getJSONObject("occupancy").let { o -> o.keys().asSequence().associateWith { o.getInt(it) } }
        )
        val withPhotos = state.copy(products = state.products.map { it.copy(imageUrl = photos.optString(it.id, "")) })
        snapshot=withPhotos
        root.optJSONArray("notifications")?.objects()?.forEach { NotificationSupport.show(it.getString("id"),it.getString("userId"),it.getString("title"),it.getString("body")) }
        return withPhotos
    }
    override suspend fun save(state: ShopState) {
        val edits=mutableListOf<Triple<String,JsonElement,JsonElement>>()
        fun <T> diff(kind: String, before: List<T>, after: List<T>, id: (T)->String, json: (T)->JsonObject) {
            val old=before.associateBy(id); val new=after.associateBy(id)
            (old.keys+new.keys).forEach { key -> if(old[key]!=new[key]) edits += Triple(kind,old[key]?.let(json) ?: JsonNull,new[key]?.let(json) ?: JsonNull) }
        }
        diff("product",snapshot.products,state.products,{it.id},{it.json()})
        diff("order",snapshot.orders,state.orders,{it.id},{it.json()})
        diff("day",snapshot.days,state.days,{it.date},{buildJsonObject { put("date",it.date);put("capacity",it.capacity);put("blocked",it.blocked) }})
        diff("expense",snapshot.expenses,state.expenses,{it.id},{buildJsonObject { put("id",it.id);put("concept",it.concept);put("amount",it.amount);put("date",it.date);put("category",it.category) }})
        // La venta de una entrega se crea dentro de la transacción del pedido.
        if(edits.none { it.first=="order" }) diff("sale",snapshot.sales,state.sales,{it.id},{buildJsonObject { put("id",it.id);put("concept",it.concept);put("amount",it.amount);put("date",it.date);put("orderId",it.orderId) }})
        require(edits.size==1) { "Guarda un cambio a la vez." }
        val (kind,before,after)=edits.single()
        if(kind == "product") client.postgrest.rpc("guardar_producto_con_foto_app",buildJsonObject { put("anterior",before);put("siguiente",after) })
        else client.postgrest.rpc("guardar_cambio_app",buildJsonObject { put("tipo",kind);put("anterior",before);put("siguiente",after) })
    }
    fun observeChanges(scope: CoroutineScope, changed: ()->Unit) = scope.launch {
        while(isActive) {
            val channel=client.channel("app-changes")
            try {
                val flow=channel.postgresChangeFlow<PostgresAction.Update>(schema="public") { table="app_changes" }
                val watcher=flow.onEach { changed() }.launchIn(this)
                channel.subscribe(blockUntilSubscribed=true)
                try { awaitCancellation() } finally { watcher.cancel() }
            } catch(e: CancellationException) { throw e }
              catch(_: Exception) { delay(5000) }
            finally { withContext(NonCancellable) { runCatching { withTimeout(3000) { client.realtime.removeChannel(channel) } } } }
        }
    }
    private fun Product.json() = buildJsonObject {
        put("id",id);put("name",name);put("description",description);put("price",price);put("active",active)
        put("imageUrl",imageUrl)
        put("flavorsEnabled",flavorsEnabled);put("flavors",JsonArray(flavors.map(::JsonPrimitive)))
        put("fillings",JsonArray(fillings.map(::JsonPrimitive)));put("servings",JsonArray(servings.map(::JsonPrimitive)))
    }
    private fun Order.json() = buildJsonObject {
        put("id",id);put("customer",customer);put("contact",contact);put("productId",productId);put("quantity",quantity)
        put("date",date);put("time",time);put("flavor",flavor);put("filling",filling);put("theme",theme);put("restrictions",restrictions)
        put("status",status.name);put("quote",quote);put("servings",servings);put("revision",revision)
    }
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.strings(key: String) = getJSONArray(key).let { a -> (0 until a.length()).map { a.getString(it) } }
}
