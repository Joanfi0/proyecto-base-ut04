package ucu.edu.aed.tda.grafo;

import java.io.IOException;
import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.aeropuertos.CargadorGrafo;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedIGraph;

public class TopologicalSortTest extends TestCase {
    public void testEmptyAndSingleVertex() {
        var graph = GraphFixtures.graph();
        var algorithms = new GraphAlgorithms();
        assertTrue(algorithms.calcularClasificacionTopologica(graph).isEmpty());
        graph.agregarVertice("A");
        assertEquals(List.of("A"), algorithms.calcularClasificacionTopologica(graph));
    }

    public void testChainHasUniqueOrder() {
        var graph = GraphFixtures.routes();
        assertEquals(List.of("A", "B", "C", "D"), new GraphAlgorithms().calcularClasificacionTopologica(graph));
    }

    public void testBranchesDisconnectedComponentsAndBothClasses() {
        var graph = GraphFixtures.routes();
        GraphFixtures.edge(graph, "X", "Y", 1);
        graph.agregarVertice("Z");
        for (IDirectedGraphAlgorithms algorithms : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            var vertices = graph.vertices();
            var edges = graph.aristas();
            assertOrder(graph, algorithms.calcularClasificacionTopologica(graph));
            assertOrder(graph, algorithms.calcularClasificacionTopologica(graph));
            assertEquals(vertices, graph.vertices());
            assertEquals(edges, graph.aristas());
        }
    }

    public void testGenericVerticesAndMultipleValidOrders() {
        var graph = new DirectedGraph<Integer, String>();
        graph.agregarArista(10, 30, "precedencia");
        graph.agregarArista(20, 30, "precedencia");
        graph.agregarVertice(40);
        assertOrder(graph, new GraphAlgorithms().calcularClasificacionTopologica(graph));
    }

    public void testSelfLoopAndDisconnectedCycleAreRejected() {
        for (boolean self : List.of(true, false)) {
            var graph = GraphFixtures.routes();
            GraphFixtures.edge(graph, "X", self ? "X" : "Y", 1);
            if (!self) GraphFixtures.edge(graph, "Y", "X", 1);
            var edges = graph.aristas();
            for (IDirectedGraphAlgorithms algorithms : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
                try {
                    algorithms.calcularClasificacionTopologica(graph);
                    fail("No debe devolver un orden parcial cuando hay ciclos");
                } catch (IllegalArgumentException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
            assertEquals(edges, graph.aristas());
        }
    }

    public void testCycleRemovalAllowsNextCall() {
        var graph = GraphFixtures.routes();
        GraphFixtures.edge(graph, "D", "A", 1);
        var algorithms = new GraphAlgorithms();
        try {
            algorithms.calcularClasificacionTopologica(graph);
            fail("Hay un ciclo");
        } catch (IllegalArgumentException expected) { }
        graph.eliminarArista(graph.construirComparable("D"), graph.construirComparable("A"));
        assertOrder(graph, algorithms.calcularClasificacionTopologica(graph));
    }

    public void testProvidedFlightScenariosHaveNoTopologicalOrder() throws IOException {
        for (String suffix : List.of("", "_2")) {
            var graph = new CargadorGrafo().cargarRecursos("aeropuertos/aeropuertos" + suffix + ".txt",
                    "aeropuertos/conexiones" + suffix + ".txt");
            try {
                new GraphAlgorithms().calcularClasificacionTopologica(graph);
                fail("Los vuelos de estos escenarios contienen ciclos");
            } catch (IllegalArgumentException expected) { }
        }
    }

    public void testGeneratedAcyclicGraphsRespectEveryPrecedence() {
        Random random = new Random(8);
        for (int scenario = 0; scenario < 15; scenario++) {
            var graph = new DirectedGraph<Integer, Integer>();
            for (int i = 0; i < 12; i++) graph.agregarVertice(i);
            for (int i = 0; i < 12; i++) for (int j = i + 1; j < 12; j++) {
                if (random.nextBoolean()) graph.agregarArista(i, j, 1);
            }
            assertOrder(graph, new GraphAlgorithms().calcularClasificacionTopologica(graph));
        }
    }

    private <V, D> void assertOrder(IDirectedIGraph<V, D> graph, List<V> order) {
        assertEquals(graph.cantidadDeVertices(), order.size());
        assertEquals(graph.vertices(), new HashSet<>(order));
        Map<V, Integer> positions = new HashMap<>();
        for (int i = 0; i < order.size(); i++) positions.put(order.get(i), i);
        for (var edge : graph.aristas()) {
            assertTrue(positions.get(edge.source()) < positions.get(edge.target()));
        }
    }
}
