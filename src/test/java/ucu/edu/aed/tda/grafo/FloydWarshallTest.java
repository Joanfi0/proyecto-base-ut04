package ucu.edu.aed.tda.grafo;

import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

public class FloydWarshallTest extends TestCase {
    private final GraphAlgorithms algorithms = new GraphAlgorithms();

    public void testFloydReconstructsMinimumCostRoute() {
        var result = algorithms.floyd(GraphFixtures.routes());
        assertEquals(List.of("A", "B", "C", "D"), result.getPath("A", "D"));
    }

    public void testWarshallRecoversDirectPath() {
        var graph = GraphFixtures.graph("A", "B");
        GraphFixtures.edge(graph, "A", "B", 1);
        assertEquals(List.of("A", "B"), algorithms.warshall(graph).getPath("A", "B"));
    }

    public void testFloydRecognizesZeroCostConnection() {
        var graph = GraphFixtures.graph("A", "B");
        GraphFixtures.edge(graph, "A", "B", 0);
        assertTrue(algorithms.floyd(graph).connected("A", "B"));
    }

    public void testSelfPathsAndDifferentMeaningsOfZero() {
        var graph = GraphFixtures.graph("A", "B", "Z");
        GraphFixtures.edge(graph, "A", "B", 0);
        var floyd = algorithms.floyd(graph);
        var warshall = algorithms.warshall(graph);
        for (String vertex : graph.vertices()) {
            assertTrue(floyd.connected(vertex, vertex));
            assertTrue(warshall.connected(vertex, vertex));
            assertEquals(List.of(vertex), floyd.getPath(vertex, vertex));
            assertEquals(List.of(vertex), warshall.getPath(vertex, vertex));
            assertEquals(0d, floyd.getCost(vertex, vertex), 0d);
            assertEquals(1d, warshall.getCost(vertex, vertex), 0d);
        }
        assertTrue(floyd.connected("A", "B"));
        assertTrue(warshall.connected("A", "B"));
        assertEquals(0d, floyd.getCost("A", "B"), 0d);
        assertEquals(1d, warshall.getCost("A", "B"), 0d);
        assertEquals(Double.POSITIVE_INFINITY, floyd.getCost("B", "A"), 0d);
        assertEquals(0d, warshall.getCost("B", "A"), 0d);
    }

    public void testUnknownAndUnreachableVerticesReturnNoRoute() {
        var graph = GraphFixtures.graph("A", "B");
        for (var result : List.of(algorithms.floyd(graph), algorithms.warshall(graph))) {
            for (String[] pair : List.of(new String[]{"A", "B"}, new String[]{"Z", "A"},
                    new String[]{"A", "Z"}, new String[]{"Z", "Z"})) {
                assertFalse(result.connected(pair[0], pair[1]));
                assertTrue(result.getPath(pair[0], pair[1]).isEmpty());
            }
            assertEquals(Double.POSITIVE_INFINITY, result.getCost("Z", "A"), 0d);
            assertEquals(Double.POSITIVE_INFINITY, result.getCost("A", "Z"), 0d);
        }
    }

    public void testEmptyGraphQueries() {
        var graph = GraphFixtures.graph();
        for (var result : List.of(algorithms.floyd(graph), algorithms.warshall(graph))) {
            assertFalse(result.connected("A", "A"));
            assertTrue(result.getPath("A", "A").isEmpty());
            assertEquals(Double.POSITIVE_INFINITY, result.getCost("A", "A"), 0d);
        }
    }

    public void testWarshallReturnsAValidRouteNotNecessarilyTheCheapest() {
        var graph = GraphFixtures.routes();
        assertValidPath(graph, algorithms.warshall(graph), "A", "D", false);
        assertEquals(List.of("A", "B", "C", "D"), algorithms.floyd(graph).getPath("A", "D"));
    }

    public void testNegativeEdgeWithoutNegativeCycle() {
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "B", 3);
        GraphFixtures.edge(graph, "B", "C", -4);
        GraphFixtures.edge(graph, "A", "C", 5);
        var result = algorithms.floyd(graph);
        assertEquals(-1d, result.getCost("A", "C"), 0d);
        assertEquals(List.of("A", "B", "C"), result.getPath("A", "C"));
        assertTrue(algorithms.warshall(graph).connected("A", "C"));
    }

    public void testNegativeCyclesAreRejectedIncludingSelfLoop() {
        for (boolean self : List.of(true, false)) {
            var graph = GraphFixtures.graph("A", "B");
            if (self) GraphFixtures.edge(graph, "A", "A", -1);
            else {
                GraphFixtures.edge(graph, "A", "B", -3);
                GraphFixtures.edge(graph, "B", "A", 1);
            }
            try {
                algorithms.floyd(graph);
                fail("Un ciclo negativo impide definir costos minimos finitos");
            } catch (IllegalArgumentException expected) {
                assertNotNull(expected.getMessage());
            }
            assertTrue(algorithms.warshall(graph).connected("A", "A"));
        }
    }

    public void testZeroCostCycleAndPositiveSelfLoop() {
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "A", 5);
        GraphFixtures.edge(graph, "A", "B", 0);
        GraphFixtures.edge(graph, "B", "A", 0);
        GraphFixtures.edge(graph, "B", "C", 1);
        var result = algorithms.floyd(graph);
        assertEquals(0d, result.getCost("A", "A"), 0d);
        assertEquals(1d, result.getCost("A", "C"), 0d);
        assertValidPath(graph, result, "A", "C", true);
        assertValidPath(graph, algorithms.warshall(graph), "A", "C", false);
    }

    public void testAllPairsAgainstDijkstraAndDepthFirstSearchThroughBothClasses() {
        Random random = new Random(20261018);
        for (int scenario = 0; scenario < 12; scenario++) {
            var graph = GraphFixtures.graph("A", "B", "C", "D", "E");
            for (String a : graph.vertices()) for (String b : graph.vertices()) {
                if (random.nextInt(3) == 0) GraphFixtures.edge(graph, a, b, random.nextInt(5));
            }
            var edgesBefore = graph.aristas();
            for (IDirectedGraphAlgorithms alg : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
                var floyd = alg.floyd(graph);
                var warshall = alg.warshall(graph);
                for (String a : graph.vertices()) {
                    var dijkstra = alg.dijkstra(graph.construirComparable(a), graph);
                    Set<String> reachable = new HashSet<>();
                    alg.recorridoEnProfundidad(graph, graph.construirComparable(a), reachable::add);
                    for (String b : graph.vertices()) {
                        assertEquals(dijkstra.getCost(b), floyd.getCost(a, b), 0d);
                        assertEquals(reachable.contains(b), floyd.connected(a, b));
                        assertEquals(reachable.contains(b), warshall.connected(a, b));
                        assertEquals(reachable.contains(b) ? 1d : 0d, warshall.getCost(a, b), 0d);
                        assertValidPath(graph, floyd, a, b, true);
                        assertValidPath(graph, warshall, a, b, false);
                    }
                }
            }
            assertEquals(edgesBefore, graph.aristas());
        }
    }

    private void assertValidPath(DirectedGraph<String, WeightedEdge> graph,
            IFloydWarshallResult<String> result, String a, String b, boolean minimumCost) {
        var path = result.getPath(a, b);
        if (!result.connected(a, b)) {
            assertTrue(path.isEmpty());
            return;
        }
        assertFalse(path.isEmpty());
        assertEquals(a, path.get(0));
        assertEquals(b, path.get(path.size() - 1));
        assertEquals(path.size(), new HashSet<>(path).size());
        double cost = 0;
        for (int i = 1; i < path.size(); i++) {
            var edge = graph.obtenerArista(graph.construirComparable(path.get(i - 1)), graph.construirComparable(path.get(i)));
            assertNotNull(edge);
            cost += edge.dato().getWeight();
        }
        if (minimumCost) assertEquals(result.getCost(a, b), cost, 0d);
    }
}
