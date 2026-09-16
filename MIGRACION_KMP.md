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
código viejo, para no cambiar de comportamiento con el cambio de librería. `DataStore`
se crea vía una factoría `createDataStore(...)` por plataforma (`androidMain`/`iosMain`).

Hoy `GastoRepositoryImpl`, `IngresoRepositoryImpl`, `CategoriasRepositoryImpl` y
`PreferencesRepository` reciben `Context` de Android y serializan a mano con
`org.json.JSONObject/JSONArray` (JVM/Android-only, no compila en iOS).

1. Reemplazar `org.json` por `kotlinx.serialization.json.Json` en los 4 repos.
2. Extraer la creación del `DataStore` (que ya es multiplataforma vía Okio desde
   1.1.x) a una función `expect fun createDataStore(...)`, con `actual` en Android
   (usa `Context.filesDir`) y `actual` en iOS (usa `NSDocumentDirectory`).
3. Mover `data` a KMP con los mismos targets que `domain`.
4. **Riesgo de esta fase (mitigado)**: el cambio de librería de serialización podía
   generar/leer el JSON de forma distinta a `org.json` (nulls explícitos vs. claves
   omitidas, formato de números, fallbacks). Se añadió
   `RepositoryJsonCompatibilityTest.kt` con fixtures que reproducen a mano el formato
   exacto del código viejo, para verificar que el nuevo decoder los sigue leyendo
   igual, más tests de round-trip. Nota: este proyecto no tiene usuarios ni datos en
   producción — el test es una red de regresión de la corrección del cambio, no una
   garantía de compatibilidad con dispositivos reales.

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

## Fase 6 — AGP 9 / Gradle 9 / Kotlin 2.4

**Estado: hecho ✅ para el cambio de build system.** No dependía de Xcode. El plan
original la dividía en 5 sub-fases incrementales (6.1 solo Gradle+Kotlin, 6.2 solo
`domain`, ...), pero al ejecutarlo se descubrieron dos acoplamientos que obligaron a
fusionarlas en un único paso — documentados aquí porque no eran obvios de antemano:

1. **Gradle 9.7.1 rompe directamente con AGP 8.13.2**, no solo al llegar a AGP 9 +
   plugin nuevo. Error real: `Plugin 'com.android.internal.application' relies on
   'org.gradle.api.problems.internal.InternalProblems', a Gradle internal API that
   was removed in Gradle 9.6.0`. Es decir, no se puede aislar el bump de Gradle del
   de AGP como planteaba la 6.1 — van atados.
2. **La versión de AGP se comparte entre todos los módulos** vía el catálogo de
   versiones, así que en el momento en que sube a 9.4.0 los 6 módulos KMP se rompen
   a la vez (el error "Cannot add extension with name 'kotlin'" ya documentado
   arriba). No hay estado intermedio estable con "domain migrado, el resto en AGP
   viejo" — la 6.2/6.3/6.4 tuvieron que hacerse en el mismo commit.

**Lo que se hizo, en un solo paso:**
- Gradle wrapper 8.13 → **9.7.1** (regenerado con `./gradlew wrapper --gradle-version
  9.7.1 --distribution-type bin`, no editando `distributionUrl` a mano).
- `libs.versions.toml`: AGP → **9.4.0**, Kotlin → **2.4.20**, KSP → **2.3.11** (sin uso
  real en el proyecto, se sube solo por consistencia).
- Los 6 módulos KMP (`domain`, `data`, `di`, `navigation`, `core`, `presentation`):
  plugin `com.android.library` → `com.android.kotlin.multiplatform.library`
  (alias `libs.plugins.android.kotlin.multiplatform.library` en el catálogo), y el
  bloque separado `android { namespace/compileSdk/minSdk/compileOptions }` +
  `kotlin { androidTarget { } }` se fusionan en uno solo:
  `kotlin { androidLibrary { namespace = ...; compileSdk = ...; minSdk = ...;
  compilerOptions { jvmTarget.set(...) } } }`. `sourceCompatibility`/
  `targetCompatibility` desaparecen (los controla `jvmTarget`).
- `app/build.gradle.kts`: se quitó el plugin `org.jetbrains.kotlin.android` — con
  AGP 9 el soporte de Kotlin ya viene integrado en `com.android.application`
  (built-in Kotlin) y aplicar el plugin aparte ahora da error explícito pidiendo
  quitarlo. `app` no se tocó en nada más: sigue con `com.android.application` normal,
  build variants intactas (`debugImplementation`, etc.), porque esas solo se pierden
  en los módulos que usan el plugin KMP nuevo.
- `domain` y `data` (los únicos con tests hoy): se añadió `withHostTest {}` dentro de
  `androidLibrary { }` para poder seguir ejecutando `commonTest` sobre JVM puro
  (`./gradlew :domain:testAndroidHostTest`), ya que el plugin nuevo no habilita esto
  por defecto. `di`/`navigation`/`core`/`presentation` no tienen tests hoy, así que no
  se les añadió (se puede añadir igual el día que los tengan).

**Verificado:**
- `./gradlew :app:assembleDebug :domain:assemble :data:assemble :di:assemble
  :core:assemble :navigation:assemble :presentation:assemble` → build completo,
  Android + los 3 targets iOS (solo compilación de klibs, sin enlazar) en verde.
- `./gradlew :domain:testAndroidHostTest :data:testAndroidHostTest` → los 20 tests
  existentes (incluido `RepositoryJsonCompatibilityTest`) pasan sin fallos.
- `./gradlew assemble` completo (incluyendo `:iosApp`) solo falla en
  `linkDebugFrameworkIosArm64`/`linkReleaseFrameworkIosArm64` con el mismo
  `xcrun exit code 72` ya documentado en la Fase 5 — no es un problema nuevo de esta
  fase, sigue siendo el bloqueo de Xcode.app.

**Extra: bump de Compose Multiplatform / Koin / navigation-compose / lifecycle-viewmodel-compose.**
No hacía falta para que AGP9/Gradle9/Kotlin2.4 funcionaran (con las versiones viejas
ya compilaba y pasaban los tests), pero se hizo también, de una sentada, y salieron
dos hallazgos reales más:

1. **Compose Multiplatform 1.12.0 exige `compileSdk 37`** (vía sus propias
   dependencias de `androidx.compose.ui`/`androidx.lifecycle`). El SDK 37 no estaba
   instalado (no había `cmdline-tools`/`sdkmanager` en la máquina). Se instaló con
   `brew install --cask android-commandlinetools` y luego
   `sdkmanager --sdk_root="$HOME/Library/Android/sdk" "platforms;android-37.2"`
   (ojo: el paquete se llama `android-37.2`, no `android-37` a secas). `compileSdk`
   subido a 37 en los 6 módulos KMP + `app` (`targetSdk` se dejó en 36, es una
   decisión aparte).
2. **Compose Multiplatform 1.12.0 y `navigation-compose` 2.10.0-beta01 ya no publican
   variante para el target `iosX64`** (el simulador de iOS en Intel, obsoleto desde
   que Apple Silicon es el estándar). Como los 7 módulos declaraban `iosX64()` como
   target, esto rompía la resolución de dependencias con errores de "no matching
   variant". Se quitó `iosX64()` de los 7 módulos (`domain`, `data`, `di`,
   `navigation`, `core`, `presentation`, `iosApp`), dejando solo `iosArm64` +
   `iosSimulatorArm64` — los únicos reales en esta máquina (ya daba warning de
   "architecture mismatch" al intentar correr tests en `iosX64Test`).

**Versiones finales:** Compose Multiplatform **1.12.0**, Koin **4.2.2**,
`navigation-compose` (JetBrains) **2.10.0-beta01**, `lifecycle-viewmodel-compose`
(JetBrains) **2.9.5**, `compileSdk` **37** en todos los módulos.

**Re-verificado tras estos dos cambios:** build completo (`app` + los 6 módulos KMP)
en verde, los 20 tests unitarios siguen pasando, y `iosApp` compila Kotlin para
`iosArm64`/`iosSimulatorArm64` (sin enlazar, sigue bloqueado por Xcode.app).

**Extra 2: resto de dependencias sueltas del catálogo** (`compose-bom`,
`kotlinx-coroutines`, `kotlinx-datetime`, `kotlinx-serialization-json`,
`androidx-datastore`), que llevaban tiempo sin tocarse y no tenían relación directa
con AGP/Gradle/Kotlin:
- `androidx.compose:compose-bom` 2024.09.00 → **2026.09.00** (2 años desactualizada).
- `kotlinx-coroutines` (antes hardcodeada sin entrada en `[versions]`) 1.8.0 →
  **1.11.0**, ahora con su propia entrada `kotlinxCoroutines` en el catálogo
  (incluye también el `kotlinx-coroutines-test` hardcodeado en `domain`).
- `kotlinx-serialization-json` 1.7.3 → **1.11.0**.
- `androidx-datastore` (datastore-preferences-core) 1.1.1 → **1.2.1**.
- `kotlinx-datetime`: **se quedó en 0.6.1**, sin subir. La 0.8.0 (última estable) es
  una migración con cambio de API real, no solo un bump de versión: desde la 0.7.0,
  `kotlinx.datetime.Clock`/`Instant` pasaron a ser alias de `kotlin.time.Clock`/
  `Instant` (movidos al stdlib de Kotlin), y `Clock.System.todayIn(...)` en
  `domain/src/commonMain/kotlin/.../util/DateUtils.kt:8` deja de resolver
  (`Unresolved reference 'System'`) específicamente en la compilación del target
  Android (`compileAndroidMain`; la compilación de metadata común no lo detecta).
  Migrar ese único call site queda pendiente como tarea aparte, no bloqueante.

Todo re-verificado de nuevo tras estos cambios: build completo en verde, 20 tests
en verde.

**Pendiente de esta fase:** verificar visualmente que la app sigue funcionando en un
emulador/dispositivo Android real (navegación entre las 3 pantallas), no solo que
compila y pasan los tests unitarios. Y, aparte, migrar `DateUtils.kt` para poder
subir `kotlinx-datetime` a 0.8.0 el día que se quiera.

---

## Resumen de riesgos transversales

- El cambio más laborioso y con más riesgo de bugs no fue la UI (se portó casi
  directa), sino la serialización JSON manual en `data` (Fase 2) — ya cubierto con
  `RepositoryJsonCompatibilityTest.kt` (ver Fase 2). Al no haber datos ni usuarios en
  producción, el riesgo real era solo de correctness del propio cambio, no de romper
  dispositivos existentes.
- AGP 9.x / Gradle 9.x: **hecho ✅** (ver Fase 6). Primer intento fallido, se investigó
  por qué, y en el segundo intento se aplicó de una vez a los 6 módulos + `app`.

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

  **Matriz de versiones que sí encajan entre sí** (confirmada de nuevo, septiembre
  2026 — ver desglose por sub-fases en la Fase 6):
  - AGP: **9.4.0** (última estable)
  - Gradle: **9.7.1** (última estable)
  - Kotlin: **2.4.20** (última estable), KSP **2.3.11**
  - Compose Multiplatform: **1.12.0** (última)
  - Koin: **4.2.2**
  - `navigation-compose` (JetBrains): **2.10.0-beta01** (sigue sin versión estable)
  - `lifecycle-viewmodel-compose` (JetBrains): **2.9.5** (estable)
  - JDK: 17+ (ya cubierto, el Gradle de este proyecto corre sobre el JDK 21 de Homebrew)

  **Alcance real, ya hecho** (ver Fase 6 para el detalle completo): se reescribió el
  `build.gradle.kts` de los 6 módulos KMP (`domain`, `data`, `di`, `presentation`,
  `navigation`, `core`) para usar el plugin nuevo y `androidLibrary{}`. En la práctica
  no hizo falta tocar build variants en `app` porque ese módulo no usa el plugin KMP
  (sigue siendo `com.android.application` normal) — sí hubo que quitarle el plugin
  `org.jetbrains.kotlin.android`, redundante con el Kotlin integrado de AGP 9.
- Proyecto pequeño → esto es semanas, no meses, si se hace por fases como aquí en vez
  de intentarlo todo junto.
