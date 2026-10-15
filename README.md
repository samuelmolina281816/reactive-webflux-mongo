# MS Customer MongoDB

API REST para gestionar clientes, desarrollada con Java 17 y Spring Boot 3.2.11. Usa MongoDB Reactive en la capa de datos y devuelve tipos Reactor (`Mono` y `Flux`).

## Requisitos

- JDK 17 instalado y disponible en `PATH`.
- Maven 3.6.3 o posterior instalado y disponible en `PATH`.
- MongoDB local o remoto al que el equipo tenga acceso. La configuración inicial apunta a `mongodb://localhost:27017`.
- Para importar los datos de ejemplo: MongoDB Database Tools (`mongoimport`).

Verifica las herramientas desde una terminal:

```bash
java -version
mvn -version
mongosh --version
mongoimport --version
```

`mongosh` y `mongoimport` solo son necesarios si vas a administrar o cargar datos localmente; no son necesarios para ejecutar las pruebas unitarias y del controlador.

## Preparación después de clonar

1. Clona el repositorio y entra al directorio del proyecto:

   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd ms-customer-mongodb-webflux-open-api
   ```

2. Comprueba que Java 17 y Maven estén instalados. El repositorio no incluye Maven Wrapper, por lo que necesitas Maven instalado.

3. Configura la conexión a MongoDB en `src/main/resources/application.properties`. Por defecto se usa:

   ```properties
   server.port=8085
   spring.data.mongodb.uri=mongodb://localhost:27017
   spring.data.mongodb.database=bootcampbd
   ```

   Spring Data puede crear la base de datos y la colección al guardar el primer documento. Para usar otra instancia, cambia la URI localmente; no guardes credenciales en el repositorio.

4. Opcionalmente, carga el conjunto de datos de ejemplo. Inicia MongoDB y ejecuta desde la raíz del proyecto:

   ```bash
   mongoimport --uri="mongodb://localhost:27017/bootcampbd" --collection=customer --file=bootcampbd.customer.json --jsonArray
   ```

   Si `mongoimport` no está instalado, instala MongoDB Database Tools o crea clientes mediante el endpoint `POST`.

5. Descarga dependencias, compila y ejecuta las pruebas:

   ```bash
   mvn clean test
   ```

6. Inicia la aplicación para desarrollo:

   ```bash
   mvn spring-boot:run
   ```

   La API queda disponible en `http://localhost:8085`. Este es el flujo recomendado para desarrollar; no es necesario generar un JAR.

## API

La ruta base es `/api/customers`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/customers` | Lista clientes |
| `GET` | `/api/customers/{id}` | Busca un cliente por ID; responde 404 si no existe |
| `POST` | `/api/customers` | Crea un cliente |
| `PUT` | `/api/customers/{id}` | Actualiza un cliente existente |
| `DELETE` | `/api/customers/{id}` | Elimina un cliente |

El cuerpo para crear o actualizar no lleva `id`; MongoDB lo genera al crear:

```json
{
  "nombre": "Ana",
  "apellido": "Perez",
  "correo": "ana@example.com",
  "dni": "12345678"
}
```

Los cuatro campos son obligatorios. Un cuerpo vacío (`{}`) o campos nulos/en blanco se rechazan con `400 Bad Request`.

La interfaz Swagger está disponible en `http://localhost:8085/swagger-ui/index.html`.

## Pruebas

Ejecuta todas las pruebas desde la raíz del proyecto:

```bash
mvn test
```

Para ejecutar una clase específica:

```bash
mvn -Dtest=CustomerServiceImplTest test
mvn -Dtest=CustomerControllerTest test
```

- `CustomerServiceImplTest` usa JUnit 5, Mockito y Reactor `StepVerifier` para verificar la lógica CRUD y sus señales reactivas, con el repositorio simulado.
- `CustomerControllerTest` usa MockMvc y un servicio simulado para comprobar rutas, delegación y validación HTTP (`@Valid`), incluyendo el rechazo de `{}`.
- `MsCustomerDummyApplicationTests` comprueba que el contexto de Spring carga.

Las pruebas de servicio y controlador no requieren conectarse a MongoDB. Si una prueba de contexto falla por configuración local, revisa `application.properties` y que el entorno tenga las dependencias Maven descargadas.

## Dependencias de pruebas

`spring-boot-starter-test` proporciona JUnit 5 y Mockito. `reactor-test` proporciona `StepVerifier` para verificar `Mono` y `Flux`. Ambas dependencias se declaran con alcance `test`, por lo que no forman parte del runtime de la aplicación.
