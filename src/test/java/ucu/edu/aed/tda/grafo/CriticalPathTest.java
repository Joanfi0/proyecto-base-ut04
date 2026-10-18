package ucu.edu.aed.tda.grafo;

import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.result.ResultadoCaminoCritico;

public class CriticalPathTest extends TestCase {
    public void testBranchesCostsAndSlackThroughBothClasses() {
        var graph = GraphFixtures.routes();
        var vertices = graph.vertices();
        var edges = graph.aristas();
        for (GraphAlgorithms algorithms : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            var result = algorithms.calcularCaminoCritico(graph);
            assertEquals(11d, result.getDuracion(), 0d);
            assertEquals(2, result.getSecuencias().size());
            assertSequence(result, List.of("A", "C", "D"), 11, 0);
            assertSequence(result, List.of("A", "B", "C", "D"), 3, 8);
            assertEquals(List.of("A", "C", "D"), result.getCaminosCriticos().get(0).getPath());
            assertEquals(1, result.getCaminosCriticos().size());
        }
        assertEquals(vertices, graph.vertices());
        assertEquals(edges, graph.aristas());
    }

    public void testAllTiedCriticalPathsAreReturned() {
        var graph = GraphFixtures.graph("S", "A", "B", "T");
        GraphFixtures.edge(graph, "S", "A", 2);
        GraphFixtures.edge(graph, "A", "T", 3);
        GraphFixtures.edge(graph, "S", "B", 4);
        GraphFixtures.edge(graph, "B", "T", 1);
        var result = new GraphAlgorithms().calcularCaminoCritico(graph);
        assertEquals(5d, result.getDuracion(), 0d);
        assertEquals(2, result.getCaminosCriticos().size());
        assertSequence(result, List.of("S", "A", "T"), 5, 0);
        assertSequence(result, List.of("S", "B", "T"), 5, 0);
    }

    public void testMultipleStartsEndsAndIsolatedComponent() {
        var graph = GraphFixtures.graph("A", "B", "C", "D", "E", "Z");
        GraphFixtures.edge(graph, "A", "C", 2);
        GraphFixtures.edge(graph, "B", "C", 5);
        GraphFixtures.edge(graph, "C", "D", 1);
        GraphFixtures.edge(graph, "C", "E", 3);
        var result = new GraphAlgorithms().calcularCaminoCritico(graph);
        assertEquals(8d, result.getDuracion(), 0d);
        assertEquals(5, result.getSecuencias().size());
        assertSequence(result, List.of("A", "C", "D"), 3, 5);
        assertSequence(result, List.of("A", "C", "E"), 5, 3);
        assertSequence(result, List.of("B", "C", "D"), 6, 2);
        assertSequence(result, List.of("B", "C", "E"), 8, 0);
        assertSequence(result, List.of("Z"), 0, 8);
    }

    public void testEmptySingleVertexAndZeroDuration() {
        var graph = GraphFixtures.graph();
        var algorithms = new GraphAlgorithms();
        var empty = algorithms.calcularCaminoCritico(graph);
        assertEquals(0d, empty.getDuracion(), 0d);
        assertTrue(empty.getSecuencias().isEmpty());
        assertTrue(empty.getCaminosCriticos().isEmpty());
        graph.agregarVertice("A");
        assertSequence(algorithms.calcularCaminoCritico(graph), List.of("A"), 0, 0);
        GraphFixtures.edge(graph, "A", "B", 0);
        assertSequence(algorithms.calcularCaminoCritico(graph), List.of("A", "B"), 0, 0);
    }

    public void testDecimalsDoNotLoseATiedCriticalPath() {
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "B", 0.1);
        GraphFixtures.edge(graph, "B", "C", 0.2);
        GraphFixtures.edge(graph, "A", "C", 0.3);
        var result = new GraphAlgorithms().calcularCaminoCritico(graph);
        assertEquals(0.3, result.getDuracion(), 1e-12);
        assertEquals(2, result.getCaminosCriticos().size());
        for (var sequence : result.getSecuencias()) assertEquals(0d, sequence.getHolgura(), 0d);
    }

    public void testRejectsCyclesIncludingDisconnectedSelfLoop() {
        var graph = GraphFixtures.routes();
        GraphFixtures.edge(graph, "X", "X", 0);
        try {
            new GraphAlgorithms().calcularCaminoCritico(graph);
            fail("Un proyecto con ciclos no es valido");
        } catch (IllegalArgumentException expected) { }
    }

    public void testRejectsInvalidDurationsAndOverflow() {
        for (double weight : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            var graph = GraphFixtures.graph("A", "B");
            GraphFixtures.edge(graph, "A", "B", weight);
            try {
                new GraphAlgorithms().calcularCaminoCritico(graph);
                fail("Duracion invalida");
            } catch (IllegalArgumentException expected) { }
        }
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "B", Double.MAX_VALUE);
        GraphFixtures.edge(graph, "B", "C", Double.MAX_VALUE);
        try {
            new GraphAlgorithms().calcularCaminoCritico(graph);
            fail("La suma desborda double");
        } catch (IllegalArgumentException expected) { }
    }

    public void testResultsDoNotChangeWithGraphOrReturnedCollections() {
        var graph = GraphFixtures.routes();
        var algorithms = new GraphAlgorithms();
        var result = algorithms.calcularCaminoCritico(graph);
        GraphFixtures.edge(graph, "D", "E", 5);
        assertEquals(16d, algorithms.calcularCaminoCritico(graph).getDuracion(), 0d);
        assertEquals(11d, result.getDuracion(), 0d);
        try { result.getSecuencias().clear(); fail("Lista inmodificable"); }
        catch (UnsupportedOperationException expected) { }
        try { result.getCaminosCriticos().get(0).getPath().clear(); fail("Ruta inmodificable"); }
        catch (UnsupportedOperationException expected) { }
    }

    public void testGeneratedDagsHaveConsistentCostsAndMaximum() {
        Random random = new Random(9);
        for (int sample = 0; sample < 12; sample++) {
            var graph = GraphFixtures.graph("A", "B", "C", "D", "E");
            var vertices = new ArrayList<>(graph.vertices());
            for (int i = 0; i < vertices.size(); i++) for (int j = i + 1; j < vertices.size(); j++) {
                if (random.nextBoolean()) GraphFixtures.edge(graph, vertices.get(i), vertices.get(j), random.nextInt(6));
            }
            var result = new GraphAlgorithms().calcularCaminoCritico(graph);
            double maximum = 0;
            Set<List<String>> seen = new HashSet<>();
            for (var sequence : result.getSecuencias()) {
                var path = sequence.getCamino().getPath();
                assertTrue(seen.add(path));
                assertEquals(0, graph.gradoDeEntrada(graph.construirComparable(path.get(0))));
                assertEquals(0, graph.gradoDeSalida(graph.construirComparable(path.get(path.size() - 1))));
                double cost = 0;
                for (int i = 1; i < path.size(); i++) {
                    var edge = graph.obtenerArista(graph.construirComparable(path.get(i - 1)), graph.construirComparable(path.get(i)));
                    assertNotNull(edge);
                    cost += edge.dato().getWeight();
                }
                assertEquals(cost, sequence.getCamino().getCost(), 0d);
                assertEquals(result.getDuracion() - cost, sequence.getHolgura(), 0d);
                maximum = Math.max(maximum, cost);
            }
            assertEquals(maximum, result.getDuracion(), 0d);
            assertFalse(result.getCaminosCriticos().isEmpty());
        }
    }

    private void assertSequence(ResultadoCaminoCritico<String> result, List<String> path, double cost, double slack) {
        for (var sequence : result.getSecuencias()) {
            if (path.equals(sequence.getCamino().getPath())) {
                assertEquals(cost, sequence.getCamino().getCost(), 0d);
                assertEquals(slack, sequence.getHolgura(), 0d);
                assertEquals(slack == 0, sequence.esCritica());
                return;
            }
        }
        fail("Falta la secuencia " + path);
    }
}
