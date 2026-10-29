# Declaración de herramientas y recursos externos

## Laboratorio 13: Pedidos que llegan a su servidor

## Inteligencia artificial

Se utilizó ChatGPT (OpenAI) como apoyo para comprender los requisitos del laboratorio, revisar la integración de Retrofit y kotlinx.serialization, diseñar los estados de envío y error, adaptar las pruebas automatizadas y revisar los comandos de Git.

Las decisiones finales de implementación, la ejecución de la aplicación, las pruebas en el emulador, las comprobaciones manuales y la validación de los resultados corresponden al equipo.

También se utilizó Claude (Anthropic) como apoyo durante el desarrollo y revisión de cambios del laboratorio.

## Otros recursos

Se consultó documentación oficial de Android Developers sobre ViewModel, `viewModelScope`, StateFlow, pruebas instrumentadas y Network Inspector.

Se consultó la documentación oficial de Retrofit y kotlinx.serialization para las anotaciones de las rutas, la conversión de JSON y los DTOs serializables.

También se consultó la documentación de MockAPI para crear el recurso `orders`, enviar pedidos, consultar datos y ordenar los registros.

No se copió código de repositorios de terceros ni de tutoriales.

## Laboratorio final: Repository e inyección manual

### Decisión de arquitectura

Se conservó la integración de Firebase Authentication y Firestore
realizada previamente. Se encapsularon Room, DataStore y Retrofit
mediante contratos de Repository e implementaciones concretas.

Los contratos se encuentran en el paquete existente
`domain.repository`. Las implementaciones permanecen en los paquetes
de datos correspondientes.

| Contrato | Implementación | Responsabilidad |
| --- | --- | --- |
| AuthRepository | FirebaseAuthRepository | Autenticación anónima |
| ProductRepository | FirestoreProductRepository | Catálogo de Firebase |
| FavoritesRepository | RoomFavoritesRepository | Favoritos |
| CartRepository | RoomCartRepository | Pedido en curso |
| CatalogPreferencesRepository | DataStoreCatalogPreferencesRepository | Orden del catálogo |
| OrdersRepository | RetrofitOrdersRepository | Confirmación, historial y detalle en MockAPI |

Favoritos, carrito y preferencias tienen contratos separados por
responsabilidad. La creación, consulta y detalle de pedidos comparten
OrdersRepository porque utilizan el mismo recurso remoto.

### Modelos y errores de dominio

Los Repositories entregan identificadores, OrderItem, CatalogSortOrder,
Product y los modelos Order, OrderLine y NewOrder según la operación.

Las entidades de Room, las preferencias de DataStore y los DTOs de
Retrofit quedan encapsulados en sus implementaciones y mapeos.

OrdersRepository devuelve OrderResult. Los fallos se representan con
OrderError: NETWORK, SERVER, INVALID_DATA, NOT_FOUND y UNKNOWN.

RetrofitOrdersRepository transforma las excepciones de transporte
y serialización a estos errores de dominio y propaga la cancelación
de las coroutines.

### Inyección manual

AppContainer crea y comparte las implementaciones de Repository.
Las factories construyen StoreViewModel y OrdersViewModel mediante
inyección por constructor.

MainActivity obtiene ambos ViewModels con sus factories y los entrega
a StoreApp.

Se reutilizan las tablas, claves y valores de almacenamiento existentes
para conservar los datos guardados. Las reglas de existencias y las
validaciones del formulario se mantienen. Un fallo de envío conserva
el pedido y el formulario; un envío exitoso los limpia.

### Evidencia de separación de capas

Se realizó una búsqueda recursiva en `app/src/main/java`, limitada
a pantallas, ViewModels, estados de UI, StoreApp y MainActivity.

Resultado: cero imports de androidx.room3, androidx.datastore,
retrofit2 y com.google.firebase en los archivos revisados.

### Reserva de existencias y reglas de seguridad

Agregar al pedido reserva la unidad en Firestore con `runTransaction`:
la transacción lee las existencias en el servidor y solo las descuenta
si alcanzan. Si otro cliente cambia el producto entre la lectura y la
escritura, Firestore vuelve a ejecutar la transacción con el valor nuevo.
Disminuir una cantidad o eliminar una línea devuelve las unidades con
otra transacción, y confirmar el pedido no cambia las existencias.

El detalle del producto conserva la acción «Prueba de concurrencia»,
que envía 20 reservas a la vez sin tocar el pedido en curso.

Las reglas definitivas están en `firestore.rules`: solo un usuario con
sesión lee y modifica productos, nadie los crea ni los elimina, y una
actualización solo puede cambiar `stock` a un entero mayor o igual que cero.

Se utilizó Claude (Anthropic) como apoyo para revisar la documentación
de transacciones y reglas, implementar la reserva y la prueba de
concurrencia, y redactar las pruebas automatizadas. Las pruebas en el
emulador, en la consola de Firebase y en el Rules Playground las realizó
el equipo.

Fuentes consultadas: documentación oficial de Firebase sobre
transacciones, actualizaciones en tiempo real, reglas de seguridad,
control de acceso por campo y Rules Playground.
