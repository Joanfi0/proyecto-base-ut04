# Notas de estudio de la implementación anterior

Se conservan los comentarios originales de DirectedGraphAlgorithms. Los nombres describen aquella versión, no necesariamente la implementación actual de GraphAlgorithms.

```text
D -> es el mapa de costos minimos desde el origen hacia cada vertice
        Es un Map<V, Double> donde:
        -> la clave es un vertice del grafo
        -> El valor es el costo minimo conocido para llegar a ese vertice desde el origen
        Al inicio se carga con los costos directos desde el origen — si no hay arista directa se pone Double.MAX_VALUE (infinito).
        A medida que el algoritmo avanza, los valores se van actualizando si se encuentra un camino más corto pasando por otros vértices.
    C -> Es la matriz de costos del grafo - representa el costo directo de ir de un vertice a otro siguiendo una arista
        Es un double[][] donde:
        -> C[i][j] = costo de la arista que va del vértice i al vértice j
        -> Si no hay arista entre i y j → Double.MAX_VALUE (infinito)
        -> Si i == j → 0 (costo de ir a sí mismo)

    C → costos directos entre vértices, no cambia
    D → costos mínimos desde el origen, se actualiza durante el algoritmo

    S -> son los nodos cvisitados
    Es el conjunto de vértices que Dijkstra ya procesó. Arranca con solo el origen y va creciendo.
    Cuando S == V (todos visitados), el algoritmo terminó.

    noVisitados -> V menos S
    Es el complemento de S - los vértices que todavía no fueron procesados. El algoritmo elige de acá el próximo w.

    w -> el vértice elegido en cada iteración
    Es el vértice no visitado con menor costo acumulado en D.
    En cada vuelta del while se elige el más barato, se agrega a S y se usa para intentar mejorar los costos de sus vecinos.

    minCosto -> auxiliar para encontrar w
    Variable temporal que guarda el menor costo encontrado mientras recorre noVisitados.
    Solo existe para poder identificar cuál es w.

    A en Floyd -> matriz de costos mínimos
    Equivalente al D de Dijkstra pero para todos los pares de vértices.
    Se inicializa con C y se va actualizando en las tres iteraciones anidadas.

    vertices en Floyd -> mapeo índice <-> vértice
    Como A es un array y los índices son enteros, se necesita esta lista para convertir:
    vertices.get(i)  da el vértice en la posición i, y vertices.indexOf(v)  da el índice de un vértice v.

     
```
