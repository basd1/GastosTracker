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

**Estado: hecho ✅ (commit `f42aae6`)**

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

**Estado: hecho ✅ (commit `43b74cf`)** — `org.json` reemplazado por
`kotlinx.serialization`, con tests que congelan el formato JSON que ya generaba el
código viejo para no romper datos guardados en dispositivos reales. `DataStore` se
crea vía una factoría `createDataStore(...)` por plataforma (`androidMain`/`iosMain`).

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

**Estado: hecho ✅ (commit `ed04790`)** — bindings de repos/casos de uso movidos a
`di/commonMain` con `koin-core`; solo el binding del `DataStore` (necesita `Context`
en Android) quedó separado por plataforma. Los `ViewModel` se siguen registrando en
`:app` (Android), pendiente de moverse en la Fase 4/5 si hiciera falta.

`AppModule.kt`, `BaseApplication.kt`, `MainActivity.kt` y las 3 pantallas usan
`koin-android`/`koin-androidx-compose` (`androidContext()`, `koinViewModel()`).

1. Migrar de `koin-android` a `koin-core`, moviendo los módulos Koin a `commonMain`
   donde sea posible.
2. El arranque (`startKoin`) queda por plataforma: `BaseApplication` en Android, un
   equivalente en Swift/Kotlin bridge para iOS.

---

## Fase 4 — UI compartida (Compose Multiplatform)

**Estado: hecho ✅ (commit `c35588f`)** — `presentation`, `navigation` y `core` ya son
KMP con el plugin de Compose Multiplatform (mismo namespace `androidx.compose.*`, la
UI no cambió). `navigation-compose` y `lifecycle-viewmodel-compose` pasaron a sus
artefactos multiplataforma (`org.jetbrains.androidx.*`, porque los de Google
`androidx.navigation`/`androidx.lifecycle` todavía no publican klibs de iOS reales,
solo variantes "stub"). `koinViewModel()` pasó a `koin-compose-viewmodel`.

**Efecto colateral importante**: esto obligó a subir Kotlin 2.0.21 → 2.1.10 y Koin
3.5.6 → 4.0.0 (las versiones multiplataforma de navigation-compose/lifecycle-viewmodel-compose
compatibles con Kotlin 2.0.21 no tienen klibs de iOS reales; y `koin-compose-viewmodel`
no existe antes de Koin 4.0.0). Ya estaba anticipado como riesgo transversal más abajo.

Se reemplazaron además usos de APIs solo-JVM que no existen en Kotlin/Native:
`java.util.UUID` → `kotlin.uuid.Uuid`, `String.format("%.Nf", ...)` → función propia
`formatDecimal()`, `BigDecimal.stripTrailingZeros()` → `Double.toPlainStringTrimmed()`
(todo en `presentation/util/`).

Verificado: build de Android completo + tests domain/data en verde + los 3 targets
iOS (`iosArm64`/`iosX64`/`iosSimulatorArm64`) de todos los módulos KMP compilan.

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

**Estado: parcial** — hecho el lado Kotlin, falta el proyecto Xcode.

1. **Hecho**: módulo Gradle `:iosApp` (KMP, solo targets iOS: `iosX64`, `iosArm64`,
   `iosSimulatorArm64`), con `MainViewController.kt` que arranca Koin
   (`sharedModule` + `platformModule` de `:di`) y expone un `UIViewController` con
   `ComposeUIViewController { AppNavHost(...) }` reutilizando `presentation` tal cual.
   Configurado `binaries.framework { baseName = "GastosTrackerApp" }`.
2. **Verificado**: `compileKotlinIosArm64`, `compileKotlinIosX64` y
   `compileKotlinIosSimulatorArm64` compilan bien (el código Kotlin/Native es
   correcto).
3. **Bloqueado en esta máquina**: `linkDebugFrameworkIosSimulatorArm64` (el paso que
   empaqueta el `.framework` final) falla con `xcrun` exit code 72 — esta máquina solo
   tiene las Command Line Tools de Xcode instaladas (`xcode-select -p` →
   `/Library/Developer/CommandLineTools`), no Xcode.app completo. Compilar código
   Kotlin/Native no lo necesita, pero enlazar el framework final y crear/abrir un
   proyecto `.xcodeproj` sí.
4. **Pendiente, a hacer en una Mac con Xcode.app instalado**:
   - Crear un proyecto Xcode normal (SwiftUI o UIKit) en, por ejemplo, `iosApp/xcode/`.
   - Añadir un "Run Script" build phase que invoque
     `./gradlew :iosApp:embedAndSignAppleFrameworkForXcode` (tarea estándar que genera
     KMP para integrar el framework en el build de Xcode).
   - Desde Swift, instanciar la vista con
     `MainViewControllerKt.MainViewController()` (el nombre exacto depende del
     `baseName` del framework, `GastosTrackerApp`).

---

## Resumen de riesgos transversales

- El cambio más laborioso y con más riesgo de bugs no es la UI (se porta casi
  directa), sino la serialización JSON manual en `data` (Fase 2) — ahí vive la
  persistencia real de gastos/ingresos/categorías y hoy no tiene tests ni
  compatibilidad garantizada hacia atrás.
- AGP 9.x / Gradle 9.x: **se probó y se revirtió** (primer intento), y luego se
  investigó por qué falló para dejar el camino correcto documentado.

  **Primer intento (fallido) y por qué falló de verdad**: subir a AGP 9.4.0 / Gradle
  9.7.1 manteniendo `com.android.library` + `org.jetbrains.kotlin.multiplatform` en los
  6 módulos KMP. Da el error "Cannot add extension with name 'kotlin'" porque AGP 9
  trae soporte de Kotlin integrado ("built-in Kotlin"). El flag
  `android.builtInKotlin=false` lo desactiva, pero incluso así falla con un
  `ClassCastException` interno (`ApplicationExtensionImpl$AgpDecorated_Decorated cannot
  be cast to BaseExtension`) contra Kotlin Gradle Plugin 2.1.10.

  **La causa real** (confirmado en la documentación oficial de Kotlin/AGP): a partir de
  AGP 9.0, **`org.jetbrains.kotlin.multiplatform` ya no se puede combinar con
  `com.android.library`/`com.android.application`**, ni con el flag de arriba. Hace
  falta el plugin nuevo **`com.android.kotlin.multiplatform.library`**, que sustituye a
  `com.android.library` en los módulos KMP y usa un bloque `androidLibrary { }` en vez
  de `androidTarget { }`. Como parche temporal (desaparece en AGP 10, ~Q2 2026) existe
  `android.enableLegacyVariantApi=true`, distinto del flag que se probó al principio.

  **Matriz de versiones que sí encajan entre sí** (fuente: documentación oficial de
  Kotlin para la migración a AGP 9 + JetBrains Compose Multiplatform):
  - AGP: 9.0.0+ (9.4.0 ya probado y disponible)
  - Gradle: 9.1.0+ (9.7.1 ya probado y disponible)
  - Kotlin: 2.3.0+ recomendado (la última es 2.4.20; con eso ya se podría usar Koin
    4.2.2, la última, en vez del 4.0.0 al que hubo que bajar en la Fase 4)
  - Compose Multiplatform: 1.9.3+ recomendado (la última es 1.11.0)
  - JDK: 17+ (ya cubierto, el Gradle de este proyecto corre sobre el JDK 21 de Homebrew)
  - `navigation-compose` / `lifecycle-viewmodel-compose` (JetBrains): habría que
    volver a fijar sus versiones para esta franja de Kotlin/Compose Multiplatform más
    nueva; no se llegó a probar.

  **Alcance real de hacerlo bien**: no es solo subir números — hay que reescribir el
  `build.gradle.kts` de los 6 módulos KMP (`domain`, `data`, `di`, `presentation`,
  `navigation`, `core`) para usar el plugin nuevo y `androidLibrary{}`, y ese plugin
  **no soporta build variants** (adiós `debugImplementation`/`compileDebugKotlinAndroid`
  tal como están hoy), así que también tocaría `app/build.gradle.kts` y los tests. Se
  dejó en AGP 8.13.2 / Gradle 8.13 (que ya funciona) — este salto queda como su propia
  migración aislada para cuando se quiera abordar.
- Proyecto pequeño → esto es semanas, no meses, si se hace por fases como aquí en vez
  de intentarlo todo junto.
