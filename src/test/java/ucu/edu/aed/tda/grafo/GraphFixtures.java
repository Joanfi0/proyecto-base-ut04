package ucu.edu.aed.tda.grafo;

import java.util.Set;
import java.util.TreeSet;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

final class GraphFixtures {
    private GraphFixtures() { }

    // Orden fijo para reproducir la reconstruccion de Floyd independientemente del HashSet.
    static DirectedGraph<String, WeightedEdge> graph(String... vertices) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>() {
            @Override public Set<String> vertices() {
                return new TreeSet<>(super.vertices());
            }
        };
        for (String vertex : vertices) graph.agregarVertice(vertex);
        return graph;
    }

    static DirectedGraph<String, WeightedEdge> routes() {
        var graph = graph("A", "B", "C", "D");
        edge(graph, "A", "C", 10);
        edge(graph, "A", "B", 1);
        edge(graph, "B", "C", 1);
        edge(graph, "C", "D", 1);
        return graph;
    }

    static void edge(DirectedGraph<String, WeightedEdge> graph, String a, String b, double cost) {
        graph.agregarArista(a, b, new WeightedEdge(cost));
    }
}
