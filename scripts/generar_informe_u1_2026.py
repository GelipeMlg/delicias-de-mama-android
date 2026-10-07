from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
from copy import deepcopy
from lxml import etree as E
ROOT=Path(__file__).resolve().parents[1]
REF=ROOT/'Archivos Proyecto Integrado/01 Pautas/Carta-1-vert.docx'
OUT=ROOT/'Archivos Proyecto Integrado/02 Informes Word/Informe_U1_Diagnostico_Delicias_de_Mama_2026.docx'
W='http://schemas.openxmlformats.org/wordprocessingml/2006/main';N={'w':W}
def el(tag,**attrs):
 x=E.Element('{'+W+'}'+tag)
 for k,v in attrs.items():x.set('{'+W+'}'+k,str(v))
 return x
with ZipFile(REF) as z:parts={n:z.read(n) for n in z.namelist()}
tree=E.fromstring(parts['word/document.xml']);body=tree.find('w:body',N);src=list(body)
sect=deepcopy(src[-1]);cover=[deepcopy(x) for x in src[:9]];closing=deepcopy(src[66])
for x in list(body):body.remove(x)
def replace(p,text):
 run=p.find('w:r',N);rp=deepcopy(run.find('w:rPr',N)) if run is not None and run.find('w:rPr',N) is not None else None
 for x in list(p):
  if x.tag!='{'+W+'}pPr':p.remove(x)
 r=el('r')
 if rp is not None:r.append(rp)
 for i,s in enumerate(text.split('\n')):
  if i:r.append(el('br'))
  t=el('t');t.set('{http://www.w3.org/XML/1998/namespace}space','preserve');t.text=s;r.append(t)
 p.append(r);return p
def prop(p):
 pp=p.find('w:pPr',N)
 if pp is None:pp=el('pPr');p.insert(0,pp)
 return pp
def setprop(pp,tag,**attrs):
 old=pp.find('w:'+tag,N)
 if old is not None:pp.remove(old)
 pp.append(el(tag,**attrs))
def para(text,kind='body',newpage=False):
 p=replace(deepcopy(src[24 if kind=='title' else 27 if kind=='sub' else 25]),text);pp=prop(p)
 setprop(pp,'spacing',before=160 if kind=='title' else 100 if kind=='sub' else 0,after=160 if kind=='title' else 100,line=260,lineRule='auto')
 if kind=='title':
  r=p.find('w:r',N);rp=r.find('w:rPr',N)
  if rp is None:rp=el('rPr');r.insert(0,rp)
  setprop(rp,'rFonts',ascii='Tahoma',hAnsi='Tahoma',cs='Tahoma')
 if kind!='body':setprop(pp,'keepNext')
 if newpage:setprop(pp,'pageBreakBefore')
 body.append(p);return p
def page(t):
 spacer=para('',newpage=True);setprop(prop(spacer),'spacing',before=0,after=0,line=240,lineRule='exact');setprop(prop(spacer),'keepNext')
 para(t,'title')
def sub(t):para(t,'sub')
def p(t):para(t)
def table(headers,rows,widths):
 tb=el('tbl');pr=el('tblPr');pr.append(el('tblW',w=sum(widths),type='dxa'));pr.append(el('tblLayout',type='fixed'));borders=el('tblBorders')
 for s in ['top','left','bottom','right','insideH','insideV']:borders.append(el(s,val='single',sz=4,color='D9D9D9'))
 pr.append(borders);margins=el('tblCellMar')
 for s in ['top','bottom']:margins.append(el(s,w=90,type='dxa'))
 for s in ['left','right']:margins.append(el(s,w=110,type='dxa'))
 pr.append(margins);tb.append(pr);grid=el('tblGrid')
 for w in widths:grid.append(el('gridCol',w=w))
 tb.append(grid)
 for i,row in enumerate([headers]+rows):
  tr=el('tr');trp=el('trPr');trp.append(el('cantSplit'))
  if i==0:trp.append(el('tblHeader'))
  tr.append(trp)
  for text,w in zip(row,widths):
   tc=el('tc');tcp=el('tcPr');tcp.append(el('tcW',w=w,type='dxa'))
   if i==0:tcp.append(el('shd',fill='EEEEEE'))
   tc.append(tcp);q=replace(deepcopy(src[25]),text);setprop(prop(q),'spacing',before=0,after=0,line=245,lineRule='auto')
   if i==0:
    for rp in q.findall('w:r/w:rPr',N):rp.append(el('b'))
   tc.append(q);tr.append(tc)
  tb.append(tr)
 body.append(tb);para('')
replace(cover[2],'DIAGNÓSTICO DEL PROBLEMA INFORMÁTICO')
replace(cover[3],'Delicias de Mamá')
replace(cover[8],'NOMBRE: Felipe Alvarado, Jafet Lavados y Matias Otero\nCARRERA: Analista Programador\nASIGNATURA: Proyecto Integrado TI · TIHV43\nPROFESOR: Por confirmar\nFECHA: 7 de octubre de 2026 · Versión para revisión')
for x in cover:body.append(x)
page('Resumen ejecutivo')
p('Delicias de Mamá necesita coordinar encargos personalizados, disponibilidad, cotizaciones y registros de ventas y gastos. El levantamiento original identifica estos procesos como el núcleo de la solución [2]. Diagnosticamos como necesidad central mantener información compartida y coherente entre la dueña y sus clientes, con permisos diferenciados y reglas que eviten comprometer más pedidos que la capacidad disponible.')
p('Nuestra propuesta es una aplicación Android en Kotlin y Jetpack Compose, conectada a Supabase para autenticación, almacenamiento y sincronización. Firebase Cloud Messaging comunica eventos de pedidos y cotizaciones. Ya existe una versión funcional con catálogo, imágenes, personalización, agenda, cotizaciones y finanzas. Este avance permite contrastar las decisiones con una implementación concreta, aunque no acredita por sí solo aceptación de la contraparte ni operación comercial sin riesgos [3].')
p('El diagnóstico conserva los siete requisitos funcionales originales, precisa su aceptación y añade catálogo, acceso por rol y notificaciones como ampliaciones. Priorizamos mediante MoSCoW y desarrollamos seis historias principales. Adoptamos el modelo incremental para analizar, implementar, probar y ajustar entregas sucesivas.')
p('El proyecto es viable como desarrollo académico. La adopción operativa requiere confirmar reglas comerciales, registrar validación con la dueña, medir tiempos y ensayar recuperación. Las 39 pruebas unitarias disponibles respaldan reglas locales; no sustituyen esas comprobaciones. La entrega U1 se estima para el 27 de octubre de 2026, sujeta al plazo oficial.')
sub('Contenido del informe')
p('1 Introducción\n2 Identificación de antecedentes\n3 Planificación del levantamiento\n4 Especificación de necesidades e historias\n5 Metodologías y decisiones TI\n6 Seguridad ética y normativa\n7 Viabilidad y planificación\n8 Conclusiones y cobertura de criterios\nReferencias\nAnexo de validación')
page('1 Introducción')
p('Este informe corresponde a la Unidad 1, Diagnóstico del problema informático, de Proyecto Integrado TI. Fundamenta las necesidades del negocio según los criterios 1.1.1 a 1.1.5 de la guía TIHV43 [1]. Aunque priorizamos desarrollar la app, esta entrega explica el problema y las decisiones que sustentan la solución; los avances técnicos son antecedentes y no resultados de una evaluación final.')
sub('1 1 Objetivo general')
p('Diagnosticar y priorizar las necesidades de información y coordinación de Delicias de Mamá para sustentar una solución Android que apoye pedidos personalizados, disponibilidad, cotizaciones y registros financieros, resguardando los datos de sus usuarios.')
sub('1 2 Objetivos específicos')
p('Identificar actores, procesos y restricciones con las fuentes disponibles. Clasificar requisitos funcionales y no funcionales con aceptación observable. Formular historias de usuario y prioridades. Justificar metodología y tecnologías adecuadas. Determinar riesgos, compromisos éticos y validaciones necesarias para cerrar el diagnóstico.')
sub('1 3 Alcance y fuentes')
p('El alcance comprende clientes, dueña, catálogo, solicitudes, cotizaciones, aceptación, agenda y finanzas. La oferta incluye repostería y coctelería dulce y salada; el modo de pastel es opcional. Quedan fuera de esta versión pasarela de pagos, abonos, fidelización, inventario completo y rutas de despacho. Su inclusión requiere análisis y acuerdo.')
p('El informe original del 1 de septiembre menciona entrevista y observación, pero no adjunta transcripciones o actas verificables. Lo utilizamos como antecedente del levantamiento; las precisiones recibidas durante el desarrollo se registran como evolución y el código como evidencia técnica. No atribuimos nuevas entrevistas, aprobaciones ni mediciones a la dueña [2–3].')
sub('1 4 Criterio de evidencia')
p('Distinguimos implementación en archivos, comprobación técnica y validación pendiente con la contraparte. Una función programada no demuestra por sí misma una mejora económica. Sede, sección, docente de esta asignatura y plazo definitivo deben confirmarse antes de emitir la versión de entrega. La regla de diapositivas se aplicará únicamente a la exposición final.')
page('2 Identificación de antecedentes')
sub('2 1 Contexto tecnológico y administrativo')
p('Delicias de Mamá es una PYME gastronómica que recibe encargos con características y fechas específicas. La dueña concentra decisiones sobre oferta, cupos y cotizaciones. El informe inicial describe gestión manual, pero no mide errores, volumen mensual, ventas perdidas ni inventario de equipos. Tampoco acredita dirección, dotación o canales específicos. Esos datos permanecen por confirmar [2].')
p('El equipo dispone de PC de desarrollo, Android Studio, app instalable, Supabase, Firebase y GitHub privado. Son recursos del proyecto, no infraestructura acreditada de la empresa. La operación conectada requiere Android e Internet y una contingencia acordada. El sistema debe adaptarse a la forma de cotizar de la dueña y permitir correcciones, sin imponer cálculos automáticos no validados.')
table(['Necesidad','Efecto o riesgo','Respuesta priorizada'],[
['N1 Coordinar cupos','Comprometer encargos sin una referencia común de capacidad.','Agenda compartida y control de disponibilidad.'],
['N2 Precisar solicitudes','Detalles incompletos o interpretaciones distintas.','Fecha, cantidad y personalización estructuradas.'],
['N3 Mantener cotización vigente','Confundir el importe aceptado con una versión anterior.','Revisión de detalle, monto y estado.'],
['N4 Actualizar la oferta','Mostrar productos u opciones que ya no se venden.','CRUD, retiro y fotografías.'],
['N5 Consultar movimientos','Dificultad para reunir ventas y gastos por período.','Registros editables y resumen operativo.'],
['N6 Proteger y compartir','Información aislada o acceso sin autorización.','Persistencia compartida, roles y recuperación.']],[2300,4300,4000])
p('Los impactos son riesgos derivados de las fuentes y no pérdidas cuantificadas. No asignamos frecuencia ni monto sin medición.')
sub('2 2 Actores y cultura de uso')
p('El cliente consulta oferta y gestiona sus pedidos; la dueña administra catálogo, agenda, cotizaciones y finanzas. El equipo mantiene código y configuración con acceso técnico restringido. Proponemos aprendizaje mediante tareas reales, lenguaje sencillo y confirmaciones antes de acciones importantes. Debe conservarse un canal alternativo para quienes no tengan Android o conectividad.')
page('3 Planificación del levantamiento')
p('Se realiza análisis documental y contraste con los flujos implementados. Para cerrar vacíos se propone entrevista semiestructurada y observación guiada con la dueña. No se presenta una encuesta como aplicada. Revisar un pedido completo permite identificar reglas y excepciones, aunque no produce conclusiones estadísticas sobre todos los clientes.')
table(['Actividad','Información y evidencia','Ejecución'],[
['Revisión documental','Necesidades originales, cambios y discrepancias. Matriz de trazabilidad.','Equipo; realizada para esta versión.'],
['Entrevista de contraste','Cupos, anticipación, cotización, cancelación, entrega y finanzas. Acta de respuestas.','Dueña y equipo; 45–60 min propuestos.'],
['Observación guiada','Crear producto, corregir cotización y revisar agenda. Registro de dificultades.','Sesión propuesta de 30–45 min.'],
['Prueba de dos roles','Pedido y respuesta entre dispositivos con datos ficticios.','Equipo; registrar antes del cierre.'],
['Priorización y acuerdo','Reconciliar solicitado, implementado y diferido.','Validación con contraparte pendiente.']],[2400,5000,3200])
sub('3 1 Análisis de los cinco porqués')
p('Problema analizado: riesgo de comprometer pedidos sin comprobar capacidad. ¿Por qué puede ocurrir? Porque solicitudes y cupos pueden consultarse por separado. ¿Por qué quedan separados? Porque los registros no comparten necesariamente un estado único. ¿Por qué no basta un registro común? Porque debe definirse cuándo se ocupa y libera el cupo. ¿Por qué puede fallar esa regla? Porque solicitudes simultáneas requieren validación del servidor. ¿Por qué validarla con la dueña? Porque el cupo técnico debe representar carga real de producción. Esta cadena es una hipótesis del equipo, no respuestas textuales de entrevista.')
sub('3 2 Resguardo y análisis')
p('Solicitaremos autorización para registrar comentarios y evitaremos copiar conversaciones o datos personales al repositorio. Cada hallazgo se vinculará con necesidad, requisito y decisión. La contraparte revisará la síntesis para reducir sesgos y documentar desacuerdos. Datos pendientes: capacidad por fecha/tipo de producto, anticipación, vigencia de cotización, cancelaciones, pagos, horarios y retiro/despacho. El anexo aporta preguntas sin inventar resultados.')
page('4 Especificación de necesidades')
sub('4 1 Requisitos funcionales')
p('Conservamos RF-01 a RF-07 y precisamos sus resultados. RF-08 a RF-10 registran ampliaciones del desarrollo. La prioridad es propuesta del equipo para contrastar con la dueña: M es imprescindible y S es importante [2–3].')
table(['ID','Requisito','Criterio de aceptación'],[
['RF-01 · M','Solicitud','Cliente autenticado envía producto, cantidad, fecha y hora válidos; se confirma guardado y recupera su pedido.'],
['RF-02 · M','Disponibilidad','Dueña define capacidad y bloqueos; no se excede cupo y cancelar libera ocupación según regla acordada.'],
['RF-03 · M','Personalización','Modo pastel opcional; cliente elige un sabor, un relleno si existe y porciones habilitadas.'],
['RF-04 · M','Cotización','Dueña propone o corrige importe; cliente revisa detalle vigente. Se rechaza aceptación de revisión antigua.'],
['RF-05 · M','Ventas','Dueña registra, consulta y corrige; filtros respetan período. Entregar no duplica la venta.'],
['RF-06 · M','Gastos','Registrar, editar y eliminar gastos válidos. Mostrar ventas menos gastos, sin llamarlo utilidad neta.'],
['RF-07 · M','Persistencia','Cambios guardados en Supabase se recuperan desde otro dispositivo con sesión autorizada.'],
['RF-08 · M','Catálogo','Solo dueña crea, edita y retira productos. Pedidos previos conservan referencia; opciones se muestran al cliente.'],
['RF-09 · M','Acceso por rol','Cliente no se autoconcede rol de dueña ni accede a finanzas o pedidos ajenos.'],
['RF-10 · S','Notificaciones','Con permiso y sesión vigente, destinatario recibe avisos pertinentes también en segundo plano.']],[1500,2400,6700])
sub('4 2 Épicas y prioridades MoSCoW')
p('E1: solicitar y coordinar encargos, RF-01 a RF-04. E2: administrar oferta y registros, RF-05, RF-06 y RF-08. E3: compartir información segura, RF-07, RF-09 y RF-10. Los M sostienen el flujo y seguridad; los S mejoran comunicación y presentación, incluidas fotos. Como C se consideran refinamientos visuales y filtros adicionales. En W, fuera de esta entrega, quedan pagos en línea, puntos e itinerarios de despacho. La prioridad refleja valor y dependencia, no orden histórico de programación.')
page('4 Especificación de necesidades')
sub('4 3 Requisitos no funcionales')
table(['ID','Condición verificable','Estado y comprobación'],[
['RNF-01','Android nativo desde API 26.','Configurado; registrar prueba en Android 8 y una versión reciente.'],
['RNF-02','Identidad y rol, sesiones protegidas y recuperación documentada.','Controles presentes; verificar permisos remotos y restauración.'],
['RNF-03','Tareas comprensibles, textos legibles y errores claros.','Evaluar con dueña, texto ampliado y accesibilidad.'],
['RNF-04','Consultar y guardar en menos de 2 s bajo condiciones definidas.','Meta no medida. Proponer 30 repeticiones, red/dispositivo y volumen registrados; informar p95 y errores.'],
['RNF-05','No exceder cupos ni aceptar cotización obsoleta ante cambios concurrentes.','Reglas y RPC presentes; comprobar desde dos sesiones.'],
['RNF-06','Código, SQL y documentación versionados; instalación reproducible.','GitHub privado y manuales disponibles; ensayar recuperación.']],[1600,4300,4700])
p('RNF-01 a RNF-04 provienen del levantamiento; RNF-05 y RNF-06 hacen explícitas necesidades técnicas de consistencia y mantenimiento. p95 es el tiempo por debajo del cual termina el 95 % de las mediciones. La muestra propuesta no demuestra rendimiento bajo toda carga.')
sub('4 4 Reglas de negocio y límites')
p('La implementación cuenta un pedido no cancelado como ocupación de su fecha. Una solicitud ya utiliza cupo y una cancelación lo libera. Debe validarse si ese criterio representa adecuadamente esfuerzos distintos de tortas y coctelería. Cotizar no cobra dinero y aceptar no demuestra pago. Los estados son solicitado, cotizado, confirmado, entregado y cancelado; preparación y listo para retiro no están implementados.')
p('La diferencia entre ventas y gastos es un indicador operativo. No incluye necesariamente impuestos, inventario, depreciación ni conciliación y no debe presentarse como utilidad neta certificada. La app permite correcciones, pero no sustituye un sistema contable. Respaldo de archivos en GitHub tampoco equivale a respaldo de datos, usuarios o imágenes de Supabase.')
p('Cada cambio de alcance debe registrar fecha, solicitante, motivo, requisitos afectados, impacto y decisión. Una idea de una maqueta no se convierte automáticamente en compromiso del proyecto.')
page('4 Historias de usuario de coordinación')
sub('HU 01 Solicitar un pedido')
p('Como cliente, quiero seleccionar producto, fecha y características para enviar un encargo con información suficiente para cotizar. E1 · RF-01 y RF-03 · Prioridad M.')
p('Aceptación: dado un producto activo y fecha habilitada, al enviar datos válidos se guarda la solicitud y permite consultarla. Si faltan datos o capacidad, se informa el motivo sin anunciar éxito. En modo pastel se selecciona un sabor y un relleno cuando corresponda; otros productos no obligan a elegirlos. Verificación: solicitud válida e inválida y comparación de datos recuperados por cliente y dueña.')
sub('HU 02 Controlar la capacidad')
p('Como dueña, quiero configurar disponibilidad y consultar pedidos comprometidos para organizar la producción sin exceder la capacidad acordada. E1 · RF-02 y RNF-05 · Prioridad M.')
p('Aceptación: bloquear una fecha impide nuevas operaciones incompatibles. No se reduce capacidad por debajo de la ocupación comprometida. Cancelar libera el cupo correspondiente. Dos sesiones sobre el último cupo no superan el límite y reciben un resultado comprensible. Verificación: fecha ficticia con un cupo, dos sesiones y cancelación posterior. Las pruebas locales no sustituyen el ensayo concurrente remoto.')
sub('HU 03 Revisar la cotización vigente')
p('Como cliente, quiero revisar detalle e importe actualizado para confirmar lo que acordé con la dueña. E1 · RF-04 y RF-10 · Prioridad M para cotización y S para aviso.')
p('Aceptación: dueña emite importe positivo y cliente ve estado cotizado. Aceptar una revisión vigente confirma el pedido. Si cambian monto, revisión o estado mientras se revisa, se bloquea esa aceptación. El aviso comunica, pero no sustituye el registro ni constituye cobro. Verificación: abrir revisión, cambiar importe desde la cuenta dueña y comprobar rechazo de la versión antigua. Las seis pruebas OrderPresentationTest respaldan parte de esta regla [3].')
p('Estas historias cubren el recorrido comercial principal. Su aceptación remota se registrará con datos de prueba, fecha, versión y resultado observado. No se atribuye conformidad final a la dueña por estar implementadas.')
page('4 Historias de administración y seguridad')
sub('HU 04 Mantener el catálogo')
p('Como dueña, quiero crear, editar y retirar productos con fotos y opciones para mostrar lo que puedo preparar. E2 · RF-08 y RF-03 · Prioridad M.')
p('Aceptación: lo guardado se recupera desde Supabase y aparece según disponibilidad. Modo pastel habilita alternativas de sabor y relleno; la configuración de porciones no obliga a activar ese modo. Puede corregirse información y foto. Si hay pedidos asociados, retirar conserva el historial. Un cliente no puede modificar catálogo. Verificación: alta, edición y retiro de producto ficticio con lectura desde otro teléfono y prueba de permisos.')
sub('HU 05 Revisar y corregir movimientos')
p('Como dueña, quiero registrar y corregir ventas y gastos para consultar un resumen por período y subsanar errores de digitación. E2 · RF-05 y RF-06 · Prioridad M.')
p('Aceptación: importes inválidos se rechazan; editar reemplaza sin duplicar. Los filtros respetan fechas y los totales corresponden a los registros del período. Entregar genera la venta vinculada una sola vez. Una cotización cerrada no se confunde con corregir su venta. Verificación: operaciones ficticias con sumas conocidas, edición, filtros y eliminación; comparar esperado y observado.')
sub('HU 06 Acceder de forma segura')
p('Como cliente o dueña, quiero iniciar sesión y conservarla solo si lo elijo para acceder a mis funciones sin exponer datos a otras cuentas. E3 · RF-07, RF-09, RF-10 y RNF-02 · Prioridad M para acceso y aislamiento.')
p('Aceptación: Supabase Auth verifica identidad y el servidor verifica privilegio de dueña. Registrarse no concede ese rol. Con persistencia elegida se recupera sesión; cerrar o cambiar cuenta no debe mostrar datos ni avisos previos. Interfaz y servidor deben aplicar los mismos límites. Verificación: dos clientes y una dueña, intentos no autorizados y cambio de sesión; comprobar RLS y RPC, no solo controles visuales.')
sub('Vinculación con la validación')
p('Las historias permiten revisar beneficio, comportamiento y evidencia sin convertirlas en especificaciones de pantalla. Se vincularán a los casos de prueba de las siguientes unidades. Si la dueña cambia una regla, se actualizarán juntos requisito, historia, prueba y documentación, manteniendo el registro de la decisión.')
page('5 Metodologías y decisiones TI')
sub('5 1 Elección del modelo incremental')
p('Adoptamos el modelo incremental porque permite revisar conjuntos útiles antes de ampliar alcance. Cada incremento incluye análisis, diseño, implementación y pruebas, y se integra con lo anterior. La evolución del proyecto comprende prototipo local, acceso por rol, datos compartidos, fotos y notificaciones. Cada entrega se identifica por su alcance, criterios de aceptación y evidencia de revisión.')
table(['Alternativa','Evaluación','Decisión'],[
['Cascada','Facilita planificación inicial; cambios de personalización exigen revisar etapas cerradas.','No elegida como enfoque principal.'],
['Incremental','Permite validar utilidad y reglas por entrega, con regresión de lo anterior.','Enfoque principal.'],
['Prototipado y diseño centrado en usuario','Ayudan a detectar confusión en formularios y navegación.','Técnicas complementarias.']],[2500,5600,2500])
p('El riesgo es acumular solicitudes sin cerrar aceptación. Lo controlaremos con prioridades, impacto de cambios, revisión y versiones identificables. Git y pruebas apoyan colaboración, pero no se afirma una canalización automática de despliegue ni certificación de estándares. Se mantiene la secuencia requisitos, diseño, construcción y verificación como buena práctica del ciclo de vida.')
sub('5 2 Lenguaje datos e infraestructura')
p('Kotlin y Jetpack Compose mantienen Android nativo y reutilizan el desarrollo actual. PostgreSQL en Supabase permite relaciones, restricciones y operaciones del servidor para pedidos y movimientos. Auth identifica usuarios, Storage guarda imágenes y Realtime facilita detectar cambios. Firebase Cloud Messaging transporta avisos. Los servicios administrados reducen administración de servidores propios; el equipo sigue a cargo de permisos, secretos y recuperación [3–4].')
sub('5 3 Arquitectura de la solución')
p('La app separa pantallas Compose, coordinación en ShopViewModel, reglas del dominio y repositorios. Las RPC son funciones del servidor que aplican operaciones y autorización. Esta separación permite probar reglas y evolucionar acceso a datos. La arquitectura 4+1 y el modelo actualizado se detallarán en U2; aquí se fundamentan decisiones frente al problema. GitHub conserva código e informes, no una copia automática de la base de datos.')
page('6 Seguridad ética y normativa')
sub('6 1 Riesgos y controles')
table(['Riesgo','Control','Evidencia pendiente'],[
['Acceso a datos ajenos','Identidad, rol y autorización RLS/RPC; ocultar botones no basta.','Pruebas negativas remotas por cuenta y operación.'],
['Exposición de credenciales','Hash bcrypt en Supabase Auth, sesiones protegidas y persistencia opcional.','Revisar configuración y cambio/cierre de sesión.'],
['Filtración por código o avisos','Secretos fuera de APK/Git y contenido mínimo en notificaciones.','Revisar pantalla bloqueada, logs y publicaciones.'],
['Pérdida o inconsistencia','Reglas del servidor, revisión de cotización y copia recuperable.','Concurrencia y restauración aislada.']],[2600,4900,3100])
p('Bcrypt es un hash no reversible: Supabase Auth almacena verificadores de contraseñas, no necesita recuperarlas como texto. Android usa ese servicio y no construye un almacén propio [4]. El proveedor no garantiza por sí solo una política robusta ni permisos correctamente configurados.')
sub('6 2 Privacidad ciudadanía y responsabilidad')
p('Se recopilará lo necesario para el encargo. Las restricciones alimentarias pueden revelar salud: minimizar detalle, restringir acceso y explicar finalidad. La dueña debe definir conservación y atención de correcciones o eliminación. Capturas y pruebas usarán información ficticia o anonimizada. No se publicarán pedidos reales, teléfonos ni claves. Se propone atención alternativa para personas sin Android o conectividad, además de evaluar lectura y comprensión de la interfaz.')
p('Ante incidentes, el equipo debe comunicar hechos y límites, preservar evidencia y evitar restauraciones que destruyan operaciones recientes. La contingencia manual requiere custodia y posterior conciliación. Usar servicios externos no elimina la responsabilidad sobre los datos.')
sub('6 3 Normativa pertinente')
p('La Ley 19.628 es una referencia directa para datos personales. El texto oficial consultado de la Ley 21.719 indica vigencia de la reforma el 1 de diciembre de 2026. Debe revisarse antes de entregar y operar: existe una propuesta gubernamental de postergación, que no equivale por sí sola a una modificación promulgada [5–7]. No declaramos cumplimiento legal integral a partir del código.')
p('La Ley 21.663 se analiza según su ámbito de servicios esenciales y operadores de importancia vital; no se presume aplicación directa a la PYME por usar nube [8]. El enlace al Decreto 7 en la guía conduce a un reglamento de protección especializada de niñez, por lo que no se usa como fundamento de ciberseguridad [9].')
page('7 Viabilidad y planificación')
sub('7 1 Justificación de viabilidad')
p('Técnica. Existe app compilable, integración de servicios y 39 pruebas unitarias aprobadas. Esto respalda la capacidad de continuar y entregar el desarrollo académico. Persisten pruebas remotas de concurrencia, accesibilidad, compatibilidad y recuperación; no se considera demostrada toda la confiabilidad necesaria para explotación comercial [3].')
p('Económica. Reutilizar equipos y código reduce nuevas adquisiciones académicas. El trabajo tiene costo de tiempo y la operación exige conectividad, mantenimiento, respaldo y revisar límites de servicios. No existe presupuesto aprobado ni ahorro medido. Proponemos estimar costo mensual total y comparar horas administrativas y correcciones antes/después de un piloto. No se presenta un retorno asegurado ni gratuidad ilimitada de la nube.')
p('Operativa. La solución responde a coordinación, cotización y control identificados. La edición de productos y movimientos facilita adaptación. La adopción depende de que la dueña complete tareas con poca ayuda y tenga una alternativa ante falta de red. Su aceptación no se reemplaza por la opinión del equipo. La conclusión es viabilidad académica fundamentada y validación operativa pendiente.')
sub('7 2 Plan de tres semanas para U1')
p('Plan propuesto desde el 7 de octubre, con entrega tentativa el 27 de octubre de 2026. No describe tareas ya ejecutadas ni el plazo completo del software. El equipo responsable es Felipe Alvarado, Jafet Lavados y Matias Otero; la distribución individual debe acordarse.')
table(['Período','Producto de trabajo','Cierre esperado'],[
['7–13 octubre','Contrastar entrevista, reglas y alcance; precisar historias.','Acta real, observaciones y datos académicos.'],
['14–20 octubre','Relacionar requisitos con app, evidencia, seguridad y viabilidad.','Matriz actualizada y resultados sin datos privados.'],
['21–27 octubre','Corregir informe y referencias, revisar formato y defensa individual.','Revisión del equipo y carga según plazo oficial.']],[2000,4800,3800])
p('Se propone un responsable de levantamiento, uno de contraste funcional y uno de seguridad/documentación, con revisión cruzada. Los nombres por función se registrarán al acordarlos, sin atribuir aportes pasados. Los recursos necesarios para el cierre son equipo de trabajo, disponibilidad de la dueña, dos dispositivos de prueba, conectividad y acceso autorizado al repositorio y servicios.')
page('8 Conclusiones y recomendaciones')
p('El diagnóstico identifica la necesidad de coordinación compartida y protección de información. La respuesta debe integrar solicitud, disponibilidad, cotización y registros de la dueña. La app actual demuestra que la propuesta es técnicamente abordable y que Android y nube responden al intercambio entre dispositivos, en lugar de mantener información aislada.')
p('La prioridad U1 es consolidar diagnóstico, confirmar reglas y cerrar aceptación con metodología incremental. Agregar funciones sin validar aumentaría alcance sin resolver vacíos de evidencia. Continuidad, seguridad y utilidad deben ser requisitos del negocio. No concluimos reducción de errores ni aumento de rentabilidad porque faltan línea base y piloto medidos.')
p('Recomendamos revisión guiada con la contraparte, comparación entre resultados esperados y observados y trazabilidad entre requisito, implementación y prueba. Los cambios se incorporarán al documento con sus evidencias. Los informes anteriores se conservan como antecedentes y esta versión actualiza las afirmaciones de operación exclusivamente local.')
sub('8 1 Correspondencia con criterios U1')
table(['Criterio','Respuesta en el informe'],[
['1.1.1 Necesidades TI','Secciones 2 y 3: contexto, efectos, actores y recopilación.'],
['1.1.2 Historias de usuario','Secciones 4: seis historias con aceptación y verificación.'],
['1.1.3 Requisitos clasificados','Secciones 4.1–4.4: RF/RNF, prioridades, épicas y alcance.'],
['1.1.4 Metodologías factibles','Secciones 5 y 7: alternativas, incremental, tecnologías y viabilidad.'],
['1.1.5 Seguridad y ética','Sección 6: riesgos, controles, privacidad, inclusión y normas.']],[3500,7100])
sub('8 2 Estado de la evidencia')
p('Los reportes del 1 de octubre contienen 39 pruebas sin fallos ni errores. El 6 de octubre Gradle informó BUILD SUCCESSFUL reutilizando 43 tareas vigentes. El usuario confirmó imágenes en vivo y avisos en segundo plano. Esto no acredita todo el recorrido de aceptación, respuesta menor a dos segundos ni restauración probada [3].')
p('Antes de emitir la versión definitiva faltan docente, sede/sección, plazo oficial, asignación individual y registro de validación con la contraparte. La presentación oral final se prepara en otra etapa; no reemplaza el informe ni sus fuentes.')
page('Referencias y fuentes de evidencia')
refs=[
'[1] INACAP. Guía ABPro Unidad 1, Proyecto Integrado TIHV43. Edición indicada Primavera 2025. Documento proporcionado por el equipo; criterios y actividades de diagnóstico.',
'[2] Alvarado, F., Lavados, J. y Otero, M. (2026). InformeTrabajoIngSoft(V2). Levantamiento original del 1 de septiembre de 2026. Registra entrevista y observación sin adjuntar acta o transcripción.',
'[3] Equipo Delicias de Mamá. Código Android, migraciones y documentación revisados entre el 6 y 7 de octubre de 2026. Repositorio privado: https://github.com/GelipeMlg/delicias-de-mama-android. Evidencias: docs/estado-actual.md, docs/mejoras-pedidos-octubre.md y reportes JUnit en Archivos Proyecto Integrado/04 Evidencias.',
'[4] Supabase. Password security. Consulta del 6 de octubre de 2026. https://supabase.com/docs/guides/auth/password-security',
'[5] Biblioteca del Congreso Nacional de Chile. Ley 19.628. Texto oficial consultado el 6 de octubre de 2026. https://www.bcn.cl/leychile/navegar?idNorma=141599',
'[6] Biblioteca del Congreso Nacional de Chile. Ley 21.719. Texto oficial consultado el 6 de octubre de 2026. https://www.bcn.cl/leychile/navegar?idNorma=1209272',
'[7] Ministerio de Economía, Fomento y Turismo. (2026, 1 de septiembre). Gobierno propone ampliar plazo para implementar nueva Ley de Protección de Datos y su institucionalidad. Anuncio de propuesta, no reforma promulgada. https://www.economia.gob.cl/2026/09/01/gobierno-propone-ampliar-plazo-para-implementar-nueva-ley-de-proteccion-de-datos-y-institucionalidad.htm',
'[8] Biblioteca del Congreso Nacional de Chile. Ley 21.663, artículo 4. Consulta del 6 de octubre de 2026. https://www.bcn.cl/leychile/navegar?idNorma=1202434',
'[9] Biblioteca del Congreso Nacional de Chile. Decreto 7, Ministerio de Desarrollo Social y Familia, reglamento de Ley 20.032. Enlace de la guía verificado el 6 de octubre de 2026 para descartar su uso como norma de ciberseguridad. https://www.bcn.cl/leychile/navegar?idNorma=1192704'
]
for t in refs:p(t)
p('Las tablas e historias son elaboración del equipo sobre [2–3] y requieren la validación indicada. No contienen credenciales ni datos de pedidos reales. La plantilla institucional Carta-1-vert.docx se conserva como base de formato.')
page('Anexo Instrumento de validación')
p('Instrumento propuesto para contrastar el diagnóstico. No es una entrevista realizada ni acta de aceptación. Registrar fecha, participantes, autorización y respuestas en un archivo separado e incorporar solo la síntesis autorizada.')
table(['Pregunta a la contraparte','Decisión que permite tomar'],[
['¿Cómo recibe, registra y consulta un encargo?','Confirmar proceso y canales sin suponer herramientas.'],
['¿Qué errores o demoras ocurren y con qué frecuencia?','Levantar línea base con ejemplos anonimizados.'],
['¿Qué cuenta como cupo y cuándo se ocupa?','Alinear agenda con carga real de producción.'],
['¿Qué anticipación, horarios y bloqueos necesita?','Precisar reglas de solicitud y entrega.'],
['¿Cuándo vence la cotización y cómo se acepta?','Definir vigencia, cambios y confirmación.'],
['¿Cómo gestiona cancelaciones, abonos y devoluciones?','Delimitar alcance y pendientes.'],
['¿Qué productos necesitan sabor, relleno o porciones?','Validar modo pastel y coctelería salada.'],
['¿Cuándo registra la venta y qué resumen utiliza?','Acordar indicadores sin confundir utilidad contable.'],
['¿Qué datos conserva, cuánto tiempo y quién accede?','Definir privacidad y responsabilidades.'],
['¿Qué hará si la app o Internet no están disponibles?','Acordar contingencia y recuperación.']],[5700,4900])
sub('Registro de cierre')
p('Anotar acuerdos, desacuerdos, cambios solicitados, prioridades y responsables de revisión. Pedir confirmación de la síntesis a la contraparte. En la observación de tareas, registrar pasos completados, dificultades y ayuda necesaria; no afirmar satisfacción ni éxito sin resultado. La información permitirá actualizar requisitos e historias antes de entregar U1.')
pp=prop(closing);setprop(pp,'pageBreakBefore');setprop(pp,'spacing',before=1440)
body.append(closing);body.append(sect)
parts['word/document.xml']=E.tostring(tree,xml_declaration=True,encoding='UTF-8',standalone=True)
with ZipFile(OUT,'w',ZIP_DEFLATED) as z:
 for n,data in parts.items():z.writestr(n,data)
with ZipFile(REF) as a,ZipFile(OUT) as b:
 assert all(a.read(n)==b.read(n) for n in a.namelist() if n!='word/document.xml')
 assert E.tostring(E.fromstring(a.read('word/document.xml')).find('w:body/w:sectPr',N))==E.tostring(E.fromstring(b.read('word/document.xml')).find('w:body/w:sectPr',N))
print('Informe creado; partes no editables y geometria preservadas.')
