# Camino crítico y holguras por secuencia

`GraphAlgorithms.calcularCaminoCritico(grafo)` devuelve `ResultadoCaminoCritico<V>`.
También está disponible en `DirectedGraphAlgorithms` por herencia. Es una operación
adicional de la clase; no se modificó la interfaz de cátedra.

## Modelo

Las aristas contienen duraciones finitas y no negativas. No se deducen duraciones
de los vértices. Todos los vértices sin entradas son inicios, y todos los vértices
sin salidas son finales. Se enumeran los caminos completos desde cualquier inicio
hasta cualquier final alcanzable, sin agregar vértices artificiales al grafo.

Se supone que las componentes del proyecto pueden comenzar en el instante cero y
que el proyecto termina al completar todas ellas. Un vértice aislado representa una
secuencia de costo cero. La duración es el máximo de las duraciones de los caminos.
La holgura de cada secuencia es duración del proyecto menos costo de esa secuencia.
Esto no calcula las holguras individuales de cada tarea de un análisis PERT completo.

## Uso

```java
var resultado = new GraphAlgorithms().calcularCaminoCritico(grafo);
System.out.println("Duracion: " + resultado.getDuracion());
for (var secuencia : resultado.getSecuencias()) {
    System.out.println(secuencia.getCamino().getPath());
    System.out.println("Costo: " + secuencia.getCamino().getCost());
    System.out.println("Holgura: " + secuencia.getHolgura());
    System.out.println("Critica: " + secuencia.esCritica());
}
var criticos = resultado.getCaminosCriticos();
```

Los caminos usan el `Path<V>` existente. El resultado conserva listas inmodificables
y copias de los recorridos. Incluye todos los caminos críticos empatados, no solamente
el primero. Con doubles se considera empate si la diferencia es menor o igual a
1e-9 por el mayor valor absoluto de los dos costos; las holguras dentro de esa
tolerancia se normalizan a cero. El orden entre secuencias no está garantizado.

## Algoritmo y límites

1. Usar clasificación topológica para rechazar ciclos.
2. Construir adyacencias auxiliares e identificar inicios y finales.
3. Propagar la mayor duración hacia cada sucesor siguiendo el orden topológico.
4. Tomar el máximo entre los finales.
5. Reutilizar `obtenerTodosLosCaminos` para enumerar las secuencias inicio-fin.
6. Asociar costos y holguras a las secuencias y seleccionar las críticas.

El cálculo de duración es O(V+E) esperado con mapas. La operación completa también
enumera caminos: su cantidad puede ser exponencial. Reutiliza el retroceso existente,
que consulta adyacencias escaneando aristas, y prueba cada par inicio-fin. Está
orientada a los grafos pequeños del ejercicio; no es un planificador de gran escala.

Un grafo vacío produce duración cero y listas vacías. Los ciclos, pesos negativos,
pesos no finitos, datos de arista nulos o sumas fuera del rango finito de double se
rechazan con IllegalArgumentException. Los escenarios de vuelos suministrados tienen
ciclos: no son ejemplos válidos para este cálculo. La etapa 10 agregó una
demostración separada de tareas acíclicas en la opción 7 de AplicacionAerolinea.

## Validación

`mvn -Dtest=CriticalPathTest test` ejecuta nueve pruebas: caminos de distinto costo,
empates, varios inicios y finales, componentes aisladas, vacío, costos cero,
decimales, ciclos, duraciones inválidas, desbordamiento, preservación de resultados
y consistencia en doce DAG generados con semilla fija. Se prueban ambas clases de
algoritmos. La prueba de ramificación diferencia camino máximo (11) de mínimo (3),
con holguras 0 y 8 respectivamente.
