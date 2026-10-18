# UT4 — Grafos dirigidos

Proyecto Java/Maven para el Problem Set UT04 hasta el ejercicio 7.
**Etapas 1 a 10 completadas; revisión técnica de etapa 11 completada.**
La entrega completa espera confirmación de las partes escritas y preparación del ZIP
cuando se solicite. Ver `docs/revision-final-entrega.md`.
El ejercicio 1 se cuenta como completo por indicación del usuario.

## Compilar y verificar

Desde esta carpeta, con JDK 16 o posterior y Maven:

```sh
mvn clean test
```

Última verificación (18/10/2026): **77 pruebas aprobadas, sin fallos ni errores**.
En este equipo se usó JDK 26, compilando con release 16; JUnit 3.8.1.

Si Java no está bien configurado en Windows:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-26'
& 'C:/ProgramData/chocolatey/lib/maven/apache-maven-3.9.14/bin/mvn.cmd' clean test
```

## Ejecutar la aplicación

Después de compilar, desde la raíz del proyecto:

```sh
java -cp target/classes ucu.edu.aed.aeropuertos.AplicacionAerolinea
```

Comando comprobado en este equipo:

```powershell
& 'C:/Program Files/Java/jdk-26/bin/java.exe' -cp target/classes ucu.edu.aed.aeropuertos.AplicacionAerolinea
```

La consola usa UTF-8. No requiere dependencias adicionales para ejecutarse.
Carga inicialmente el escenario 1 y muestra los nombres de ciudades disponibles.
Se aceptan espacios o guiones bajos, sin distinguir mayúsculas; se mantienen los
nombres de los archivos (por ejemplo, `Asuncion`, sin tilde).

| Opción | Función |
| --- | --- |
| 1 | Cambiar entre los dos escenarios suministrados. |
| 2 | Consultar distancia mínima entre dos ciudades. |
| 3 | Mostrar el itinerario mínimo y su distancia. |
| 4 | Consultar el centro de mantenimiento, usando distancias de entrada. |
| 5 | Enumerar todos los caminos simples con sus costos. |
| 6 | Indicar si el escenario contiene ciclos. |
| 7 | Mostrar un proyecto acíclico de tareas, orden topológico, caminos y holguras. |
| 8 | Listar ciudades. |
| 0 | Salir. |

Ejemplo de entrada, una línea por respuesta:

```text
3
montevideo
buenos aires
1
2
6
7
0
```

La primera consulta devuelve distancia 200 e itinerario Montevideo -> Buenos_Aires.
El escenario 2 tiene ciclos. El proyecto de tareas de la opción 7 es independiente:
no sustituye el grafo de vuelos. Su duración es 10 días y tiene dos secuencias:

- Inicio -> Diseno -> Construccion -> Fin: costo 10, holgura 0, crítica.
- Inicio -> Diseno -> Documentacion -> Fin: costo 5, holgura 5.

Las duraciones están en las aristas; las holguras son por secuencia, no por tarea.
El orden topológico puede variar entre ejecuciones sin dejar de ser correcto.

## Datos y comportamiento

Los recursos están en `src/main/resources/aeropuertos`:

- Escenario 1: 8 aeropuertos, 24 conexiones.
- Escenario 2: 9 aeropuertos, 15 conexiones.

Ambos contienen ciclos. Los archivos no se alteraron para coincidir con las tablas
del PDF: sus diferencias están documentadas en `docs/carga-aeropuertos.md`.
Floyd se calcula una vez por carga y se reutiliza en distancias, itinerarios y centro.
Cambiar de escenario reemplaza también ese resultado. Si falla una carga, se conserva
el escenario previo. Una ciudad desconocida o un destino inalcanzable se informa en
pantalla. El fin de entrada termina limpiamente, incluso durante una consulta.

La opción 5 enumera caminos simples: no repite vértices. Su cantidad puede crecer
exponencialmente; está pensada para los escenarios pequeños del ejercicio.

## Organización

- `GraphAlgorithms`: implementación central.
- `DirectedGraphAlgorithms`: hereda de ella sin duplicar código.
- `CargadorGrafo`: lectura y validación de archivos/recursos.
- `AplicacionAerolinea`: interacción de consola; permite inyectar Reader/Writer para pruebas.
- `ProyectoDemostracion`: ejemplo acíclico de tareas.
- `ResultadoCaminoCritico`: duración, secuencias y holguras.

Las interfaces de cátedra se conservaron. La sobrecarga de obtenerCentroGrafo que
recibe un resultado de Floyd exige que ese resultado corresponda al grafo actual.
La aplicación mantiene ambos juntos y no permite mutar el grafo desde el menú.

## Documentación y próximos pasos

- [Historial de etapas y pruebas](docs/pruebas-y-roadmap.md).
- [Revisión final y pendientes de entrega](docs/revision-final-entrega.md).
- [Carpetas para que el compañero agregue las partes escritas](resoluciones-escritas/README.md).
- [Carga de archivos](docs/carga-aeropuertos.md).
- [Camino crítico](docs/camino-critico.md).
- [Notas de estudio originales](docs/notas-algoritmos-originales.md).

Revisión técnica hasta el ejercicio 7 realizada con `mvn clean package`: 77 pruebas
aprobadas, JAR con recursos verificado y sesión de consola ejecutada desde el JAR.
Pendiente: el compañero completará las soluciones escritas en resoluciones-escritas/,
organizadas por ejercicio del 2 al 7. Después habrá que revisarlas y preparar el ZIP
si se solicita.
No dar por completas las partes a mano porque el código pase las pruebas. El
nombre de equipo y docente para el ZIP todavía no está definido en este chat.
Los ejercicios 8 en adelante quedan fuera del alcance.

El trabajo sigue sin commit; preservar fuentes, tests, recursos y documentación
locales. El repositorio versiona archivos de target: compilar puede modificarlos.
Los informes se generan en target/surefire-reports. Una compilación limpia es la
referencia para evitar confiar en clases antiguas. No restaurar archivos ajenos al
curso que aparezcan eliminados en Git.
