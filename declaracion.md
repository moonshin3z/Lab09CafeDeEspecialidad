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

![Búsqueda de imports en presentación](captura-imports.png)
