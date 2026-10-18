# Etapas 1 a 11: pruebas, algoritmos y revisión de entrega

GraphAlgorithms es la implementación principal. DirectedGraphAlgorithms hereda de
ella, sin sobrescribir algoritmos. Ambos nombres siguen siendo utilizables mediante
IDirectedGraphAlgorithms. Las interfaces de cátedra no se modificaron.

Se conserva JUnit 3.8.1, ya presente en el proyecto. Maven Compiler 3.15.0 utiliza
release 16 para comprobar también la compatibilidad de las APIs de Java.
Surefire 3.5.4 ejecuta las pruebas. Se necesita un JDK 16 o posterior.

## Ejecución desde la raíz del proyecto

```sh
mvn test
```

Estado actual: la suite completa pasa desde una compilación limpia tras la etapa 10.
Se restauró, a pedido del usuario, la corrección de DijkstraResult.getPath que había
reaparecido con su cuerpo antiguo. No se conoce la causa de ese cambio en el fuente.
Todas las pruebas siguen activas.

Para ejecutar las clases de prueba de algoritmos:

```sh
mvn "-Dtest=AlgorithmsCompatibilityTest,GraphStructureTest,DijkstraTest,FloydWarshallTest,GraphCenterTest" test
```

Para ejecutar solamente las pruebas de excentricidad y centro:

```sh
mvn -Dtest=GraphCenterTest test
```

## Resultado de las etapas 1 y 2 (18/10/2026)

Antes de unificar: 14 pruebas, 11 fallos de aserción y 1 error por el resultado nulo
de DirectedGraphAlgorithms. Después: 14 pruebas, 3 aprobadas, 11 fallos de aserción,
0 errores y 0 omitidas. Compilación y ejecución verificadas con JDK 26.

AlgorithmsCompatibilityTest comprueba costos de Dijkstra y Floyd, accesibilidad de
Warshall, recorridos desde un origen y todos los caminos con sus costos mediante
ambas clases. También comprueba un ciclo real. No certifica todos los algoritmos.

## Resultado de la etapa 3 (18/10/2026)

Suite completa: 21 pruebas, 15 aprobadas, 6 fallos conocidos, 0 errores y 0 omitidas.
GraphStructureTest reúne las cinco regresiones corregidas y siete pruebas nuevas.
Sus 12 pruebas pasan, junto con las 3 de AlgorithmsCompatibilityTest.

- removerVertice informa si se eliminó un vértice y conserva la eliminación de sus
  aristas incidentes, sin afectar otras conexiones.
- tieneCiclos comparte visitados entre componentes y distingue los vértices de la
  rama activa. Se comprueban convergencias, lazos, ciclos en otra componente y
  llamadas repetidas después de modificar el grafo.
- Los recorridos completos sobrescriben los métodos por defecto en GraphAlgorithms
  y comparten visitados. Los métodos desde un origen siguen limitados a lo alcanzable.
  Amplitud marca al encolar para evitar entradas duplicadas en la cola.
- Ambas clases se verifican a través de IDirectedGraphAlgorithms con ciclos,
  componentes desconectadas, vértices aislados, orígenes inexistentes y grafos vacíos.
  Se comprueba el orden por niveles de amplitud sin imponer orden entre vecinos.

Las interfaces se conservaron sin cambios; las variantes completas corregidas están
en GraphAlgorithms y se heredan en DirectedGraphAlgorithms. No se impone orden de
iteración al HashSet ni se cambia la representación interna del grafo.

## Resultado de la etapa 4 (18/10/2026)

Suite completa: 27 pruebas, 23 aprobadas, 4 fallos conocidos, 0 errores y 0 omitidas.
Las 8 pruebas de DijkstraTest pasan. Incluyen las dos regresiones originales y seis
casos nuevos: origen con lazo, destinos ausentes e inalcanzables, origen inexistente,
ciclo de costo cero, pesos negativos en distintas posiciones y coherencia entre
rutas y costos mediante ambas clases, incluyendo empates y llamadas repetidas.

Dijkstra valida todos los pesos negativos antes de calcular, incluso en componentes
inalcanzables. Termina si no queda un próximo vértice con costo finito y conserva
infinito para los destinos inalcanzables. Solo registra predecesores cuando hay una
conexión o mejora de costo. La reconstrucción termina antes del predecesor null.

Comportamiento de las consultas:

- Destino inalcanzable o inexistente: costo infinito y camino vacío.
- Origen como destino: costo cero y camino con el origen solamente.
- Origen inexistente, incluido grafo vacío: se conserva el retorno null existente.
- Peso negativo con origen válido: IllegalArgumentException con mensaje explicativo.
- Empates: cualquier camino mínimo válido; no se exige orden de HashSet.

Se conserva el algoritmo existente y la representación del grafo; no se modifican
Floyd, Warshall ni excentricidad en esta etapa. Las pruebas restantes corresponden
a esas próximas correcciones.

## Resultado de la etapa 5 (18/10/2026)

Suite completa: 35 pruebas, 34 aprobadas, 1 fallo conocido de excentricidad, 0 errores
y 0 omitidas. Las 11 pruebas de FloydWarshallTest pasan. Incluyen las tres regresiones
originales y ocho casos nuevos. Se comparan todos los pares en 12 grafos generados
con semilla fija, usando Dijkstra para costos y profundidad para accesibilidad,
mediante ambas clases de algoritmos. Se verifica que cada ruta usa aristas existentes
y que las rutas de Floyd suman el costo mínimo; el grafo original no se modifica.

Floyd y Warshall guardan el siguiente vértice del camino, no un intermediario. Al
pasar por k, se copia el primer paso de i hacia k. Warshall inicializa también los
siguientes de las aristas directas. Su ruta es válida, no necesariamente mínima.

FloydWarshallResult conserva el constructor de dos argumentos para costos y agrega
una representación explícita REACHABILITY para Warshall. Se mantiene la interfaz
de cátedra sin cambios. Semántica de las consultas:

| Consulta | Floyd | Warshall |
| --- | --- | --- |
| getCost de un par alcanzable | Costo mínimo, incluso cero | 1 (accesibilidad; no suma de pesos) |
| getCost de un par existente inalcanzable | Infinito | 0 |
| getCost con un vértice inexistente | Infinito | Infinito |
| connected con vértices inexistentes o sin camino | false | false |
| getPath sin camino o con vértices inexistentes | Lista vacía | Lista vacía |
| getPath de un vértice existente a sí mismo | Lista con ese vértice | Lista con ese vértice |
| getCost de un vértice existente a sí mismo | 0 | 1 |

Se conserva la conectividad reflexiva de Warshall: un vértice se alcanza a sí mismo
por el camino vacío. Floyd admite aristas negativas sin ciclos negativos; si detecta
un ciclo negativo, incluido un lazo negativo, rechaza el grafo completo con
IllegalArgumentException. Esto evita presentar rutas mínimas indefinidas. Warshall
ignora los pesos para decidir accesibilidad. La reconstrucción limita los pasos para
evitar bucles si alguien altera las matrices públicas del resultado.

## Resultado de la etapa 6 (18/10/2026)

Suite completa: 41 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas. Maven informa
BUILD SUCCESS. GraphCenterTest contiene la última regresión original y seis pruebas
nuevas. KnownDefectsTest se retiró tras trasladar su última prueba, sin omitirla.

Convención confirmada en ../notes/AED_UT04_01_Grafos dirigidos.pdf, página 33 del
archivo (diapositiva rotulada 42): e(v) = max d(u,v), máximo de la columna de Floyd.
La página 34 muestra el ejemplo con excentricidades A=9, B=7, C=7, D=10, E=6 y
centro E. Ese ejemplo se reproduce en una prueba.

- Se consideran las distancias de entrada, incluidos los infinitos.
- Un vértice inexistente tiene excentricidad infinita; uno aislado como único
  vértice tiene excentricidad cero y es centro.
- obtenerCentroGrafo devuelve null para el grafo vacío o si ningún candidato tiene
  excentricidad finita. Esta es la política de la aplicación para indicar que no hay
  un taller accesible desde todos los aeropuertos; no elige arbitrariamente entre
  candidatos cuya excentricidad es infinita.
- En empates finitos devuelve cualquiera de los centros mínimos, sin imponer orden
  de HashSet.
- El centro calcula Floyd una sola vez y reutiliza el resultado para cada columna.
  Las consultas individuales calculan Floyd una vez. No hay caché persistente que
  pueda quedar desactualizada después de modificar el grafo.
- Se comprueban la dirección de las distancias en una cadena asimétrica, grafos
  vacíos y desconectados, vértices inexistentes, empates, pesos cero y cantidad de
  ejecuciones de Floyd. Ambas clases heredan el comportamiento corregido.

La prueba de Dijkstra inalcanzable ejecuta un proceso Java separado con un límite de
cinco segundos y lo termina si queda bloqueado. Así no deja un hilo consumiendo CPU
ni impide que termine la suite. Los grafos de prueba fijan el orden de vértices para
reproducir el fallo de Floyd sin depender del orden de HashSet.

## Resultado de la etapa 7 (18/10/2026)

CargadorGrafo implementa carga desde Path o recursos del classpath, validando nombres,
campos, pesos y duplicados. Los cuatro archivos se movieron sin cambios a
src/main/resources/aeropuertos. Ver carga-aeropuertos.md para formato y uso.

Se ejecutó `mvn -o clean test`: Maven copió los cuatro recursos desde cero. Las 9
pruebas del cargador pasan, incluidos los escenarios de 8 vértices/24 aristas y
9 vértices/15 aristas. Resultado total: 50 pruebas, 45 aprobadas, 5 fallos de Dijkstra,
0 errores y 0 omitidas. Los fallos son rutas con null y caminos no vacíos para
destinos inalcanzables; se localizan en la versión actual de DijkstraResult.getPath.
La compilación limpia evita que una clase compilada anterior oculte ese estado.

## Resultado de la etapa 8 y restauración de Dijkstra (18/10/2026)

`mvn -o clean test`: 58 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas;
BUILD SUCCESS. La compilación limpia confirma que los fuentes actuales, sin depender
de clases compiladas previamente, incluyen la corrección de getPath: ruta vacía para
destinos inalcanzables o inexistentes y fin de reconstrucción antes de agregar null.

calcularClasificacionTopologica implementa Kahn en GraphAlgorithms y se hereda en
DirectedGraphAlgorithms. Construye grados de entrada y listas de sucesores auxiliares
leyendo las aristas una vez. Tiempo esperado O(V+E) y espacio O(V+E), sin modificar
el grafo ni invocar repetidamente adyacencias (que escanea las aristas del grafo).

- Grafo vacío: lista vacía.
- DAG: todos los vértices exactamente una vez, respetando cada precedencia.
- Componentes desconectadas y vértices aislados: también se incluyen.
- Ciclos, incluidos lazos: IllegalArgumentException; nunca se devuelve un orden parcial.
- Varios órdenes válidos: cualquiera es aceptable; no se impone orden de HashSet.

Las 8 pruebas de TopologicalSortTest cubren ambos nombres de clase, vértices genéricos,
convergencias, componentes, ciclos, repetición tras cambios y 15 DAG generados con
semilla fija. Verifican cada arista como precedencia y preservación del grafo. También
comprueban que los dos escenarios de vuelos se rechazan porque contienen ciclos.

## Resultado de la etapa 9 (18/10/2026)

`mvn -o clean test`: 67 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas;
BUILD SUCCESS. Las nueve pruebas de CriticalPathTest pasan junto con las anteriores,
incluida la corrección restaurada de Dijkstra.

calcularCaminoCritico calcula la duración máxima en orden topológico y reutiliza
obtenerTodosLosCaminos para enumerar secuencias completas. ResultadoCaminoCritico
incluye duración, caminos críticos y todas las secuencias con sus costos y holguras.
Se contemplan varios inicios, finales y componentes; las duraciones están en aristas.
Se conserva la interfaz de cátedra. Modelo, uso, tolerancia numérica, validaciones
y límites de rendimiento están documentados en camino-critico.md.

## Resultado de la etapa 10 (18/10/2026)

`mvn -o clean test`: 77 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas;
BUILD SUCCESS. AplicacionAerolineaTest agrega diez pruebas de sesiones, validación,
recuperación ante errores, fin de entrada, caché y demostración de tareas.
También se ejecutó el main real con entrada por consola: itinerario Montevideo a
Buenos_Aires (200), centro, cambio al escenario 2, ciclos y demostración de tareas.

AplicacionAerolinea integra carga de los dos escenarios, distancia mínima, itinerario,
centro, todos los caminos simples, ciclos y listado de ciudades. Reader/Writer se
inyectan para probar interacciones; main usa UTF-8. Las ciudades aceptan espacios o
guiones bajos y no distinguen mayúsculas. Una carga fallida conserva el grafo anterior.

Se agregó una sobrecarga obtenerCentroGrafo(grafo, resultadoFloyd) para reutilizar
el resultado ya calculado en la aplicación, conservando la operación anterior y
las interfaces. Se verifica una sola ejecución de Floyd por carga exitosa y su
reemplazo al cambiar escenario.

ProyectoDemostracion construye un DAG separado con duración 10 días, camino crítico
Inicio-Diseno-Construccion-Fin, y otra secuencia de costo 5/holgura 5. La demostración
muestra aristas, orden topológico, costos, holguras y condición crítica sin sustituir
el escenario de vuelos. README.md ahora contiene instrucciones operativas, en lugar
del traspaso temporal, ya que el usuario retomó el trabajo.

## Etapa 11: revisión técnica final (18/10/2026)

Se contrastaron las páginas 1 a 8 de la consigna con el proyecto, manteniendo el
ejercicio 1 completo por indicación del usuario. La matriz de cobertura, evidencia
y pendientes escritos están en revision-final-entrega.md.

`mvn -o clean package -B`: 77 pruebas aprobadas, BUILD SUCCESS. Se inspeccionó el
JAR generado, que contiene los cuatro recursos, y se ejecutó una sesión real desde
él con consultas, centro, caminos, cambio de escenario, ciclos y proyecto de tareas.
No se encontraron resoluciones escritas de ejercicios 2 a 7 dentro del proyecto;
se pidió al usuario su ubicación/estado y los datos de equipo/docente si desea ZIP.
El usuario confirmó que los escritos no están terminados y los subirá su compañero.
Se preparó resoluciones-escritas/ con índice y carpetas ejercicio-02 a ejercicio-07,
cada una con requisitos y casillas de seguimiento. No contienen soluciones.
La revisión técnica está completada; los escritos y el paquete final siguen
pendientes. No se declara la entrega académica completa.

Estas pruebas son una base, no una
lista exhaustiva: cada corrección deberá agregar los casos específicos necesarios.
El contenido de los archivos de aeropuertos permanece intacto; su ubicación cambió
en la etapa 7.

Los comentarios de estudio de la antigua DirectedGraphAlgorithms se conservaron
en notas-algoritmos-originales.md; describen la versión anterior.

Los informes de ejecución se generan en target/surefire-reports. El repositorio
actual versiona algunos archivos de target; una compilación puede modificarlos.
La limpieza de esa política de versionado queda fuera de estas etapas.
