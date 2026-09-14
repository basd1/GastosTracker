# Plan de migración a Kotlin Multiplatform (KMP)

Objetivo acordado: llevar GastosTracker a **Android + iOS**, compartiendo también la UI
mediante **Compose Multiplatform**.

Estado del proyecto en el momento de este análisis: 56 archivos `.kt`, ~5.300 líneas,
100% offline (sin red), arquitectura ya modularizada por capas
(`core`, `domain`, `data`, `presentation`, `navigation`, `di`, `app`).

---

## Fase 0 — Preparación

**Estado: tests hechos ✅ · actualización de versiones evaluada y descartada por ahora**

1. **Tests unitarios reales en `domain`** (hecho): antes no existía más que el
   `ExampleUnitTest.kt` boilerplate. Se añadieron en `domain/src/test/java/...`:
   - `CategoriaTest` — valida `displayName`, opacidad de colores y que la paleta de
     colores personalizados no tenga duplicados.
   - `FakeGastoRepository`, `FakeIngresoRepository`, `FakeCategoriasRepository` — fakes
     en memoria (no Mockito/MockK) porque en `commonTest` de KMP no hay mocking por
     reflection; así ya quedan listos para cuando `domain` se mueva a Fase 1.
   - `GastoUseCasesTest`, `IngresoUseCasesTest`, `CategoriaUseCasesTest` — cubren los 13
     casos de uso, usando `kotlin.test` + `kotlinx-coroutines-test` (multiplataforma en
     vez de JUnit4 puro).
   - Compilan correctamente (`:domain:compileDebugUnitTestKotlin`); no se pudieron
     **ejecutar** en esta sesión por una restricción del entorno (bloqueo al hacer fork
     de procesos nativos) — se recomienda correr `./gradlew test` localmente.

2. **Actualización de Gradle/AGP/Kotlin** (evaluada, no aplicada):
   - AGP ya está en `8.13.2`, la última versión de la rama 8.x (Google saltó directo a
     9.0 en enero 2026). Subir de versión implica cruzar a AGP 9.x, un salto mayor
     (JDK 17+, DSL con cambios) que conviene hacer junto con la Fase 1, no antes.
   - Se probó subir el wrapper de Gradle 8.13 → 8.14.5: **rompe** con
     `IllegalArgumentException: 25.0.3` al ejecutarse sobre el JDK 25.0.3 que trae
     empaquetado Android Studio en esta máquina (bug de parseo de versión de JDK en el
     Kotlin embebido de esa versión de Gradle). Se revirtió.
   - Se probó subir Kotlin 2.0.21 → 2.4.20 y KSP → 2.3.12: compila, pero exige migrar
     el DSL `kotlinOptions { jvmTarget = "11" }` (eliminado/error duro en Kotlin 2.4) al
     nuevo `kotlin { compilerOptions { jvmTarget.set(...) } }` en los 7 módulos.
   - Conclusión: technically viable, pero se dejó fuera de esta fase a pedido del
     usuario — el alcance real de "Fase 0" era solo tests.

---

## Fase 1 — `domain` a `commonMain` (Kotlin Multiplatform)

**Estado: pendiente**

Convertir `domain` de módulo Android (`com.android.library`) a módulo KMP puro. Es la
fase de menor riesgo porque `domain` no tiene ni un import de Android.

1. Cambiar plugins en `domain/build.gradle.kts`: quitar `com.android.library` +
   `org.jetbrains.kotlin.android`, aplicar `org.jetbrains.kotlin.multiplatform`.
2. Declarar targets dentro de `kotlin { }`: `androidTarget()`, `iosX64()`,
   `iosArm64()`, `iosSimulatorArm64()`.
3. Mover código fuente al layout KMP:
   - `domain/src/main/java/...` → `domain/src/commonMain/kotlin/...`
   - `domain/src/test/java/...` → `domain/src/commonTest/kotlin/...`
     (los tests de la Fase 0 ya están escritos con APIs multiplataforma, así que se
     mueven sin reescritura).
4. Mover dependencias (`kotlinx.coroutines`, test deps) a
   `sourceSets.commonMain.dependencies { }` / `commonTest.dependencies { }`.
5. Ajustar `settings.gradle.kts`/catálogo de versiones para declarar el plugin KMP.
6. Verificar que `data`, `presentation`, `di`, `app` (que siguen siendo solo-Android)
   sigan resolviendo `project(":domain")` sin cambios, gracias a `androidTarget()`.
7. Build de verificación: compilar target Android y, si hay toolchain de Xcode
   disponible, compilar también el target iOS.

Fuera de alcance en esta fase: `data`, UI, Compose Multiplatform.

---

## Fase 2 — `data` desacoplado de Android

**Estado: pendiente**

Hoy `GastoRepositoryImpl`, `IngresoRepositoryImpl`, `CategoriasRepositoryImpl` y
`PreferencesRepository` reciben `Context` de Android y serializan a mano con
`org.json.JSONObject/JSONArray` (JVM/Android-only, no compila en iOS).

1. Reemplazar `org.json` por `kotlinx.serialization.json.Json` en los 4 repos.
2. Extraer la creación del `DataStore` (que ya es multiplataforma vía Okio desde
   1.1.x) a una función `expect fun createDataStore(...)`, con `actual` en Android
   (usa `Context.filesDir`) y `actual` en iOS (usa `NSDocumentDirectory`).
3. Mover `data` a KMP con los mismos targets que `domain`.
4. **Punto de riesgo real de esta fase**: no hay tests hoy sobre la serialización, y
   los usuarios ya tienen datos guardados en DataStore con el formato JSON actual —
   conviene escribir tests que comparen el JSON generado antes/después del cambio de
   librería de serialización, para no romper la compatibilidad con dispositivos
   existentes.

---

## Fase 3 — Inyección de dependencias (Koin)

**Estado: pendiente**

`AppModule.kt`, `BaseApplication.kt`, `MainActivity.kt` y las 3 pantallas usan
`koin-android`/`koin-androidx-compose` (`androidContext()`, `koinViewModel()`).

1. Migrar de `koin-android` a `koin-core`, moviendo los módulos Koin a `commonMain`
   donde sea posible.
2. El arranque (`startKoin`) queda por plataforma: `BaseApplication` en Android, un
   equivalente en Swift/Kotlin bridge para iOS.

---

## Fase 4 — UI compartida (Compose Multiplatform)

**Estado: pendiente**

1. Aplicar el plugin `org.jetbrains.compose` en `presentation`.
2. Cambiar imports de `androidx.compose.*` a las coordenadas multiplataforma
   (`compose.foundation`, `compose.material3`) — el código de las pantallas cambia
   poco, ya que la API es prácticamente la misma.
3. Sustituir `androidx.navigation:navigation-compose` (Android-only) por su artefacto
   multiplataforma (misma API de `NavHost`/`NavController`).
4. Convertir los ViewModels a `koin-compose-viewmodel` (sobre
   `androidx.lifecycle:lifecycle-viewmodel-compose`, que ya es multiplataforma).

---

## Fase 5 — App iOS

**Estado: pendiente**

1. Crear el módulo `iosApp` (proyecto Xcode + punto de entrada Compose Multiplatform
   vía `MainViewController.kt`).
2. Reutilizar `presentation` tal cual desde el lado iOS.

---

## Resumen de riesgos transversales

- El cambio más laborioso y con más riesgo de bugs no es la UI (se porta casi
  directa), sino la serialización JSON manual en `data` (Fase 2) — ahí vive la
  persistencia real de gastos/ingresos/categorías y hoy no tiene tests ni
  compatibilidad garantizada hacia atrás.
- AGP 9.x / Gradle 9.x (necesarios tarde o temprano para KMP moderno) tienen su propia
  guía de migración específica publicada por JetBrains — conviene abordarlos como
  parte de la Fase 1, no antes.
- Proyecto pequeño → esto es semanas, no meses, si se hace por fases como aquí en vez
  de intentarlo todo junto.
