package ucu.edu.aed.tda.grafo.algorithms;

import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.interfaces.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.*;
import java.util.function.Consumer;

public class DirectedGraphAlgorithms implements IDirectedGraphAlgorithms {

    private <V, D extends WeightedEdge> double[][] construirMatrizCostos(IDirectedIGraph<V, D> grafo, List<V> vertices) {
        int  n = vertices.size();
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                V source = vertices.get(i);
                V target = vertices.get(j);
                if (grafo.existeArista(grafo.construirComparable(source), grafo.construirComparable(target))) {
                    C[i][j] = grafo.obtenerArista(source, target).dato().getWeight();
                } else {
                    C[i][j] = Double.MAX_VALUE;
                }
            }
        }
        return C;
    }

    /*
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

     */
    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        V origen = grafo.buscarVertice(source);
        Set<V> S = new HashSet<>();
        Map<V, Double> D = new HashMap<>();
        S.add(origen);

        for (V vertice : grafo.vertices()) {
            if (grafo.existeArista(source, grafo.construirComparable(vertice))) {
                D.put(vertice, grafo.obtenerArista(source, grafo.construirComparable(vertice)).dato().getWeight());
            } else {
                D.put(vertice, Double.MAX_VALUE);
            }
        }
        Set<V> noVisitados = new HashSet<>(grafo.vertices());
        noVisitados.removeAll(S);

        while  (!noVisitados.isEmpty()) {
            V w = null;
            double minCosto = Double.MAX_VALUE;
            for (V vertice : noVisitados) {
                if (D.get(vertice) < minCosto) {
                    minCosto = D.get(vertice);
                    w = vertice;
                }
            }
            S.add(w);
            noVisitados.remove(w);
            for (V vertice : noVisitados) {
                if (grafo.existeArista(grafo.construirComparable(w), grafo.construirComparable(vertice))) {
                    double costo = grafo.obtenerArista(grafo.construirComparable(w), grafo.construirComparable(vertice)).dato().getWeight();
                    D.put(vertice, Math.min(D.get(vertice), D.get(w) + costo));
                }
            }
        }
        return null;
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        int n = vertices.size();
        double[][] A = new double[n][n];
        double[][] C = construirMatrizCostos(grafo, vertices);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = C[i][j];
            }
            A[i][i] = 0;
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (A[i][k] + A[k][j] < A[i][j]) {
                        A[i][j] = A[i][k] + A[k][j];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        return null;
    }

    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        return null;
    }

    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        return 0;
    }

    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        return List.of();
    }

    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {

    }

    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {

    }

    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        return List.of();
    }
}
