# API de Gestión de Pólizas

API en Spring Boot para gestionar pólizas de arrendamiento **individuales** y **colectivas**: consultar,
renovar y cancelar pólizas, y agregar o cancelar riesgos. Cada cambio se avisa al CORE a través de un mock.

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

No necesita base de datos externa: usa H2 en memoria y carga datos de prueba al iniciar.

## Cómo ejecutar

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Cómo correr las pruebas

```bash
mvn test
```

Incluye:

- **Pruebas unitarias de las reglas de negocio:** `PolizaTest` y `RiesgoTest`.
- **Pruebas de los servicios con Mockito:** `PolizaServiceTest` y `RiesgoServiceTest`.
- **Prueba del adapter hacia el CORE:** `CoreEdicionRestAdapterTest`.
- **Prueba de integración que recorre todos los endpoints:** `PolizaApiIntegrationTest`.

## Seguridad

Todas las peticiones deben llevar el header:

```
api-key: 123456
```

Sin él, o con un valor distinto, la API responde `401`. El valor se puede cambiar con la variable de entorno
`API_KEY`.

## Estructura del proyecto

```
src/main/java/com/pruebatecnica/polizas
├── controller   Endpoints REST (pólizas, riesgos y mock del CORE)
├── service      Lógica de aplicación y el puerto hacia el CORE
├── repository   Acceso a datos con Spring Data JPA
├── model        Entidades Poliza y Riesgo con sus reglas de negocio
├── dto          Objetos de entrada y salida de la API
├── client       Adapter que llama al servicio de edición del CORE
├── config       Filtros de api-key y de correlation id, cliente HTTP del CORE
└── exception    Errores de negocio y manejo centralizado de errores
```

## Datos de prueba

| Id | Número | Tipo | Estado | Riesgos |
|---|---|---|---|---|
| 1 | POL-IND-0001 | INDIVIDUAL | ACTIVA | 1 activo (id 1) |
| 2 | POL-COL-0002 | COLECTIVA | ACTIVA | 2 activos (ids 2 y 3) |
| 3 | POL-COL-0003 | COLECTIVA | CANCELADA | 2 cancelados (ids 4 y 5) |
| 4 | POL-IND-0004 | INDIVIDUAL | CANCELADA | 1 cancelado (id 6) |
| 5 | POL-COL-0005 | COLECTIVA | RENOVADA | 1 activo (id 7) |
| 6 | POL-COL-0006 | COLECTIVA | ACTIVA | 1 activo y 1 cancelado (ids 8 y 9) |
| 555 | POL-COL-0555 | COLECTIVA | ACTIVA | 1 activo (id 10) |

## Endpoints

### Listar pólizas por tipo y estado

Los dos filtros son opcionales. Valores posibles: `tipo` = `INDIVIDUAL` | `COLECTIVA`,
`estado` = `ACTIVA` | `RENOVADA` | `CANCELADA`. Un valor distinto responde `400`.

```bash
curl -H "api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"
```

### Consultar los riesgos de una póliza

```bash
curl -H "api-key: 123456" http://localhost:8080/polizas/2/riesgos
```

### Renovar una póliza

Sube el canon según el IPC, recalcula la prima (canon × meses), corre la vigencia por el mismo número de
meses y deja la póliza en `RENOVADA`.

```bash
curl -X POST -H "api-key: 123456" http://localhost:8080/polizas/555/renovar
```

### Cancelar una póliza

Cancela la póliza y todos sus riesgos activos.

```bash
curl -X POST -H "api-key: 123456" http://localhost:8080/polizas/6/cancelar
```

### Agregar un riesgo (solo pólizas colectivas)

```bash
curl -X POST -H "api-key: 123456" -H "Content-Type: application/json" \
  -d '{"direccionInmueble": "Cl 10 # 43-12 Apto 903, Medellín", "arrendatario": "Jorge Pérez", "arrendador": "María Restrepo"}' \
  http://localhost:8080/polizas/2/riesgos
```

### Cancelar un riesgo

```bash
curl -X POST -H "api-key: 123456" http://localhost:8080/riesgos/3/cancelar
```

### Mock del CORE

Solo deja en el log que la operación se intentó enviar al CORE y responde `202`.

```bash
curl -X POST -H "api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento": "ACTUALIZACION", "polizaId": 555}' \
  http://localhost:8080/core-mock/evento
```

## Reglas de negocio

- Una póliza individual solo puede tener un riesgo: solo las colectivas aceptan riesgos nuevos.
- No se puede renovar una póliza cancelada.
- La cancelación de una póliza cancela todos sus riesgos.
- Agregar un riesgo valida el tipo de póliza (debe ser colectiva) y que no esté cancelada.
- Cuando una regla no se cumple, la API responde `409` explicando el motivo.

Además, tomé estas decisiones donde el enunciado no decía nada:

- **El riesgo de una póliza individual no se cancela por separado** (`409`): para eso se cancela la póliza.
  Así nunca queda una póliza individual vigente sin riesgo y sin forma de agregarle uno.
- **Una póliza ya renovada se puede volver a renovar** en el siguiente periodo; la regla solo prohíbe
  renovar las canceladas.
- **El IPC se configura** en `application.yml` (`negocio.ipc`, por defecto 5,20 %). El canon nuevo se
  redondea a pesos y la prima se calcula sobre ese valor.

## Respuestas de error

Los errores siguen el formato estándar Problem Details (RFC 7807):

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos o parámetros con valores no permitidos |
| 401 | Falta el header `api-key` o es incorrecto |
| 404 | La póliza o el riesgo no existen |
| 409 | La operación no cumple una regla de negocio |
| 500 | Error inesperado (se registra en el log) |

## Integración con el CORE

Toda operación que cambia el estado de una póliza o de un riesgo (renovar, cancelar póliza, agregar riesgo y
cancelar riesgo) se envía al servicio de edición del CORE con el evento `ACTUALIZACION`:

1. El servicio guarda el cambio y publica un evento interno.
2. Cuando la transacción se confirma, el evento se envía al CORE a través de `CoreEdicionPort`. Así nunca se
   avisa de un cambio que no quedó guardado.
3. La implementación `CoreEdicionRestAdapter` llama por HTTP a `POST /core-mock/evento`. En un ambiente real
   solo cambiaría la URL (`CORE_BASE_URL`) para apuntar a la capa media en WebLogic.
4. Si el CORE falla, el error se registra en el log y la operación del usuario no se ve afectada. Para no
   dejar peticiones bloqueadas, la llamada tiene un tiempo máximo de 2 s para conectar y 3 s para responder
   (`core.connect-timeout` y `core.read-timeout`).

Cada petición lleva un identificador (`X-Correlation-Id`) que aparece en todos sus logs y se reenvía al
CORE, así se puede seguir una operación de punta a punta. Por ejemplo:

```
INFO [3f2a...] CoreEdicionRestAdapter : Enviando evento ACTUALIZACION de la póliza 555 al CORE
INFO [3f2a...] CoreMockController     : [CORE-MOCK] Se intentó enviar al CORE el evento ACTUALIZACION de la póliza 555
```

## Relación con el diseño del Módulo 1

Este proyecto es una versión simplificada del diseño del Módulo 1:

- `PolizaService` y `RiesgoService` corresponden al Servicio de Pólizas y al Servicio de Riesgos.
- `CoreEdicionPort` y `CoreEdicionRestAdapter` aplican la arquitectura hexagonal: las reglas de negocio no
  dependen de cómo se habla con el CORE.
- El aviso al CORE después de confirmar la transacción sigue la idea del diseño orientado a eventos. En
  producción se reemplazaría por el patrón outbox y una cola, para reintentar si el CORE no está disponible.
- Las rutas son las del enunciado; la versión (`/v1`) la expondría el API Gateway.
