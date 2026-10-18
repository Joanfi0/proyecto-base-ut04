package ucu.edu.aed.tda.grafo;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;

public class DijkstraTest extends TestCase {
    private final GraphAlgorithms algorithms = new GraphAlgorithms();

    public void testDijkstraPathHasNoNullVertex() {
        var graph = GraphFixtures.routes();
        assertEquals(List.of("A", "B", "C", "D"),
                algorithms.dijkstra(graph.construirComparable("A"), graph).getPath("D"));
    }

    public void testDijkstraTerminatesWithUnreachableDestination() throws Exception {
        // Proceso separado: una regresion con bucle infinito no debe bloquear la suite.
        String java = new File(System.getProperty("java.home"), "bin/java.exe").getPath();
        if (!new File(java).exists()) java = new File(System.getProperty("java.home"), "bin/java").getPath();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Process process = new ProcessBuilder(java, "-cp", classpath, UnreachableProbe.class.getName())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
        try {
            assertTrue("Dijkstra debe terminar aunque B sea inalcanzable", process.waitFor(5, TimeUnit.SECONDS));
            assertEquals("El destino inalcanzable debe conservar costo infinito", 0, process.exitValue());
        } finally {
            if (process.isAlive()) process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
    }

    public static class UnreachableProbe {
        public static void main(String[] args) {
            var graph = GraphFixtures.graph("A", "B");
            var result = new GraphAlgorithms().dijkstra(graph.construirComparable("A"), graph);
            if (result.getCost("B") != Double.POSITIVE_INFINITY) throw new AssertionError("Costo de B");
        }
    }

    public void testOriginHasZeroCostAndSingletonPath() {
        var graph = GraphFixtures.graph("A");
        GraphFixtures.edge(graph, "A", "A", 4);
        var result = algorithms.dijkstra(graph.construirComparable("A"), graph);
        assertEquals(0d, result.getCost("A"), 0d);
        assertEquals(List.of("A"), result.getPath("A"));
    }

    public void testUnreachableAndUnknownDestinationsHaveNoPath() {
        var graph = GraphFixtures.routes();
        graph.agregarVertice("Z");
        var result = algorithms.dijkstra(graph.construirComparable("A"), graph);
        for (String destination : List.of("Z", "UNKNOWN")) {
            assertEquals(Double.POSITIVE_INFINITY, result.getCost(destination), 0d);
            assertTrue(result.getPath(destination).isEmpty());
        }
        assertEquals(3d, result.getCost("D"), 0d);
        assertEquals(List.of("A", "B", "C", "D"), result.getPath("D"));
    }

    public void testMissingOriginRetainsExistingNullContract() {
        var graph = GraphFixtures.graph();
        assertNull(algorithms.dijkstra(graph.construirComparable("A"), graph));
        graph.agregarVertice("B");
        assertNull(algorithms.dijkstra(graph.construirComparable("A"), graph));
    }

    public void testZeroWeightCycleTerminatesWithValidPaths() {
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "B", 0);
        GraphFixtures.edge(graph, "B", "A", 0);
        GraphFixtures.edge(graph, "B", "C", 0);
        var result = algorithms.dijkstra(graph.construirComparable("A"), graph);
        assertEquals(0d, result.getCost("C"), 0d);
        assertEquals(List.of("A", "B", "C"), result.getPath("C"));
    }

    public void testRejectsNegativeWeightsEverywhere() {
        for (IDirectedGraphAlgorithms alg : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            for (String[] edge : List.of(new String[]{"A", "B"}, new String[]{"B", "C"}, new String[]{"X", "Y"})) {
                var graph = GraphFixtures.graph("A", "B", "C", "X", "Y");
                GraphFixtures.edge(graph, "A", "B", 1);
                graph.eliminarArista(graph.construirComparable(edge[0]), graph.construirComparable(edge[1]));
                GraphFixtures.edge(graph, edge[0], edge[1], -1);
                try {
                    alg.dijkstra(graph.construirComparable("A"), graph);
                    fail("Debe rechazar pesos negativos incluso fuera de las aristas del origen");
                } catch (IllegalArgumentException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
    }

    public void testBothClassesProduceRoutesWhoseCostsMatch() {
        for (IDirectedGraphAlgorithms alg : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            var graph = GraphFixtures.routes();
            // Empate: no se exige una ruta especifica si ambas son minimas.
            GraphFixtures.edge(graph, "A", "D", 3);
            var edgesBefore = graph.aristas();
            for (int run = 0; run < 2; run++) {
                var result = alg.dijkstra(graph.construirComparable("A"), graph);
                assertEquals(3d, result.getCost("D"), 0d);
                for (String destination : graph.vertices()) {
                    var path = result.getPath(destination);
                    assertEquals("A", path.get(0));
                    assertEquals(destination, path.get(path.size() - 1));
                    assertEquals(path.size(), new HashSet<>(path).size());
                    double total = 0;
                    for (int i = 1; i < path.size(); i++) {
                        var edge = graph.obtenerArista(graph.construirComparable(path.get(i - 1)), graph.construirComparable(path.get(i)));
                        assertNotNull(edge);
                        total += edge.dato().getWeight();
                    }
                    assertEquals(result.getCost(destination), total, 0d);
                }
            }
            assertEquals(edgesBefore, graph.aristas());
        }
    }
}
