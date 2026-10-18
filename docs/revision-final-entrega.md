# Revisión final de entrega — UT4, ejercicios 1 a 7

Fecha: 18/10/2026. Alcance: consigna de Problem-set-04.pdf en
`../implementations/proyecto-base-ut04`, páginas 1 a 8, hasta ejercicio 7 inclusive.

## Conclusión y límites

La revisión técnica del código quedó completada: los métodos requeridos están
implementados, integrados cuando corresponde y las 77 pruebas pasan desde una
compilación limpia. Esto no certifica ausencia de cualquier error ni reemplaza las
soluciones escritas. La entrega académica completa queda pendiente de confirmar y
adjuntar las partes a mano. El ejercicio 1 se cuenta completo por indicación expresa
del usuario, sin reabrir sus partes.

El usuario confirmó que las resoluciones escritas todavía no están terminadas y que
las agregará su compañero. Se preparó resoluciones-escritas/, con una carpeta y un
README de requisitos por ejercicio (2 al 7), más un índice para registrar archivos.
Esos README son listas de pendientes, no soluciones. No se generó un ZIP presentado
como entrega final.

## Cobertura por ejercicio

| Ejercicio / parte | Evidencia de código | Estado de lo escrito o de la evidencia de entrega |
| --- | --- | --- |
| 1, todas | No se reabre la revisión del ejercicio. | Completo por indicación del usuario. |
| 2, partes 1 y 2 | Floyd y centro disponibles como herramientas. | Confirmar dibujo de cuatro aeropuertos, análisis temporal, cálculo de distancias y elección del taller. |
| 2, parte 3 | GraphAlgorithms.floyd + FloydWarshallResult.getPath. | La recuperación usa siguientes vértices; explicar esa convención si se pide presentar el algoritmo. |
| 3, parte 1.1 | DirectedGraph agrega vértices/aristas; CargadorGrafo lee ambos archivos. | Confirmar justificación de estructuras, memoria y operaciones. |
| 3, parte 1.2 | AplicacionAerolinea: opciones 2 y 3 para costo/ruta, 4 para centro; Floyd se reutiliza. | No sustituye los cálculos manuales de otros escenarios. |
| 3, parte 2 | GraphAlgorithms.warshall, probado con accesibilidad y rutas. | La consigna exige implementarlo, no una opción independiente del menú. |
| 4, parte 1 | DFS disponible desde un origen y para todo el grafo. | Confirmar dibujo, lista, bosque desde Asunción y clasificación de arcos usando la tabla propia del ejercicio. |
| 4, parte 2 | recorridoEnProfundidad y auxiliar con visitados compartidos para el bosque. | La interfaz entregada en este proyecto es IDirectedGraphAlgorithms, aunque el PDF menciona IGraphAlgorithms. Se conserva la interfaz existente. |
| 5, parte 1 | Enumeración de caminos disponible. | Confirmar lenguaje natural y ejecución en papel Montevideo-Porto Alegre. La referencia del PDF es al ejercicio 1 de TA4. |
| 5, parte 2 | obtenerTodosLosCaminos devuelve List<Path<V>>, rutas y costos; opción 5. | Implementado para caminos simples, sin repetir vértices. |
| 6, parte 1.1 | Detección por visitados y rama activa. | Confirmar lenguaje natural y pseudocódigo. |
| 6, partes 1.2 y 1.3 | DirectedGraph.tieneCiclos; cargar escenario 2 y opción 6. | Resultado verificado: el segundo escenario sí contiene ciclos. Incorporar resultado a la entrega escrita si corresponde. |
| 6, parte 2 | calcularCaminoCritico + ResultadoCaminoCritico; opción 7 demuestra rutas, costos y holguras. | Modelo explícito: duraciones en aristas y holgura por secuencia. |
| 6, parte 3 | calcularClasificacionTopologica implementa Kahn; opción 7 muestra un orden. | Rechaza ciclos en lugar de devolver una lista parcial. |
| 7, parte 1 | Dijkstra y reconstrucción implementados como apoyo. | Confirmar dibujo, pasos desde B, vector de predecesores y algoritmo para imprimir el camino. La consigna no exige Java adicional aquí. |
| 7, parte 2 | Floyd, rutas, excentricidad y centro disponibles como apoyo. | Confirmar lista de adyacencias, caminos mínimos, matriz de predecesores, pasos A-E, excentricidades y centro sobre la figura del PDF. |

## Evidencia técnica reproducible

Se ejecutó `mvn -o clean package -B` con JDK 26 y release 16:

- 77 pruebas; cero fallos, errores u omisiones.
- Generado `target/aed-2026-s1-1.0-SNAPSHOT.jar`.
- Confirmados los cuatro recursos de aeropuertos dentro del JAR.
- Ejecutado el main desde el JAR, no solamente desde clases sueltas.
- `git diff --check` sin errores de espacios.

Comando del programa empaquetado:

```sh
java -cp target/aed-2026-s1-1.0-SNAPSHOT.jar ucu.edu.aed.aeropuertos.AplicacionAerolinea
```

El JAR no declara Main-Class: se usa `-cp` y el nombre de clase, no `java -jar`.

Resultados observados en la sesión real:

| Operación | Resultado |
| --- | --- |
| Carga escenario 1 | 8 aeropuertos, 24 conexiones. |
| Montevideo a Buenos_Aires | Distancia mínima 200; itinerario directo. |
| Centro del escenario 1 | Curitiba. |
| Todos los caminos simples Montevideo a Buenos_Aires | 4 caminos. |
| Carga escenario 2 | 9 aeropuertos, 15 conexiones. |
| Ciclos del escenario 2 | Sí. Por ejemplo Florianopolis -> San_Pablo -> Florianopolis está en los archivos. |
| Montevideo a Asuncion en escenario 2 | No hay camino. |
| Proyecto acíclico de demostración | Duración 10; secuencias con holguras 0 y 5. |
| Salida | Termina limpiamente. |

Logs regenerables: `target/etapa-11-verificacion.log`,
`target/etapa-11-consola.log` y `target/surefire-reports/`.

## Diferencias y convenciones que deben quedar claras

1. Los recursos suministrados y la tabla 2 del PDF no son idénticos. Se conservó
   el contenido original de los archivos. El centro Curitiba anterior corresponde
   al escenario de archivos, no necesariamente a las tablas de la consigna.
2. Ejemplos de diferencias: Montevideo-San_Pablo es 2000 en archivo frente a 980
   en tabla; Montevideo-Santos de la tabla no está en archivo; Porto_Alegre tiene
   salidas en el archivo pero no en la tabla. No mezclar resultados de ambos grafos.
3. Excentricidad de entrada, máximo de columna, según material de cátedra. Si no
   existe candidato accesible desde todos los vértices, la aplicación informa que
   no hay centro de mantenimiento; el método retorna null.
4. Floyd almacena siguientes vértices, no una matriz literal de predecesores.
   La parte escrita que solicite predecesores debe mostrar esa representación
   expresamente, sin cambiarle el nombre a la matriz next.
5. HashSet no fija un orden. Para una ejecución manual de DFS indicar el orden
   elegido de vecinos. Para empezar por Asunción, utilizar la variante con origen;
   la variante completa recorre todos los vértices sin garantizar cuál es primero.
6. Enumerar todos los caminos puede ser exponencial. El proyecto está orientado a
   los tamaños del ejercicio, no a una red de vuelos de gran escala.
7. El menú no transforma los vuelos cíclicos en un proyecto acíclico; la demostración
   de tareas es independiente y muestra sus aristas y duraciones.

## Lo necesario para cerrar la entrega completa

- El compañero debe agregar los documentos/fotos/PDF en resoluciones-escritas/ y
  actualizar sus listas e índice.
- Revisar su correspondencia con cada parte y con el grafo correcto.
- Adjuntar esas resoluciones como imágenes o PDF, según la consigna.
- Definir número de equipo y nombre del docente si se prepara el ZIP final.
- Nombre exigido: `equipo-{numero de equipo}-{nombre de docente}.zip`.

Contenido recomendado del paquete:

```text
proyecto-base-ut04/
  pom.xml
  README.md
  src/main/java/
  src/main/resources/
  src/test/java/
  docs/
resoluciones-escritas/
  ... documentos o imágenes realmente revisados ...
```

No incluir por defecto .git, .idea, target, logs ni documentos ajenos al curso.
No inventar datos de equipo/docente ni crear placeholders presentados como
resoluciones terminadas. No hacer commit/push ni restaurar eliminaciones locales
sin una instrucción correspondiente.
