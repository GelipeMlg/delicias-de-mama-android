package cl.deliciasdemama.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val locale = Locale("es", "CL")
private fun money(value: Long) = NumberFormat.getCurrencyInstance(locale).apply { maximumFractionDigits=0 }.format(value)
private fun dateLabel(date: String) = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ShopViewModel(LocalRepository(filesDir)) as T
        }
        val vm = ViewModelProvider(this,factory)[ShopViewModel::class.java]
        setContent { MaterialTheme(colorScheme=lightColorScheme(
            primary=Color(0xFF854734), onPrimary=Color.White,
            primaryContainer=Color(0xFFFFDACB), secondary=Color(0xFF48664C),
            background=Color(0xFFFFF8F2), surface=Color(0xFFFFF8F2)
        )) { ShopApp(vm) } }
    }
}

@Composable
private fun ShopApp(vm: ShopViewModel) {
    val ui by vm.state.collectAsStateWithLifecycle()
    var owner by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable { mutableStateOf("Catálogo") }
    var selectedProduct by rememberSaveable { mutableStateOf(demoCatalog.first().id) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    BackHandler(enabled=page!="Catálogo") { page="Catálogo" }
    Scaffold(containerColor=MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(Modifier.padding(horizontal=20.dp,vertical=10.dp)) {
                Text("Delicias de Mamá",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                Text("Hecho con cariño, para compartir",style=MaterialTheme.typography.bodyMedium)
                Text("DEMOSTRACIÓN LOCAL · Usa datos ficticios",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text(if(owner) "Vista de la dueña" else "Vista del cliente",Modifier.weight(1f))
                    Text("Cambiar vista",style=MaterialTheme.typography.labelSmall)
                    Switch(checked=owner,onCheckedChange={owner=it;page=if(it) "Pedidos" else "Catálogo"},enabled=!ui.busy)
                }
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    (if(owner) listOf("Catálogo","Agenda","Pedidos","Finanzas") else listOf("Catálogo","Agenda","Pedidos")).forEach { tab ->
                        FilterChip(selected=page==tab,onClick={page=tab},label={Text(tab)},enabled=!ui.busy)
                    }
                }
            }
            HorizontalDivider()
            if(ui.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if(ui.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            ui.message?.let { message ->
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(message,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)
                    TextButton(onClick=vm::dismiss) { Text("Cerrar") }
                }
            }
            if(!ui.loading && !ui.loadFailed) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    when(page) {
                        "Catálogo" -> Catalog { selectedProduct=it;page="Solicitud" }
                        "Agenda" -> Calendar(ui,owner,selectedDate,{selectedDate=it},vm) {page="Solicitud"}
                        "Solicitud" -> RequestForm(ui,selectedProduct,selectedDate,vm) {page="Pedidos"}
                        "Pedidos" -> Orders(ui,owner,vm)
                        "Finanzas" -> Finance(ui,vm)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable private fun SectionTitle(title: String, subtitle: String) {
    Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
    Text(subtitle,style=MaterialTheme.typography.bodyMedium)
}
@Composable private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}
@Composable private fun Field(value: String, change: (String)->Unit, label: String, single: Boolean=true) {
    OutlinedTextField(value=value,onValueChange=change,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=single,minLines=if(single) 1 else 2)
}

@Composable private fun Catalog(reserve: (String)->Unit) {
    SectionTitle("Un dulce para cada ocasión", "Catálogo y precios de ejemplo. La dueña confirmará el valor de cada pedido.")
    demoCatalog.forEach { product -> Panel {
        Text(product.name,style=MaterialTheme.typography.titleLarge)
        Text(product.description)
        Text("Desde ${money(product.price)}",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
        Button(onClick={reserve(product.id)}) {Text("Personalizar pedido")}
    } }
}

@Composable private fun Calendar(ui: UiState,owner: Boolean,selectedDate: String,select: (String)->Unit,vm: ShopViewModel,reserve: ()->Unit) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.from(LocalDate.parse(selectedDate)).toString()) }
    val month = YearMonth.parse(monthText)
    SectionTitle("Agenda de pedidos","Elige una fecha. El cupo se confirma al aceptar la cotización.")
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
        TextButton(onClick={monthText=month.minusMonths(1).toString()}) {Text("Anterior")}
        Text(month.format(DateTimeFormatter.ofPattern("MMM yyyy",locale)),fontWeight=FontWeight.Bold)
        TextButton(onClick={monthText=month.plusMonths(1).toString()}) {Text("Siguiente")}
    }
    Row(Modifier.fillMaxWidth()) { listOf("L","M","X","J","V","S","D").forEach { Text(it,Modifier.weight(1f),style=MaterialTheme.typography.labelSmall) } }
    val offset = month.atDay(1).dayOfWeek.value-1
    val cells = (0 until ((offset+month.lengthOfMonth()+6)/7)*7).toList()
    cells.chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(2.dp)) {
            week.forEach { index ->
                val day=index-offset+1
                if(day !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(48.dp))
                else {
                    val date=month.atDay(day)
                    val available=ShopRules.available(ui.shop,date.toString())
                    val selected=date.toString()==selectedDate
                    TextButton(onClick={select(date.toString())},enabled=owner || (!date.isBefore(LocalDate.now()) && available),
                        modifier=Modifier.weight(1f).height(48.dp),contentPadding=PaddingValues(0.dp),
                        colors=ButtonDefaults.textButtonColors(containerColor=if(selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)) {
                        Text(day.toString(),style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    val config=ShopRules.day(ui.shop,selectedDate)
    Panel {
        Text(dateLabel(selectedDate),style=MaterialTheme.typography.titleMedium)
        Text(if(config.blocked) "Fecha bloqueada" else "${(config.capacity-ShopRules.used(ui.shop,selectedDate)).coerceAtLeast(0)} cupos disponibles")
        Text("Regla de prueba: 3 pedidos por día si no se configura otro valor.",style=MaterialTheme.typography.bodySmall)
        if(owner) {
            var capacity by rememberSaveable(selectedDate,config.capacity) {mutableStateOf(config.capacity.toString())}
            Field(capacity,{capacity=it},"Capacidad de pedidos")
            Button(enabled=!ui.busy,onClick={vm.act("Capacidad guardada") {ShopRules.configure(it,config.copy(capacity=capacity.toIntOrNull() ?: -1))}}) {Text("Guardar capacidad")}
            OutlinedButton(enabled=!ui.busy,onClick={vm.act("Disponibilidad actualizada") {ShopRules.configure(it,config.copy(blocked=!config.blocked))}}) {Text(if(config.blocked) "Habilitar fecha" else "Bloquear fecha")}
        } else Button(onClick=reserve,enabled=!ui.busy && !LocalDate.parse(selectedDate).isBefore(LocalDate.now()) && ShopRules.available(ui.shop,selectedDate)) {Text("Solicitar para esta fecha")}
    }
}

@Composable private fun RequestForm(ui: UiState,productId: String,date: String,vm: ShopViewModel,done: ()->Unit) {
    var name by rememberSaveable {mutableStateOf("")}; var contact by rememberSaveable {mutableStateOf("")}
    var quantity by rememberSaveable {mutableStateOf("1")}; var time by rememberSaveable {mutableStateOf("15:00")}
    var flavor by rememberSaveable {mutableStateOf("")}; var filling by rememberSaveable {mutableStateOf("")}
    var theme by rememberSaveable {mutableStateOf("")}; var restrictions by rememberSaveable {mutableStateOf("")}
    SectionTitle("Cuéntanos tu idea", "${demoCatalog.first{it.id==productId}.name} · ${dateLabel(date)} · Retiro en el local")
    Text("Para cambiar la fecha, entra a Agenda. En esta versión las solicitudes son de retiro.",style=MaterialTheme.typography.bodySmall)
    Field(name,{name=it},"Nombre ficticio")
    Field(contact,{contact=it},"Contacto ficticio")
    Field(quantity,{quantity=it},"Cantidad")
    Field(time,{time=it},"Hora de retiro HH:mm")
    Field(flavor,{flavor=it},"Sabor")
    Field(filling,{filling=it},"Relleno")
    Field(theme,{theme=it},"Temática o decoración",false)
    Field(restrictions,{restrictions=it},"Restricciones alimentarias",false)
    Button(modifier=Modifier.fillMaxWidth(),enabled=!ui.busy,onClick={vm.act("Solicitud guardada en este dispositivo",onSaved=done) {
        ShopRules.request(it,Order(customer=name.trim(),contact=contact.trim(),productId=productId,quantity=quantity.toIntOrNull() ?: 0,date=date,time=time.trim(),flavor=flavor,filling=filling,theme=theme,restrictions=restrictions))
    }}) {Text("Enviar solicitud de prueba")}
}

@Composable private fun Orders(ui: UiState,owner: Boolean,vm: ShopViewModel) {
    SectionTitle(if(owner) "Gestionar pedidos" else "Mis pedidos de prueba", "Recorrido local: solicitud, cotización, confirmación y entrega.")
    if(ui.shop.orders.isEmpty()) Text("Aún no hay pedidos. Crea uno desde el catálogo.")
    ui.shop.orders.asReversed().forEach { order -> key(order.id) { Panel {
        Text(demoCatalog.first{it.id==order.productId}.name,style=MaterialTheme.typography.titleMedium)
        Text("${order.customer} · ${order.quantity} unidad(es)")
        Text("${dateLabel(order.date)} · ${order.time}")
        Text(order.status.label,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
        listOf("Sabor" to order.flavor,"Relleno" to order.filling,"Temática" to order.theme,"Restricciones" to order.restrictions).filter{it.second.isNotBlank()}.forEach{Text("${it.first}: ${it.second}",style=MaterialTheme.typography.bodySmall)}
        order.quote?.let{Text("Cotización total: ${money(it)}",fontWeight=FontWeight.Bold)}
        if(owner && order.status==Status.REQUESTED) {
            var amount by rememberSaveable(order.id) {mutableStateOf("")}
            Field(amount,{amount=it},"Cotización total en CLP, sin puntos")
            Button(enabled=!ui.busy,onClick={vm.act("Cotización emitida") {ShopRules.quote(it,order.id,amount.toLongOrNull() ?: 0)}}) {Text("Enviar cotización")}
        }
        if(!owner && order.status==Status.QUOTED) Button(enabled=!ui.busy,onClick={vm.act("Pedido confirmado") {ShopRules.confirm(it,order.id)}}) {Text("Aceptar y confirmar")}
        if(owner && order.status==Status.CONFIRMED) Button(enabled=!ui.busy,onClick={vm.act("Entrega y venta de prueba registradas") {ShopRules.deliver(it,order.id)}}) {Text("Marcar entregado")}
        if(order.status !in setOf(Status.DELIVERED,Status.CANCELLED)) TextButton(enabled=!ui.busy,onClick={vm.act("Pedido cancelado") {ShopRules.cancel(it,order.id)}}) {Text("Cancelar pedido de prueba")}
    } } }
}

@Composable private fun Finance(ui: UiState,vm: ShopViewModel) {
    var period by rememberSaveable {mutableStateOf("Mes")}
    var concept by rememberSaveable {mutableStateOf("")}; var amount by rememberSaveable {mutableStateOf("")}
    SectionTitle("Ventas y gastos", "La demo registra una venta al marcar el pedido como entregado. No procesa pagos.")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Hoy","Semana","Mes").forEach {p->FilterChip(selected=period==p,onClick={period=p},label={Text(p)})} }
    val today=LocalDate.now()
    val start=when(period){"Hoy"->today;"Semana"->today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));else->today.withDayOfMonth(1)}
    fun inPeriod(value: String)=LocalDate.parse(value).let{!it.isBefore(start) && !it.isAfter(today)}
    val sales=ui.shop.orders.filter{it.status==Status.DELIVERED && it.saleDate?.let(::inPeriod)==true}
    val expenses=ui.shop.expenses.filter{inPeriod(it.date)}
    val salesTotal=sales.sumOf{it.quote ?: 0L}; val expenseTotal=expenses.sumOf{it.amount}
    Panel {
        Text("${dateLabel(start.toString())} al ${dateLabel(today.toString())}")
        Text("Ventas: ${money(salesTotal)}",style=MaterialTheme.typography.titleLarge)
        Text("Gastos: ${money(expenseTotal)}")
        Text("Diferencia: ${money(salesTotal-expenseTotal)}",fontWeight=FontWeight.Bold)
        Text("Diferencia simple de la demo; no equivale a utilidad contable ni saldo de caja.",style=MaterialTheme.typography.bodySmall)
    }
    Panel {
        Text("Registrar gasto de prueba",style=MaterialTheme.typography.titleMedium)
        Field(concept,{concept=it},"Concepto")
        Field(amount,{amount=it},"Importe CLP sin puntos")
        Button(enabled=!ui.busy,onClick={vm.act("Gasto guardado",onSaved={concept="";amount=""}) {ShopRules.expense(it,Expense(concept=concept.trim(),amount=amount.toLongOrNull() ?: 0,date=today.toString(),category="otro"))}}) {Text("Guardar gasto de hoy")}
    }
    Text("Movimientos del período",style=MaterialTheme.typography.titleMedium)
    if(sales.isEmpty() && expenses.isEmpty()) Text("Todavía no hay movimientos para este período.")
    sales.forEach{Text("Venta · ${it.customer} · ${money(it.quote ?: 0)}")}
    expenses.asReversed().forEach{Text("Gasto · ${it.concept} · ${money(it.amount)}")}
}
