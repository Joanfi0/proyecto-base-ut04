package ucu.edu.aed.tda.grafo;

import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;

public class AlgorithmsCompatibilityTest extends TestCase {
    public void testGraphAlgorithmsBehavior() {
        verify(new GraphAlgorithms());
    }

    public void testDirectedGraphAlgorithmsBehavior() {
        verify(new DirectedGraphAlgorithms());
    }

    private void verify(IDirectedGraphAlgorithms algorithms) {
        var graph = GraphFixtures.routes();
        var origin = graph.construirComparable("A");
        var destination = graph.construirComparable("D");
        assertEquals(3d, algorithms.dijkstra(origin, graph).getCost("D"), 0d);
        assertEquals(3d, algorithms.floyd(graph).getCost("A", "D"), 0d);
        assertTrue(algorithms.warshall(graph).connected("A", "D"));
        assertFalse(algorithms.warshall(graph).connected("D", "A"));

        List<String> visited = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, origin, visited::add);
        assertEquals(4, visited.size());
        assertEquals(graph.vertices(), new HashSet<>(visited));
        visited.clear();
        algorithms.recorridoEnAmplitud(graph, origin, visited::add);
        assertEquals(4, visited.size());
        assertEquals(graph.vertices(), new HashSet<>(visited));

        Map<List<String>, Double> paths = new HashMap<>();
        var results = algorithms.obtenerTodosLosCaminos(origin, destination, graph);
        for (var path : results) paths.put(path.getPath(), path.getCost());
        assertEquals(2, results.size());
        assertEquals(Map.of(List.of("A", "B", "C", "D"), 3d,
                            List.of("A", "C", "D"), 11d), paths);
    }

    public void testRealCycleIsDetected() {
        var graph = GraphFixtures.graph("A", "B");
        GraphFixtures.edge(graph, "A", "B", 1);
        GraphFixtures.edge(graph, "B", "A", 1);
        assertTrue(graph.tieneCiclos());
    }
}
