package cl.deliciasdemama.app

import android.os.Bundle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import java.util.UUID

private val locale = Locale("es", "CL")
private fun money(value: Long) = NumberFormat.getCurrencyInstance(locale).apply { maximumFractionDigits=0 }.format(value)
private fun dateLabel(date: String) = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))

class MainActivity : ComponentActivity() {
    private var openOrders by mutableStateOf(false)
    override fun onNewIntent(intent: android.content.Intent) { super.onNewIntent(intent); openOrders=intent.getBooleanExtra("openOrders",false) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openOrders=intent.getBooleanExtra("openOrders",false)
        enableEdgeToEdge()
        NotificationSupport.requestPermission(this)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ShopViewModel(ConnectedRepository(), SupabaseAuthRepository()) as T
        }
        val vm = ViewModelProvider(this,factory)[ShopViewModel::class.java]
        setContent { MaterialTheme(colorScheme=lightColorScheme(
            primary=Color(0xFF984858), onPrimary=Color.White,
            primaryContainer=Color(0xFFFFDADF), onPrimaryContainer=Color(0xFF572936),
            secondary=Color(0xFF516546), secondaryContainer=Color(0xFFE3ECD9),
            background=Color(0xFFFFF8F4), surface=Color(0xFFFFF8F4),
            onSurface=Color(0xFF38251F), onBackground=Color(0xFF38251F),
            surfaceVariant=Color(0xFFF6E6E1), outline=Color(0xFF997B76)
        ), shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(20.dp),large=RoundedCornerShape(28.dp))) { ShopApp(vm,openOrders) { openOrders=false } } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopApp(vm: ShopViewModel,openOrders: Boolean,handled: ()->Unit) {
    val ui by vm.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val owner = ui.identity?.owner == true
    var loginOpen by remember { mutableStateOf(false) }
    var ownerPortal by remember { mutableStateOf(false) }
    var page by rememberSaveable { mutableStateOf("Catálogo") }
    var selectedProduct by rememberSaveable { mutableStateOf(demoCatalog.first().id) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    val tabs = if(owner) listOf("Productos","Agenda","Pedidos","Finanzas") else listOf("Catálogo","Agenda","Pedidos")
    LaunchedEffect(ui.identity?.id, owner) { page=if(owner) "Productos" else "Catálogo" }
    LaunchedEffect(openOrders,ui.identity?.id) { if(openOrders && ui.identity!=null) {page="Pedidos";handled()} }
    BackHandler(enabled=page!=tabs.first()) { page=tabs.first() }
    Scaffold(containerColor=MaterialTheme.colorScheme.background, bottomBar = {
        NavigationBar(containerColor=Color.White) {
            tabs.forEachIndexed { index, title ->
                NavigationBarItem(selected=page==title, onClick={page=title}, enabled=!ui.busy,
                    icon={Text(listOf("▦","▤","♡","$")[index], fontWeight=FontWeight.Bold, style=MaterialTheme.typography.titleLarge)},
                    label={Text(title)}, alwaysShowLabel=true)
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(Modifier.padding(horizontal=20.dp,vertical=10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_delicias_mama),
                        contentDescription = "Logo",
                        modifier = Modifier.size(60.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Delicias de Mamá",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
                        Text("Hecho con cariño, para compartir",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)
                    }
                }
                Text("Desliza hacia abajo para actualizar", style=MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text(if(owner) "Vista de la dueña" else "Vista del cliente",Modifier.weight(1f))
                    if(ui.identity!=null) TextButton(enabled=!ui.busy,onClick=vm::logout) {Text("Cerrar sesión")}
                    else TextButton(enabled=!ui.busy,onClick={ownerPortal=false;loginOpen=true;vm.dismiss()}) {Text("Iniciar sesión")}
                }
                if(ui.identity==null) TextButton(enabled=!ui.busy,onClick={ownerPortal=true;loginOpen=true;vm.dismiss()}) {Text("Acceso de la dueña")}
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
            if(ui.loading || ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            PullToRefreshBox(isRefreshing=ui.refreshing,onRefresh=vm::manualRefresh,modifier=Modifier.weight(1f)) {
                key(page) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    if(ui.loadFailed) Text("No se pudieron cargar los datos. Revisa tu conexión y desliza hacia abajo para reintentar.")
                    else if(!ui.loading) when(page) {
                        "Catálogo", "Productos" -> Catalog(ui,owner,vm) { if(ui.identity==null) {ownerPortal=false;loginOpen=true;vm.dismiss()} else {selectedProduct=it;page="Solicitud"} }
                        "Agenda" -> Calendar(ui,owner,selectedDate,{selectedDate=it},vm) {
                            if(ui.identity==null) {ownerPortal=false;loginOpen=true;vm.dismiss()}
                            else {selectedProduct=ui.shop.products.firstOrNull { it.active }?.id ?: "";page="Solicitud"}
                        }
                        "Solicitud" -> if(ui.identity!=null) RequestForm(ui,selectedProduct,selectedDate,vm) {page="Pedidos"}
                        "Pedidos" -> Orders(ui,owner,vm)
                        "Finanzas" -> if(owner) Finance(ui,vm) {page="Pedidos"}
                    }
                    Spacer(Modifier.height(16.dp))
                } }
            }
        }
    }
    if(loginOpen) LoginDialog(ui,vm,ownerPortal) {loginOpen=false}
}

@Composable private fun LoginDialog(ui: UiState,vm: ShopViewModel,ownerPortal: Boolean,close: ()->Unit) {
    var email by remember { mutableStateOf("") }
    // Nunca rememberSaveable: la contraseña no se serializa en el estado de Android.
    var password by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }
    var keepSession by rememberSaveable { mutableStateOf(true) }
    AlertDialog(onDismissRequest={if(!ui.busy) {password="";close()}},
        title={Text(if(ownerPortal) "Acceso de la dueña" else if(register) "Crear cuenta de cliente" else "Iniciar sesión")},
        text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(if(ownerPortal) "Ingresa con la cuenta autorizada para administrar Delicias de Mamá." else "Usa tu cuenta para solicitar y consultar tus pedidos.")
            OutlinedTextField(value=email,onValueChange={email=it},label={Text("Correo electrónico")},singleLine=true,enabled=!ui.busy,
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=password,onValueChange={password=it},label={Text("Contraseña")},singleLine=true,enabled=!ui.busy,
                visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),modifier=Modifier.fillMaxWidth())
            if(!register) Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(keepSession,{keepSession=it},enabled=!ui.busy); Text("Mantener sesión iniciada") }
            if(register) Text("Al menos 12 caracteres. Confirma tu correo antes de entrar.",style=MaterialTheme.typography.bodySmall)
            if(!ownerPortal) TextButton(enabled=!ui.busy,onClick={register=!register;password="";vm.dismiss()}) {Text(if(register) "Ya tengo cuenta" else "Crear cuenta de cliente")}
            ui.message?.let {Text(it,color=MaterialTheme.colorScheme.primary)}
            if(ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }},
        confirmButton={TextButton(enabled=!ui.busy && email.isNotBlank() && password.isNotEmpty(),onClick={
            val entered=password;password=""
            if(register) vm.register(email,entered) {register=false}
            else vm.login(email,entered,ownerPortal,keepSession,close)
        }) {Text(if(register) "Crear cuenta" else "Entrar")}},
        dismissButton={TextButton(enabled=!ui.busy,onClick={password="";close()}) {Text("Volver")}})
}

private fun used(ui: UiState,date: String)=ui.occupancy[date] ?: 0
private fun available(ui: UiState,date: String): Boolean {
    val day=ShopRules.day(ui.shop,date)
    return !day.blocked && used(ui,date)<day.capacity
}

@Composable private fun SectionTitle(title: String, subtitle: String) {
    Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
    Text(subtitle,style=MaterialTheme.typography.bodyMedium)
}
@Composable private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),elevation=CardDefaults.cardElevation(defaultElevation=1.dp),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}
@Composable private fun Field(value: String, change: (String)->Unit, label: String, single: Boolean=true, numeric: Boolean=false) {
    OutlinedTextField(value=value,onValueChange=change,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=single,minLines=if(single) 1 else 2,
        shape=RoundedCornerShape(14.dp), keyboardOptions=KeyboardOptions(keyboardType=if(numeric) KeyboardType.Number else KeyboardType.Text))
}

@Composable private fun Catalog(ui: UiState,owner: Boolean,vm: ShopViewModel,reserve: (String)->Unit) {
    var editing by remember { mutableStateOf<Product?>(null) }
    var deleting by remember { mutableStateOf<Product?>(null) }
    var showRetired by rememberSaveable { mutableStateOf(false) }
    SectionTitle(if(owner) "Tus productos" else "Un dulce para cada ocasión", if(owner) "Crea y actualiza lo que ofreces. Los cambios se reflejan en el catálogo." else "Elige tu favorito y cuéntanos cómo lo imaginas.")
    if(owner) Button(onClick={editing=Product(UUID.randomUUID().toString(),"","",0)},enabled=!ui.busy,modifier=Modifier.fillMaxWidth()) {Text("+ Agregar producto")}
    if(owner) Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(showRetired,{showRetired=it}); Text("Mostrar productos retirados") }
    val products=ui.shop.products.filter { it.active || (owner && showRetired) }
    if(products.isEmpty()) Panel {Text("Aún no hay productos disponibles.")}
    products.forEach { product -> key(product.id) { Panel {
        ProductPhoto(product.imageUrl, product.name)
        Text(product.name,style=MaterialTheme.typography.titleLarge)
        Text(product.description)
        if(product.flavorsEnabled) Text("Sabores: ${product.flavors.joinToString(", ")}",style=MaterialTheme.typography.bodySmall)
        Text("Desde ${money(product.price)}",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
        if(owner) {
            Text(if(product.active) "Visible en catálogo" else "Retirado del catálogo", style=MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick={editing=product},enabled=!ui.busy) {Text("Editar")}
                TextButton(onClick={deleting=product},enabled=!ui.busy) {Text("Eliminar")}
            }
        } else Button(onClick={reserve(product.id)},modifier=Modifier.fillMaxWidth(),enabled=!ui.busy) {Text("Personalizar pedido")}
    } } }
    editing?.let { product -> ProductEditor(product,ui,vm) {editing=null} }
    deleting?.let { product -> ConfirmRemoval("Eliminar ${product.name}", "Si tiene pedidos asociados, se retirará del catálogo conservando su historial.",ui.busy,{deleting=null}) {
        vm.act("Producto eliminado del catálogo. Si tiene pedidos, se conserva retirado en el historial.",onSaved={deleting=null}) {ShopRules.deleteProduct(it,product.id)}
    } }
}

@Composable private fun ProductPhoto(model: Any?, name: String) {
    var failed by remember(model) { mutableStateOf(false) }
    if (model == null || model == "" || failed) {
        Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFE9E6)), contentAlignment = Alignment.Center) {
            Text(if(failed) "No se pudo cargar la foto" else "Foto por agregar",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else AsyncImage(model = model, contentDescription = "Foto de $name",
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(18.dp)),
        contentScale = ContentScale.Crop, onError = { failed = true })
}

@Composable private fun CakeArt() {
    Canvas(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFFFE9E6))) {
        val w=size.width; val h=size.height
        drawOval(Color(0xFFE6C3B6),Offset(w*.27f,h*.77f),Size(w*.46f,h*.15f))
        drawRoundRect(Color(0xFFAF7960),Offset(w*.32f,h*.35f),Size(w*.36f,h*.46f),CornerRadius(12f,12f))
        drawRect(Color(0xFFFFFAEC),Offset(w*.32f,h*.49f),Size(w*.36f,h*.1f))
        drawOval(Color(0xFFFFF6DF),Offset(w*.32f,h*.27f),Size(w*.36f,h*.22f))
        listOf(.4f,.5f,.6f).forEach { drawCircle(Color(0xFFC45C70),h*.075f,Offset(w*it,h*.32f)) }
    }
}

@Composable private fun ConfirmRemoval(title: String, detail: String,busy: Boolean,close: ()->Unit,confirm: ()->Unit) {
    AlertDialog(onDismissRequest={if(!busy)close()},title={Text(title)},text={Text(detail)},
        confirmButton={TextButton(onClick=confirm,enabled=!busy){Text("Eliminar")}},
        dismissButton={TextButton(onClick=close,enabled=!busy){Text("Volver")}})
}

@Composable private fun ProductEditor(product: Product,ui: UiState,vm: ShopViewModel,close: ()->Unit) {
    var name by rememberSaveable(product.id){mutableStateOf(product.name)}
    var description by rememberSaveable(product.id){mutableStateOf(product.description)}
    var price by rememberSaveable(product.id){mutableStateOf(product.price.toString())}
    var active by rememberSaveable(product.id){mutableStateOf(product.active)}
    var flavorsEnabled by rememberSaveable(product.id){mutableStateOf(product.flavorsEnabled)}
    var flavorsText by rememberSaveable(product.id){mutableStateOf(product.flavors.joinToString("\n"))}
    var fillingsText by rememberSaveable(product.id){mutableStateOf(product.fillings.joinToString("\n"))}
    var servingsText by rememberSaveable(product.id){mutableStateOf(product.servings.joinToString("\n"))}
    var imageUrl by rememberSaveable(product.id){mutableStateOf(product.imageUrl)}
    var photoUri by rememberSaveable(product.id){mutableStateOf<String?>(null)}
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if(uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photoUri = uri.toString()
        }
    }
    AlertDialog(onDismissRequest={if(!ui.busy)close()},title={Text("Producto")}, text={
        Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Field(name,{name=it},"Nombre"); Field(description,{description=it},"Descripción",false)
            ProductPhoto(photoUri?.let(Uri::parse) ?: imageUrl, name)
            OutlinedButton(enabled=!ui.busy, onClick={photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}) {
                Text(if(photoUri != null || imageUrl.isNotBlank()) "Cambiar foto" else "Elegir foto")
            }
            if(photoUri != null || imageUrl.isNotBlank()) TextButton(enabled=!ui.busy,onClick={photoUri=null;imageUrl=""}) {Text("Quitar foto")}
            Text("Opcional. La foto se publica en el catálogo al guardar.",style=MaterialTheme.typography.bodySmall)
            Field(price,{price=it},"Precio base CLP",numeric=true)
            Row(verticalAlignment=Alignment.CenterVertically){Switch(active,{active=it}); Text("Visible en catálogo")}
            Row(verticalAlignment=Alignment.CenterVertically){Checkbox(flavorsEnabled,{flavorsEnabled=it}); Text("Modo de pastel")}
            Field(servingsText,{servingsText=it},if(flavorsEnabled) "Pastel para cuántas personas (una opción por línea)" else "Para cuántas personas (opcional, una opción por línea)",false)
            if(flavorsEnabled) {
                Field(flavorsText,{flavorsText=it},"Sabores disponibles (uno por línea)",false)
                Field(fillingsText,{fillingsText=it},"Rellenos disponibles (uno por línea)",false)
                Text("El cliente elegirá un sabor y un relleno de tus opciones.",style=MaterialTheme.typography.bodySmall)
            }
            ui.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
        }
    },confirmButton={TextButton(enabled=!ui.busy && !ui.refreshing && !ui.loadFailed,onClick={vm.saveProduct(product.copy(name=name,description=description,price=price.toLongOrNull() ?: -1,active=active,flavorsEnabled=flavorsEnabled,flavors=flavorsText.lines(),fillings=fillingsText.lines(),servings=servingsText.lines().filter{it.isNotBlank()}.map{it.trim().toIntOrNull() ?: -1},imageUrl=imageUrl),photoUri?.let(Uri::parse),context,close)}){Text(if(ui.busy) "Guardando…" else "Guardar")}},
        dismissButton={TextButton(onClick=close,enabled=!ui.busy){Text("Volver")}})
}

@Composable private fun Calendar(ui: UiState,owner: Boolean,selectedDate: String,select: (String)->Unit,vm: ShopViewModel,reserve: ()->Unit) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.from(LocalDate.parse(selectedDate)).toString()) }
    val month = YearMonth.parse(monthText)
    SectionTitle("Tu agenda dulce","Cada solicitud reserva un cupo. Al cancelarla, el cupo vuelve a estar disponible.")
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
                    val available=available(ui,date.toString())
                    val selected=date.toString()==selectedDate
                    TextButton(onClick={select(date.toString())},enabled=owner || (!date.isBefore(LocalDate.now()) && available),
                        modifier=Modifier.weight(1f).height(48.dp),contentPadding=PaddingValues(0.dp),
                        colors=ButtonDefaults.textButtonColors(containerColor=if(selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)) {
                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(day.toString(),style=MaterialTheme.typography.bodySmall)
                            val count=used(ui,date.toString())
                            if(count>0) Text("$count p.",style=MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
    val config=ShopRules.day(ui.shop,selectedDate)
    Panel {
        Text(dateLabel(selectedDate),style=MaterialTheme.typography.titleMedium)
        val dailyOrders=ui.shop.orders.filter{it.date==selectedDate && it.status!=Status.CANCELLED}
        val pending=dailyOrders.count{it.status in setOf(Status.REQUESTED,Status.QUOTED)}
        val count=used(ui,selectedDate)
        val free=if(config.blocked) 0 else (config.capacity-count).coerceAtLeast(0)
        Text("$count de ${config.capacity} cupos ocupados",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        LinearProgressIndicator(progress={if(config.capacity==0) 0f else (count.toFloat()/config.capacity).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
        Text(if(config.blocked) "Fecha bloqueada · 0 cupos disponibles" else "$free cupos disponibles")
        if(owner) Text("$pending pendientes · ${dailyOrders.size-pending} confirmados o entregados",style=MaterialTheme.typography.bodySmall)
        if(owner) {
            var capacity by rememberSaveable(selectedDate,config.capacity) {mutableStateOf(config.capacity.toString())}
            Field(capacity,{capacity=it},"Capacidad de pedidos",numeric=true)
            Button(enabled=!ui.busy,onClick={vm.act("Capacidad guardada") {ShopRules.configure(it,config.copy(capacity=capacity.toIntOrNull() ?: -1))}}) {Text("Guardar capacidad")}
            OutlinedButton(enabled=!ui.busy,onClick={vm.act("Disponibilidad actualizada") {ShopRules.configure(it,config.copy(blocked=!config.blocked))}}) {Text(if(config.blocked) "Habilitar fecha" else "Bloquear fecha")}
        } else Button(onClick=reserve,enabled=!ui.busy && ui.shop.products.any{it.active} && !LocalDate.parse(selectedDate).isBefore(LocalDate.now()) && available(ui,selectedDate)) {Text("Solicitar para esta fecha")}
        if(owner) dailyOrders.sortedBy{it.time}.forEach { order ->
            HorizontalDivider()
            Text("${order.time} · ${order.customer}",fontWeight=FontWeight.SemiBold)
            Text("${order.productName} · ${order.status.label}",style=MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun RequestForm(ui: UiState,productId: String,date: String,vm: ShopViewModel,done: ()->Unit) {
    val requestId=rememberSaveable(productId,date) { UUID.randomUUID().toString() }
    var name by rememberSaveable {mutableStateOf("")}; var contact by rememberSaveable {mutableStateOf("")}
    var quantity by rememberSaveable {mutableStateOf("1")}; var time by rememberSaveable {mutableStateOf("15:00")}
    var flavor by rememberSaveable(productId) {mutableStateOf("")}; var filling by rememberSaveable(productId) {mutableStateOf("")}
    var servings by rememberSaveable(productId) {mutableStateOf<Int?>(null)}
    var theme by rememberSaveable {mutableStateOf("")}; var restrictions by rememberSaveable {mutableStateOf("")}
    val product=ui.shop.products.find{it.id==productId && it.active}
    if(product==null) { SectionTitle("Elige un producto", "Visita el catálogo para seleccionar un producto disponible."); return }
    SectionTitle("Cuéntanos tu idea", "${product.name} · ${dateLabel(date)} · Retiro en el local")
    Text("Para cambiar la fecha, entra a Agenda. En esta versión las solicitudes son de retiro.",style=MaterialTheme.typography.bodySmall)
    Field(name,{name=it},"Tu nombre")
    Field(contact,{contact=it},"Teléfono o correo")
    Field(quantity,{quantity=it},"Cantidad",numeric=true)
    Field(time,{time=it},"Hora de retiro HH:mm")
    if(product.servings.isNotEmpty()) {
        Text(if(product.flavorsEnabled) "Pastel para cuántas personas" else "Para cuántas personas",fontWeight=FontWeight.Bold)
        product.servings.forEach { option ->
            Row(verticalAlignment=Alignment.CenterVertically) { RadioButton(servings==option,{servings=option}); Text("$option personas") }
        }
    }
    if(product.flavorsEnabled) {
        Text("Elige un sabor",fontWeight=FontWeight.Bold)
        product.flavors.forEach { option ->
            Row(verticalAlignment=Alignment.CenterVertically) { RadioButton(flavor==option,{flavor=option}); Text(option) }
        }
        if(product.fillings.isNotEmpty()) Text("Elige un relleno",fontWeight=FontWeight.Bold)
        product.fillings.forEach { option ->
            Row(verticalAlignment=Alignment.CenterVertically) { RadioButton(filling==option,{filling=option}); Text(option) }
        }
    }
    Field(theme,{theme=it},"Temática o decoración",false)
    Field(restrictions,{restrictions=it},"Restricciones alimentarias",false)
    Button(modifier=Modifier.fillMaxWidth(),enabled=!ui.busy,onClick={vm.requestOrder(
        Order(id=requestId,customer=name.trim(),contact=contact.trim(),productId=productId,quantity=quantity.toIntOrNull() ?: 0,date=date,time=time.trim(),flavor=if(product.flavorsEnabled) flavor else "",filling=if(product.flavorsEnabled) filling else "",theme=theme,restrictions=restrictions,servings=servings),done)
    }) {Text("Enviar solicitud")}
}

@Composable private fun Orders(ui: UiState,owner: Boolean,vm: ShopViewModel) {
    var filter by rememberSaveable { mutableStateOf("Pendientes") }
    var reviewing by remember { mutableStateOf<Order?>(null) }
    var cancelling by remember { mutableStateOf<Order?>(null) }
    val canSave = !ui.busy && !ui.refreshing && !ui.loading && !ui.loadFailed
    SectionTitle(if(owner) "Gestionar pedidos" else "Mis pedidos", "Consulta el estado y las cotizaciones de tus pedidos.")
    if (ui.identity != null) {
        Text("${ui.shop.orders.count { it.isOpen() }} pendientes · ${ui.shop.orders.count { it.status == Status.QUOTED }} por aceptar", style=MaterialTheme.typography.bodyMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("Pendientes", "Historial", "Todos").forEach { label ->
                FilterChip(selected=filter==label, onClick={filter=label}, label={Text(label)})
            }
        }
    }
    val visibleOrders = ui.shop.orders.filter {
        when(filter) { "Pendientes" -> it.isOpen(); "Historial" -> !it.isOpen(); else -> true }
    }.sortedWith(compareBy<Order> { it.date }.thenBy { it.time })
    if(ui.identity==null) Text("Inicia sesión para consultar tus pedidos.")
    else if(ui.shop.orders.isEmpty()) Text("Aún no tienes pedidos.")
    else if(visibleOrders.isEmpty()) Text("No hay pedidos en esta sección.")
    visibleOrders.forEach { order -> key(order.id) { Panel {
        Text(order.productName.ifBlank {ui.shop.products.find{it.id==order.productId}?.name ?: "Producto retirado"},style=MaterialTheme.typography.titleMedium)
        Text("${order.customer} · ${order.quantity} unidad(es)")
        order.servings?.let { Text("Para $it personas") }
        Text("${dateLabel(order.date)} · ${order.time}")
        Text(order.status.label,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
        if(order.status != Status.CANCELLED) {
            val step = orderProgress.indexOf(order.status)
            Text("Paso ${step + 1} de ${orderProgress.size} · ${orderProgress.joinToString(" → ") { it.label }}", style=MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(progress={ (step + 1).toFloat() / orderProgress.size }, modifier=Modifier.fillMaxWidth())
        }
        Text(order.status.nextStep(),style=MaterialTheme.typography.bodySmall)
        if(owner) Text("Contacto: ${order.contact}",style=MaterialTheme.typography.bodySmall)
        listOf("Sabor" to order.flavor,"Relleno" to order.filling,"Temática" to order.theme,"Restricciones" to order.restrictions).filter{it.second.isNotBlank()}.forEach{Text("${it.first}: ${it.second}",style=MaterialTheme.typography.bodySmall)}
        order.quote?.let{Text("Cotización total: ${money(it)}",fontWeight=FontWeight.Bold)}
        if(owner && order.status in setOf(Status.REQUESTED,Status.QUOTED,Status.CONFIRMED)) {
            var amount by rememberSaveable(order.id,order.quote) {mutableStateOf(order.quote?.toString() ?: "")}
            Field(amount,{amount=it},"Cotización total CLP",numeric=true)
            if(order.status==Status.CONFIRMED) Text("Al corregir el monto, el cliente deberá aceptar nuevamente. Su cupo se conserva.",style=MaterialTheme.typography.bodySmall)
            Button(enabled=canSave && (amount.toLongOrNull() ?: 0) in 1..999999999,onClick={vm.act("Cotización guardada; pendiente de aceptación") {ShopRules.quote(it,order.id,amount.toLongOrNull() ?: 0)}}) {Text(if(order.quote==null) "Enviar cotización" else "Corregir cotización")}
        }
        if(!owner && order.status==Status.QUOTED) Button(enabled=canSave,onClick={reviewing=order}) {Text("Revisar y aceptar")}
        if(owner && order.status==Status.CONFIRMED) Button(enabled=canSave,onClick={vm.act("Entrega y venta registradas") {ShopRules.deliver(it,order.id)}}) {Text("Marcar entregado")}
        if(order.isOpen()) TextButton(enabled=canSave,onClick={cancelling=order}) {Text("Cancelar pedido")}
    } } }
    reviewing?.let { reviewed ->
        val current = ui.shop.orders.find { it.id == reviewed.id }
        val unchanged = current != null && runCatching { requireCurrentQuote(current, reviewed) }.isSuccess
        AlertDialog(onDismissRequest={if(!ui.busy) reviewing=null},title={Text("Revisar cotización")},text={
            Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(reviewed.productName.ifBlank { "Producto del pedido" },fontWeight=FontWeight.Bold)
                Text("${reviewed.quantity} unidad(es) · ${dateLabel(reviewed.date)} · ${reviewed.time}")
                reviewed.servings?.let { Text("Para $it personas") }
                listOf("Sabor" to reviewed.flavor,"Relleno" to reviewed.filling,"Temática" to reviewed.theme,"Restricciones" to reviewed.restrictions).filter { it.second.isNotBlank() }.forEach { Text("${it.first}: ${it.second}") }
                Text("Total del pedido: ${money(reviewed.quote ?: 0)}",fontWeight=FontWeight.Bold)
                Text("Aceptar confirma el pedido. No realiza un cobro; coordina el pago con la dueña.")
                if(!unchanged) Text("El pedido cambió. Cierra esta ventana y revisa la nueva cotización.",color=MaterialTheme.colorScheme.error)
            }
        },confirmButton={TextButton(enabled=canSave && unchanged,onClick={vm.confirmOrder(reviewed){reviewing=null}}){Text("Aceptar cotización")}},
            dismissButton={TextButton(enabled=!ui.busy,onClick={reviewing=null}){Text("Volver")}})
    }
    cancelling?.let { order ->
        AlertDialog(onDismissRequest={if(!ui.busy) cancelling=null}, title={Text("¿Cancelar este pedido?")},
            text={Text("${order.productName} · ${dateLabel(order.date)} · ${order.time}. Se liberará el cupo. Si ya pagaste un abono, coordina su devolución con la dueña; la app no devuelve dinero automáticamente.")},
            confirmButton={TextButton(enabled=canSave,onClick={vm.cancelOrder(order.id);cancelling=null}){Text("Sí, cancelar pedido")}},
            dismissButton={TextButton(enabled=!ui.busy,onClick={cancelling=null}){Text("Conservar pedido")}})
    }
}

@Composable private fun Finance(ui: UiState,vm: ShopViewModel,openQuotes: ()->Unit) {
    var period by rememberSaveable {mutableStateOf("Mes")}
    var editingSale by remember {mutableStateOf<Sale?>(null)}
    var editingExpense by remember {mutableStateOf<Expense?>(null)}
    var deletingSale by remember {mutableStateOf<Sale?>(null)}
    var deletingExpense by remember {mutableStateOf<Expense?>(null)}
    SectionTitle("Las cuentas de tu negocio", "Revisa tus ventas, corrige importes y registra tus gastos.")
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("Hoy","Semana","Mes","Todo").forEach {p->FilterChip(selected=period==p,onClick={period=p},label={Text(p)})}
    }
    val today=LocalDate.now()
    val start=when(period){"Hoy"->today;"Semana"->today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));else->today.withDayOfMonth(1)}
    fun inPeriod(value: String)=period=="Todo" || LocalDate.parse(value).let{!it.isBefore(start) && !it.isAfter(today)}
    val sales=ui.shop.sales.filter{inPeriod(it.date)}.sortedByDescending{it.date}
    val expenses=ui.shop.expenses.filter{inPeriod(it.date)}.sortedByDescending{it.date}
    val salesTotal=sales.sumOf{it.amount}; val expenseTotal=expenses.sumOf{it.amount}
    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer),shape=RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(if(period=="Todo") "Todos los movimientos" else "${dateLabel(start.toString())} al ${dateLabel(today.toString())}",style=MaterialTheme.typography.labelLarge)
            Text("Diferencia del período",style=MaterialTheme.typography.titleMedium)
            Text(money(salesTotal-expenseTotal),style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
            Text("Ventas: ${money(salesTotal)}")
            Text("Gastos: ${money(expenseTotal)}")
            Text("Ventas menos gastos registrados. No equivale a utilidad contable ni saldo de caja.",style=MaterialTheme.typography.bodySmall)
        }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Button(modifier=Modifier.weight(1f),enabled=!ui.busy,onClick={vm.dismiss();editingSale=Sale(concept="",amount=0,date=today.toString())}){Text("+ Venta")}
        OutlinedButton(modifier=Modifier.weight(1f),enabled=!ui.busy,onClick={vm.dismiss();editingExpense=Expense(concept="",amount=0,date=today.toString(),category="otro")}){Text("+ Gasto")}
    }
    OutlinedButton(onClick=openQuotes,modifier=Modifier.fillMaxWidth()){Text("Revisar o corregir cotizaciones")}
    Text("Una entrega registra su venta automáticamente. Las cotizaciones pendientes aún no son ventas. Los pagos se coordinan con la dueña.",style=MaterialTheme.typography.bodySmall)
    Text("Ventas",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    if(sales.isEmpty()) Text("No hay ventas en este período.")
    sales.forEach { sale -> key(sale.id) { Panel {
        Text(sale.concept,style=MaterialTheme.typography.titleMedium)
        Text(money(sale.amount),style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.secondary,fontWeight=FontWeight.Bold)
        Text("${dateLabel(sale.date)} · ${if(sale.orderId==null) "Venta manual" else "Pedido entregado"}",style=MaterialTheme.typography.bodySmall)
        Row {
            TextButton(enabled=!ui.busy,onClick={vm.dismiss();editingSale=sale}){Text("Editar")}
            TextButton(enabled=!ui.busy,onClick={deletingSale=sale}){Text("Eliminar")}
        }
    } } }
    Text("Gastos",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    if(expenses.isEmpty()) Text("No hay gastos en este período.")
    expenses.forEach { expense -> key(expense.id) { Panel {
        Text(expense.concept,style=MaterialTheme.typography.titleMedium)
        Text(money(expense.amount),style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
        Text("${dateLabel(expense.date)} · ${categoryLabels[expense.category] ?: expense.category}",style=MaterialTheme.typography.bodySmall)
        Row {
            TextButton(enabled=!ui.busy,onClick={vm.dismiss();editingExpense=expense}){Text("Editar")}
            TextButton(enabled=!ui.busy,onClick={deletingExpense=expense}){Text("Eliminar")}
        }
    } } }
    editingSale?.let { sale ->
        MovementEditor("Venta",sale.id,sale.concept,sale.amount,sale.date,null,ui,{editingSale=null}) { concept,amount,date,_ ->
            vm.act("Venta guardada",onSaved={editingSale=null}){ShopRules.sale(it,sale.copy(concept=concept,amount=amount,date=date))}
        }
    }
    editingExpense?.let { expense ->
        MovementEditor("Gasto",expense.id,expense.concept,expense.amount,expense.date,expense.category,ui,{editingExpense=null}) { concept,amount,date,category ->
            vm.act("Gasto guardado",onSaved={editingExpense=null}){ShopRules.expense(it,expense.copy(concept=concept,amount=amount,date=date,category=category ?: "otro"))}
        }
    }
    deletingSale?.let { sale -> ConfirmRemoval("¿Eliminar venta?","Se quitará ${sale.concept} por ${money(sale.amount)} de Finanzas. Su pedido, si existe, seguirá entregado y conservará el cupo.",ui.busy,{deletingSale=null}) {
        vm.act("Venta eliminada",onSaved={deletingSale=null}){ShopRules.deleteSale(it,sale.id)}
    } }
    deletingExpense?.let { expense -> ConfirmRemoval("¿Eliminar gasto?","Se quitará ${expense.concept} por ${money(expense.amount)} de Finanzas.",ui.busy,{deletingExpense=null}) {
        vm.act("Gasto eliminado",onSaved={deletingExpense=null}){ShopRules.deleteExpense(it,expense.id)}
    } }
}

private val categoryLabels=linkedMapOf("insumos" to "Insumos","costo_fijo" to "Costo fijo","costo_variable" to "Costo variable","otro" to "Otro")

@Composable private fun MovementEditor(
    title: String,id: String,initialConcept: String,initialAmount: Long,initialDate: String,initialCategory: String?,
    ui: UiState,close: ()->Unit,save: (String,Long,String,String?)->Unit
) {
    var concept by rememberSaveable(id){mutableStateOf(initialConcept)}
    var amount by rememberSaveable(id){mutableStateOf(if(initialAmount==0L) "" else initialAmount.toString())}
    var date by rememberSaveable(id){mutableStateOf(initialDate)}
    var category by rememberSaveable(id){mutableStateOf(initialCategory)}
    AlertDialog(onDismissRequest={if(!ui.busy)close()},title={Text(title)},text={
        Column(Modifier.heightIn(max=440.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Field(concept,{concept=it},"Concepto")
            Field(amount,{amount=it},"Importe CLP sin puntos",numeric=true)
            Field(date,{date=it},"Fecha AAAA-MM-DD")
            if(initialCategory!=null) {
                Text("Categoría",style=MaterialTheme.typography.labelLarge)
                categoryLabels.forEach { (value,label) ->
                    FilterChip(selected=category==value,onClick={category=value},label={Text(label)})
                }
            }
            ui.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
        }
    },confirmButton={TextButton(enabled=!ui.busy,onClick={save(concept,amount.toLongOrNull() ?: -1,date.trim(),category)}){Text("Guardar")}},
        dismissButton={TextButton(enabled=!ui.busy,onClick=close){Text("Volver")}})
}
