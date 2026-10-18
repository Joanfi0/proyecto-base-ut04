package ucu.edu.aed.tda.grafo.algorithms;

import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.interfaces.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.implementation.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.implementation.FloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;
import ucu.edu.aed.tda.grafo.model.result.ResultadoCaminoCritico;

import java.util.*;
import java.util.function.Consumer;

public class GraphAlgorithms implements IDirectedGraphAlgorithms {
    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        V src = grafo.buscarVertice(source);
        if (src == null) return null;
        // La precondicion se aplica a todo el grafo, incluso a otras componentes.
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato().getWeight() < 0) {
                throw new IllegalArgumentException("Dijkstra requiere pesos no negativos");
            }
        }
        Map<V, Double> D = new HashMap<>();
        Map<V, V> P = new HashMap<>();
        Set<V> V = grafo.vertices();
        Set<V> S = new HashSet<>(V);
        S.remove(src);

        for (V v : V) {
            Edge<V, D> e = grafo.obtenerArista(src, v);
            if (e == null) {
                D.put(v, Double.POSITIVE_INFINITY);
            } else {
                double peso = e.dato().getWeight();
                D.put(v, peso);
                P.put(v, src);
            }
        }

        P.put(src, null);
        D.put(src, 0d);

        while (!S.isEmpty()) {
            V w = null;
            double cost = Double.POSITIVE_INFINITY;
            for (V v : S) {
                double localCost = D.getOrDefault(v, Double.POSITIVE_INFINITY);
                if (localCost < cost) {
                    w = v;
                    cost = localCost;
                }
            }
            // Los vertices restantes son inalcanzables: conservan costo infinito.
            if (w == null) break;
            S.remove(w);
            for (V v : S) {
                Edge<V, D> arista = grafo.obtenerArista(w, v);
                if (arista != null) {
                    double d = cost + arista.dato().getWeight();
                    if (d < D.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                        D.put(v, d);
                        P.put(v, w);
                    }
                }
            }
        }

        return new DijkstraResult<V>(D, P);
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        Set<V> V = grafo.vertices();
        Map<V, Map<V, Double>> A = new HashMap<>();
        Map<V, Map<V, V>> P = new HashMap<>();

        for (V i : V) {
            A.putIfAbsent(i, new HashMap<>());
            P.putIfAbsent(i, new HashMap<>());
            A.get(i).put(i, 0d);

            for (V j : V) {
                Edge<V, D> e = grafo.obtenerArista(i, j);
                if (Objects.equals(i, j)) {
                    A.get(i).put(j, e == null ? 0d : Math.min(0d, e.dato().getWeight()));
                } else if (e != null) {
                    A.get(i).put(j, e.dato().getWeight());
                    P.get(i).put(j, j);
                } else {
                    A.get(i).put(j, Double.POSITIVE_INFINITY);
                }
            }
        }

        for (V k : V) {
            for (V i : V) {
                for (V j : V) {
                    double c = A.get(i).getOrDefault(k, Double.POSITIVE_INFINITY) + A.get(k).getOrDefault(j, Double.POSITIVE_INFINITY);
                    if (c < A.get(i).getOrDefault(j, Double.POSITIVE_INFINITY)) {
                        A.get(i).put(j, c);
                        // Guardamos el primer paso hacia k, no el intermediario k.
                        P.get(i).put(j, P.get(i).get(k));
                    }
                }
            }
        }

        for (V vertex : V) {
            if (A.get(vertex).get(vertex) < 0) {
                throw new IllegalArgumentException("Floyd no tiene caminos minimos finitos con ciclos negativos");
            }
        }
        return new FloydWarshallResult<>(A, P);
    }


    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        Set<V> V = grafo.vertices();
        Map<V, Map<V, Double>> A = new HashMap<>();
        Map<V, Map<V, V>> P = new HashMap<>();

        for (V i : V) {
            A.putIfAbsent(i, new HashMap<>());
            P.putIfAbsent(i, new HashMap<>());

            for (V j : V) {
                Edge<V, D> e = grafo.obtenerArista(i, j);
                if (Objects.equals(i, j)) {
                    A.get(i).put(j, 1d);
                } else {
                    A.get(i).put(j, e == null ? 0d : 1d);
                    if (e != null) P.get(i).put(j, j);
                }
            }
        }

        for (V k : V) {
            for (V i : V) {
                for (V j : V) {
                    if (A.get(i).get(j) == 0) {
                        boolean hasPath = A.get(i).get(k) == 1 && A.get(k).get(j) == 1;
                        A.get(i).put(j, hasPath ? 1d : 0d);
                        if (hasPath) {
                            P.get(i).put(j, P.get(i).get(k));
                        }
                    }
                }
            }
        }
        return new FloydWarshallResult<>(A, P, FloydWarshallResult.Representation.REACHABILITY);
    }


    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        Set<V> vertices = grafo.vertices();
        if (vertices.isEmpty()) return null;
        return obtenerCentroGrafo(grafo, floyd(grafo));
    }

    /** Reutiliza un resultado de Floyd correspondiente al estado actual del grafo. */
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo,
                                                          IFloydWarshallResult<V> distancias) {
        Set<V> vertices = grafo.vertices();
        V node = null;
        double x = Double.POSITIVE_INFINITY;
        for (V vertex : vertices) {
            double v = excentricidadDeEntrada(vertices, vertex, distancias);
            if (v < x) {
                node = vertex;
                x = v;
            }
        }
        return node;
    }

    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V v = grafo.buscarVertice(vertexCriteria);
        if (v == null) {
            return Double.POSITIVE_INFINITY;
        }
        return excentricidadDeEntrada(grafo.vertices(), v, floyd(grafo));
    }

    private <V> double excentricidadDeEntrada(Set<V> vertices, V destino, IFloydWarshallResult<V> distancias) {
        // Catedra: maximo de la columna, distancias desde cada origen hacia destino.
        double max = Double.NEGATIVE_INFINITY;
        for (V origen : vertices) {
            max = Math.max(max, distancias.getCost(origen, destino));
        }
        return max;
    }


    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        List<Path<V>> paths = new LinkedList<>();
        V src = grafo.buscarVertice(source);

        if (src != null) {

            Stack<V> stack = new Stack<>();
            Set<V> visitados = new HashSet<>();
            todosLosCaminos(stack, src, target, visitados, paths, 0, grafo);
        }

        return paths;
    }
    private <V, D extends WeightedEdge> void todosLosCaminos(Stack<V> stack, V node, Comparable<V> targetCriteria, Set<V> visitados, List<Path<V>> caminos, double cost, IGraph<V, D> grafo) {
        if (visitados.contains(node)) {
            return;
        }

        stack.push(node);
        visitados.add(node);

        if (targetCriteria.compareTo(node) == 0) {
            caminos.add(new Path<>(List.copyOf(stack), cost));
        } else {
            for (Edge<V, D> adyacencia : grafo.adyacencias(grafo.construirComparable(node))) {
                todosLosCaminos(stack, adyacencia.target(), targetCriteria, visitados, caminos, cost + adyacencia.dato().getWeight(), grafo);
            }
        }

        stack.pop();
        visitados.remove(node);
    }


    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V v = grafo.buscarVertice(sourceCriteria);
        if (v == null) return;

        recorridoEnProfundidadAux(new HashSet<>(), v, grafo, consumer);
    }


    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Consumer<V> consumer) {
        // Compartir visitados permite cubrir todas las componentes sin repetir vertices.
        Set<V> visitados = new HashSet<>();
        for (V vertex : grafo.vertices()) {
            if (!visitados.contains(vertex)) {
                recorridoEnProfundidadAux(visitados, vertex, grafo, consumer);
            }
        }
    }

    private <V, D> void recorridoEnProfundidadAux(Set<V> visitados, V node, IGraph<V, D> grafo, Consumer<V> consumer) {
        consumer.accept(node);
        visitados.add(node);

        List<Edge<V, D>> edges = grafo.adyacencias(grafo.construirComparable(node));

        for (Edge<V, D> edge : edges) {
            V target = edge.target();
            if (!visitados.contains(target)) {
                recorridoEnProfundidadAux(visitados, target, grafo, consumer);
            }
        }
    }

    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V src = grafo.buscarVertice(sourceCriteria);
        if (src == null) return;
        recorridoEnAmplitudAux(new HashSet<>(), src, grafo, consumer);
    }

    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Consumer<V> consumer) {
        Set<V> visitados = new HashSet<>();
        for (V vertex : grafo.vertices()) {
            if (!visitados.contains(vertex)) {
                recorridoEnAmplitudAux(visitados, vertex, grafo, consumer);
            }
        }
    }

    private <V, D> void recorridoEnAmplitudAux(Set<V> visitados, V src, IGraph<V, D> grafo, Consumer<V> consumer) {
        Queue<V> queue = new LinkedList<>();
        // Marcar al encolar evita agregar varias veces un mismo destino.
        visitados.add(src);
        queue.add(src);
        while (!queue.isEmpty()) {
            V v = queue.poll();
            consumer.accept(v);
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(v))) {
                V target = edge.target();
                if (visitados.add(target)) {
                    queue.add(target);
                }
            }
        }
    }


    /**
     * Proyecto con duraciones en las aristas. Incluye todos los caminos desde
     * vertices sin entradas hasta vertices sin salidas, incluso otras componentes.
     * Rechaza ciclos y duraciones negativas o no finitas.
     */
    public <V, D extends WeightedEdge> ResultadoCaminoCritico<V> calcularCaminoCritico(IDirectedIGraph<V, D> grafo) {
        List<V> orden = calcularClasificacionTopologica(grafo);
        Map<V, List<Edge<V, D>>> salientes = new HashMap<>();
        Set<V> inicios = new HashSet<>(orden);
        Map<V, Double> duraciones = new HashMap<>();
        for (V vertex : orden) {
            salientes.put(vertex, new ArrayList<>());
            duraciones.put(vertex, 0d);
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato() == null || !Double.isFinite(arista.dato().getWeight()) || arista.dato().getWeight() < 0) {
                throw new IllegalArgumentException("Las duraciones deben ser finitas y no negativas");
            }
            salientes.get(arista.source()).add(arista);
            inicios.remove(arista.target());
        }

        List<V> finales = new ArrayList<>();
        double duracion = 0;
        // En orden topologico, cada vertice recibe la mayor duracion de sus precedencias.
        for (V vertex : orden) {
            if (salientes.get(vertex).isEmpty()) {
                finales.add(vertex);
                duracion = Math.max(duracion, duraciones.get(vertex));
            }
            for (Edge<V, D> arista : salientes.get(vertex)) {
                double candidata = duraciones.get(vertex) + arista.dato().getWeight();
                if (!Double.isFinite(candidata)) throw new IllegalArgumentException("La duracion total excede el rango de double");
                duraciones.put(arista.target(), Math.max(duraciones.get(arista.target()), candidata));
            }
        }

        // La consigna pide enumerar todas las secuencias: reutilizamos el retroceso existente.
        List<Path<V>> caminos = new ArrayList<>();
        for (V inicio : inicios) {
            for (V fin : finales) {
                caminos.addAll(obtenerTodosLosCaminos(grafo.construirComparable(inicio),
                        grafo.construirComparable(fin), grafo));
            }
        }
        return new ResultadoCaminoCritico<>(duracion, caminos);
    }

    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        Set<V> vertices = grafo.vertices();
        Map<V, Integer> grados = new HashMap<>();
        Map<V, List<V>> sucesores = new HashMap<>();
        for (V vertex : vertices) {
            grados.put(vertex, 0);
            sucesores.put(vertex, new ArrayList<>());
        }
        // Una sola lectura de las aristas evita volver a escanear el grafo por vertice.
        for (Edge<V, D> arista : grafo.aristas()) {
            sucesores.get(arista.source()).add(arista.target());
            grados.put(arista.target(), grados.get(arista.target()) + 1);
        }
        Queue<V> disponibles = new LinkedList<>();
        for (V vertex : vertices) {
            if (grados.get(vertex) == 0) disponibles.add(vertex);
        }
        List<V> orden = new ArrayList<>();
        while (!disponibles.isEmpty()) {
            V vertex = disponibles.remove();
            orden.add(vertex);
            for (V sucesor : sucesores.get(vertex)) {
                int grado = grados.get(sucesor) - 1;
                grados.put(sucesor, grado);
                if (grado == 0) disponibles.add(sucesor);
            }
        }
        if (orden.size() != vertices.size()) {
            throw new IllegalArgumentException("Un grafo con ciclos no tiene clasificacion topologica");
        }
        return orden;
    }
}
