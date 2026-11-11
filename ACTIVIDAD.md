# Actividad · Pruebas de la Unidad 2 y pipeline de CI/CD (Java con Spring Boot)

**Modalidad:** individual
**Fecha de entrega:** [FECHA DE ENTREGA]
**Entrega:** enlace a su repositorio en GitHub, con la actividad completa y el pipeline en verde.

## Objetivo

Partir de este proyecto, que hoy solo tiene pruebas unitarias en su pipeline, y llevarlo a un pipeline completo: desarrollar un método nuevo con **TDD**, ampliar las **pruebas unitarias**, escribir escenarios **BDD**, aplicar **análisis estático**, medir la **cobertura** y hacer que el **despliegue (simulado)** solo ocurra cuando todo eso pasa.

Antes de empezar, lean el [README.md](README.md): ahí están los conceptos y los comandos que se usan en cada paso.

---

## Paso 0. Tener el proyecto en su propio repositorio

**Por qué:** el pipeline corre en GitHub Actions, y para eso el código tiene que estar en un repositorio suyo.

1. Clonen el proyecto y entren a la carpeta:
   ```bash
   git clone <url-del-repositorio-del-curso>
   cd pronosticos-java
   ```
2. Quiten el remoto del curso, para no intentar subir cambios a él:
   ```bash
   ```
3. En GitHub, creen un repositorio **vacío** (sin README, sin `.gitignore`, sin licencia) con el nombre `pronosticos-java-<su-apellido>`.
4. Conecten su repositorio y suban el código:
   ```bash
   git remote add origin https://github.com/<su-usuario>/pronosticos-java-<su-apellido>.git
   git branch -M main
   git push -u origin main
   ```
5. En GitHub, abran la pestaña **Actions**.

**Qué deben observar:** el workflow **CI/CD** corriendo con dos jobs: *Pruebas unitarias* y *Despliegue (simulado)*. Abran el job de despliegue: la última línea dice `Desplegado`.

---

## Paso 1. Conocer el punto de partida

**Por qué:** antes de cambiar algo hay que saber que el punto de partida está en verde. Si después algo se pone en rojo, sabemos que fue por nuestros cambios.

```bash
mvn test -Dtest='**/unitarias/*Test'
```

**Qué deben observar:** al final, `Tests run: 10, Failures: 0, Errors: 0` y `BUILD SUCCESS`.

Abran `CalculadoraPronosticos.java` y `MadTest.java`. Fíjense en tres cosas que deben repetir en sus pruebas: cada prueba verifica un solo comportamiento, `@DisplayName` dice qué se espera, y los decimales se comparan con `assertEquals(esperado, obtenido, tolerancia)`.

---

## Paso 2. Desarrollar un método nuevo con TDD

Van a construir `double suavizacionExponencial(List<Double> serie, double alfa)` en `CalculadoraPronosticos`, que pronostica el siguiente periodo con suavización exponencial simple:

> F<sub>t+1</sub> = F<sub>t</sub> + α · (D<sub>t</sub> − F<sub>t</sub>), empezando con F<sub>2</sub> = D<sub>1</sub>

Donde F es el pronóstico, D la demanda real y α (alfa) un número entre 0 y 1.

| Serie | Alfa | Resultado esperado |
|---|---|---|
| 120, 132, 128, 141, 138, 150 | 0.3 | 137.5546 |
| 120, 132, 128, 141, 138, 150 | 0 | 120.00 (con alfa 0 el pronóstico nunca cambia) |
| 120, 132, 128, 141, 138, 150 | 1 | 150.00 (con alfa 1 el pronóstico es la última demanda) |
| cualquiera | −0.1 o 1.5 | `IllegalArgumentException` con el mensaje "Alfa debe estar entre 0 y 1" |

**Por qué:** en TDD la prueba se escribe primero. Si escriben el código primero, la prueba solo confirma lo que ya hicieron; si la escriben antes, la prueba define qué debe hacer el código.

### 2.1 Rojo

Creen `src/test/java/pronosticos/unitarias/SuavizacionExponencialTest.java` con **solo** esta prueba:

```java
package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

class SuavizacionExponencialTest {

    private static final List<Double> SERIE = List.of(120.0, 132.0, 128.0, 141.0, 138.0, 150.0);

    @Test
    @DisplayName("Pronostica el siguiente periodo con alfa 0.3")
    void pronosticaElSiguientePeriodoConAlfa03() {
        double resultado = new CalculadoraPronosticos().suavizacionExponencial(SERIE, 0.3);

        assertEquals(137.5546, resultado, 0.01);
    }
}
```

```bash
mvn test -Dtest='**/unitarias/*Test'
```

**Qué deben observar:** `COMPILATION ERROR` con el mensaje `cannot find symbol` sobre `suavizacionExponencial`. En Java el rojo suele ser un error de compilación: la prueba usa un método que todavía no existe.

```bash
git add .
git commit -m "test: prueba en rojo para suavizacionExponencial"
```

### 2.2 Verde

Escriban en `CalculadoraPronosticos.java` el código **mínimo** para que esa prueba pase. Corran de nuevo el comando hasta ver `BUILD SUCCESS`.

```bash
git add .
git commit -m "feat: implementa suavizacionExponencial"
```

### 2.3 Más casos y refactor

Agreguen, una por una, pruebas para las filas restantes de la tabla (alfa 0, alfa 1 y alfa fuera de rango; para la última pueden usar `@ParameterizedTest` con `@ValueSource(doubles = {-0.1, 1.5})`, como en `PromedioMovilTest`). Con cada prueba nueva repitan el ciclo: primero la ven fallar, después ajustan el código.

Cuando todas pasen, mejoren el código sin cambiar su comportamiento: nombres claros, un Javadoc que explique la fórmula, la validación de alfa al inicio. Después de cada cambio corran las pruebas; nunca deben quedar en rojo.

```bash
git add .
git commit -m "refactor: mejora la legibilidad de suavizacionExponencial"
```

---

## Paso 3. Ampliar las pruebas unitarias

**Por qué:** `mape` y `debeGenerarAlerta` ya existen pero no tienen pruebas. Código sin pruebas puede romperse en cualquier cambio sin que nadie lo note.

Creen `MapeTest.java` y `AlertasTest.java` en el paquete `pronosticos.unitarias` con al menos estos casos:

| Método | Entrada | Esperado | Qué comprueba |
|---|---|---|---|
| `mape` | real `[141, 138, 150]`, pronóstico `[126.67, 133.67, 135.67]` | 7.62 | El cálculo normal |
| `mape` | real `[80, 0, 95]`, pronóstico `[80, 0, 100]` | 2.63 | El periodo con demanda 0 se excluye |
| `mape` | real `[0, 0]`, pronóstico `[10, 20]` | `IllegalArgumentException` | Sin periodos válidos el MAPE no existe |
| `debeGenerarAlerta` | error 12, umbral 10 | `true` | Supera el umbral |
| `debeGenerarAlerta` | error 10, umbral 10 | `true` | Caso límite: igual al umbral sí alerta |
| `debeGenerarAlerta` | error 7, umbral 10 | `false` | No alcanza el umbral |
| `debeGenerarAlerta` | umbral 150 | `IllegalArgumentException` | Umbral fuera de rango |

Para los resultados booleanos usen `assertTrue` y `assertFalse`. Calculen a mano el 2.63 antes de escribir la prueba: ¿por qué el promedio se divide entre 2 y no entre 3?

```bash
mvn test -Dtest='**/unitarias/*Test'
git add .
git commit -m "test: pruebas unitarias de mape y debeGenerarAlerta"
```

---

## Paso 4. Escribir escenarios BDD con Cucumber

**Por qué:** la regla "el error igual al umbral sí alerta" es una decisión de negocio. En BDD la escribimos de forma que Producción pueda leerla y confirmarla, y esa misma descripción se ejecuta como prueba.

Las dependencias de Cucumber ya están en el `pom.xml`. Necesitan tres archivos.

### 4.1 El escenario

Creen `src/test/resources/features/alertas.feature`. La línea `# language: es` tiene que ser la **primera** del archivo:

```gherkin
# language: es
Característica: Alertas de desviación del pronóstico
  Como planeador de producción
  Quiero recibir una alerta cuando el error del pronóstico sea alto
  Para revisar el plan de producción a tiempo

  Escenario: El error supera el umbral
    Dado un umbral de alerta de 10 %
    Cuando el error del pronóstico es 12 %
    Entonces se genera una alerta
```

Agreguen ustedes dos escenarios más: el error **igual** al umbral (10 %) y el error **por debajo** del umbral (7 %).

### 4.2 Los pasos

Creen `src/test/java/pronosticos/bdd/AlertasSteps.java`. Les damos la estructura y el primer paso; completen los demás:

```java
package pronosticos.bdd;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import pronosticos.CalculadoraPronosticos;

/**
 * Conecta cada frase de alertas.feature con código.
 * Cucumber crea una instancia nueva de esta clase para cada escenario.
 */
public class AlertasSteps {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();
    private double umbral;
    private boolean alerta;

    // {int} captura el número de la frase y lo entrega como parámetro:
    // así el mismo paso sirve para 10 %, 12 % o 7 %.
    @Dado("un umbral de alerta de {int} %")
    public void unUmbralDeAlertaDe(int valor) {
        umbral = valor;
    }

    // Completen: @Cuando("el error del pronóstico es {int} %")
    // Completen: @Entonces("se genera una alerta")
    // Completen: @Entonces("no se genera una alerta")
}
```

Al completar los dos `@Entonces` van a necesitar también `import static org.junit.jupiter.api.Assertions.assertFalse;`.

### 4.3 El ejecutor

Creen `src/test/java/pronosticos/bdd/RunCucumberTest.java`. Esta clase le dice a JUnit que ejecute los escenarios con el motor de Cucumber:

```java
package pronosticos.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Suite agrupa pruebas e IncludeEngines("cucumber") hace que se ejecuten con Cucumber.
 * SelectClasspathResource indica dónde están los .feature (src/test/resources/features)
 * y GLUE_PROPERTY_NAME, en qué paquete están los pasos.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pronosticos.bdd")
public class RunCucumberTest {
}
```

```bash
mvn test -Dtest=RunCucumberTest
```

**Qué deben observar:** `Tests run: 3, Failures: 0`. Si un paso no tiene método, Cucumber lo marca como *undefined* y en la salida propone el código del método que falta.

```bash
git add .
git commit -m "test: escenarios BDD de alertas de desviación"
```

---

## Paso 5. Análisis estático con Checkstyle

**Por qué:** el análisis estático encuentra problemas sin ejecutar el código, incluso en líneas que ninguna prueba recorre.

```bash
mvn checkstyle:check
```

Si aparecen hallazgos en su código, corríjanlos. Cada uno indica archivo, línea, descripción y, entre corchetes, la regla.

Ahora provoquen un hallazgo a propósito para ver cómo se reporta. En `CalculadoraPronosticos.java` agreguen la línea `import java.util.Map;` debajo de `import java.util.List;` y corran `mvn checkstyle:check`. Lean el mensaje de la regla `UnusedImports`. Después borren la línea.

```bash
git add .
git commit -m "style: código sin hallazgos de Checkstyle"
```

---

## Paso 6. Cobertura de código

**Por qué:** la cobertura muestra qué partes del código ninguna prueba ejecuta.

```bash
mvn verify
```

Abran `target/site/jacoco/index.html` en el navegador y entren a `CalculadoraPronosticos`. Las líneas en rojo no las ejecutó ninguna prueba; en amarillo, las decisiones a las que les faltó un lado (verdadero o falso). Si hay rojos o amarillos, escriban las pruebas que faltan (pista: revisen las validaciones que lanzan `IllegalArgumentException`).

Cuando estén por encima del 90 %, fijen el mínimo. En `pom.xml`, dentro del plugin `jacoco-maven-plugin`, agreguen esta ejecución en `<executions>`, después de la de `reporte`:

```xml
<!-- Falla el build si la cobertura de líneas o de ramas baja de 90 %. -->
<execution>
    <id>verificar-cobertura</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.90</minimum>
                    </limit>
                    <limit>
                        <counter>BRANCH</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.90</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

La meta `check` corre en la fase `verify`. Corran de nuevo `mvn verify`: si la cobertura alcanza el mínimo verán `All coverage checks have been met`; si no, el build falla y dice qué contador quedó por debajo.

```bash
git add .
git commit -m "test: cobertura superior al 90 % con verificación de JaCoCo"
```

---

## Paso 7. Llevar todo al pipeline

**Por qué:** hasta ahora todo depende de que alguien corra los comandos. En el pipeline se ejecutan solos en cada push, y el despliegue solo ocurre si todo pasa.

Abran `.github/workflows/ci-cd.yml`.

### 7.1 Job de análisis estático

Agreguen este job debajo de `jobs:`, al mismo nivel que `pruebas-unitarias`:

```yaml
  analisis-estatico:
    name: Análisis estático
    runs-on: ubuntu-latest
    steps:
      - name: Descargar el código
        uses: actions/checkout@v6

      - name: Instalar Java
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      # Revisa código sobrante, nombres y complejidad sin ejecutar el código.
      - name: Revisar el código con Checkstyle
        run: mvn -B checkstyle:check
```

En YAML la indentación es parte de la sintaxis: el job tiene que quedar con dos espacios, igual que `pruebas-unitarias`.

### 7.2 Jobs de BDD y de cobertura

Siguiendo el mismo modelo, agreguen ustedes dos jobs más:

- `pruebas-bdd` (nombre: "Pruebas BDD"), que corra `mvn -B test -Dtest=RunCucumberTest`.
- `cobertura` (nombre: "Cobertura"), que corra `mvn -B verify`.

Cada job corre en su propia máquina, así que cada uno necesita sus dos primeros pasos: descargar el código e instalar Java. Maven ya viene instalado en `ubuntu-latest`.

### 7.3 Que el despliegue dependa de todo

Cambien la línea `needs` del job `despliegue`:

```yaml
    needs: [analisis-estatico, pruebas-unitarias, pruebas-bdd, cobertura]
```

Ahora el despliegue espera a los cuatro jobs y solo corre si los cuatro terminan bien.

### 7.4 Subir y comprobar

```bash
git add .
git commit -m "ci: análisis estático, BDD y cobertura en el pipeline"
git push
```

**Qué deben observar en Actions:** los cuatro jobs de verificación corren en paralelo y, cuando todos pasan, corre el despliegue e imprime `Desplegado`. En la vista del workflow se ve el diagrama con las flechas de `needs`.

### 7.5 Comprobar que el pipeline los protege

1. Rompan algo a propósito: en `CalculadoraPronosticos.java`, cambien el `>=` de `debeGenerarAlerta` por `>`.
2. Suban el cambio:
   ```bash
   git add .
   git commit -m "prueba: cambio que rompe la regla del umbral"
   git push
   ```
3. En Actions, el pipeline queda en rojo: fallan las pruebas que verifican el caso límite y el despliegue **no corre**.
4. Reviertan el cambio y suban de nuevo:
   ```bash
   git revert HEAD --no-edit
   git push
   ```
5. El pipeline vuelve a verde y el despliegue corre.

Tomen captura de las dos ejecuciones (la roja y la verde) para el informe.

---

## Paso 8. Informe

Creen en la raíz del repositorio el archivo `INFORME_<Nombre>_<Apellido>.md` con:

1. El enlace a su repositorio.
2. Las capturas del pipeline en rojo (Paso 7.5) y en verde.
3. Una captura del reporte de JaCoCo (`target/site/jacoco/index.html`).
4. Respuestas breves, con sus palabras:
   - ¿Qué vieron en el rojo del Paso 2.1 y por qué en Java suele ser un error de compilación?
   - ¿Por qué el MAPE del caso `[80, 0, 95]` se divide entre 2?
   - ¿Por qué las pruebas unitarias crean la calculadora con `new` en lugar de levantar Spring?
   - ¿Qué diferencia hay entre una prueba unitaria y un escenario BDD en este proyecto?
   - En el Paso 7.5, ¿qué pruebas fallaron y por qué el despliegue no corrió?

```bash
git add .
git commit -m "docs: informe de la actividad"
git push
```

---

## Checklist de entrega

- [ ] El repositorio es suyo y el último pipeline está en verde.
- [ ] `suavizacionExponencial` existe y el historial muestra los commits de rojo, verde y refactor.
- [ ] Hay pruebas unitarias de `suavizacionExponencial`, `mape` y `debeGenerarAlerta`, con los casos de las tablas.
- [ ] `alertas.feature` tiene `# language: es` en la primera línea y tres escenarios; existen `AlertasSteps` y `RunCucumberTest`.
- [ ] `mvn checkstyle:check` no reporta nada.
- [ ] El `pom.xml` tiene la verificación de cobertura y `mvn verify` termina con `BUILD SUCCESS`.
- [ ] El workflow tiene los jobs de análisis estático, pruebas unitarias, BDD, cobertura y despliegue, y el despliegue depende de los cuatro.
- [ ] El informe tiene las capturas y las respuestas.

## Rúbrica

| Criterio | Peso |
|---|---|
| TDD: método correcto y commits de rojo, verde y refactor | 20 % |
| Pruebas unitarias: casos completos, nombres claros, tolerancia numérica | 20 % |
| BDD: escenarios correctos en español, pasos y ejecutor | 15 % |
| Análisis estático y cobertura con verificación de JaCoCo | 15 % |
| Pipeline: jobs correctos, `needs` del despliegue y evidencia del rojo y el verde | 20 % |
| Informe: respuestas razonadas con sus palabras | 10 % |

## Documentación oficial

- JUnit, guía de usuario: https://docs.junit.org/
- Cucumber JVM con JUnit Platform: https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-junit-platform-engine
- Expresiones de Cucumber (`{int}`, `{string}`): https://github.com/cucumber/cucumber-expressions
- Checkstyle, reglas: https://checkstyle.org/checks.html
- JaCoCo, meta `check`: https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html
- Acción setup-java: https://github.com/actions/setup-java
- Sintaxis de workflows: https://docs.github.com/actions/reference/workflow-syntax-for-github-actions
