package ucu.edu.aed.tda.grafo;

import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;

public class GraphStructureTest extends TestCase {
    private final GraphAlgorithms algorithms = new GraphAlgorithms();

    public void testConvergingPathsAreNotACycle() {
        assertFalse(GraphFixtures.routes().tieneCiclos());
    }

    public void testRemovingLastVertexReturnsTrue() {
        var graph = GraphFixtures.graph("A");
        assertTrue(graph.removerVertice(graph.construirComparable("A")));
        assertTrue(graph.vertices().isEmpty());
    }

    public void testRemovingMissingVertexReturnsFalse() {
        var graph = GraphFixtures.graph("A");
        assertFalse(graph.removerVertice(graph.construirComparable("Z")));
        assertEquals(Set.of("A"), graph.vertices());
    }

    public void testFullDepthFirstTraversalVisitsEachVertexOnce() {
        var graph = GraphFixtures.routes();
        graph.agregarVertice("Z"); // Tambien debe cubrir componentes desconectadas.
        List<String> visited = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, visited::add);
        assertEquals(graph.vertices(), new HashSet<>(visited));
        assertEquals(graph.cantidadDeVertices(), visited.size());
    }

    public void testFullBreadthFirstTraversalVisitsEachVertexOnce() {
        var graph = GraphFixtures.routes();
        graph.agregarVertice("Z");
        List<String> visited = new ArrayList<>();
        algorithms.recorridoEnAmplitud(graph, visited::add);
        assertEquals(graph.vertices(), new HashSet<>(visited));
        assertEquals(graph.cantidadDeVertices(), visited.size());
    }

    public void testRemovalDeletesOnlyIncidentEdges() {
        var graph = GraphFixtures.routes();
        GraphFixtures.edge(graph, "C", "C", 2);
        assertTrue(graph.removerVertice(graph.construirComparable("C")));
        assertEquals(Set.of("A", "B", "D"), graph.vertices());
        assertEquals(1, graph.cantidadDeAristas());
        assertNotNull(graph.obtenerArista(graph.construirComparable("A"), graph.construirComparable("B")));
        assertFalse(graph.removerVertice(graph.construirComparable("C")));
    }

    public void testEmptyGraphAndIsolatedVerticesHaveNoCycles() {
        var graph = GraphFixtures.graph();
        assertFalse(graph.tieneCiclos());
        assertFalse(graph.removerVertice(graph.construirComparable("A")));
        graph.agregarVertice("A");
        graph.agregarVertice("B");
        assertFalse(graph.tieneCiclos());
    }

    public void testSelfLoopIsACycleAndStateIsNotRetained() {
        var graph = GraphFixtures.graph("A");
        GraphFixtures.edge(graph, "A", "A", 1);
        assertTrue(graph.tieneCiclos());
        graph.eliminarArista(graph.construirComparable("A"), graph.construirComparable("A"));
        assertFalse(graph.tieneCiclos());
    }

    public void testCycleInDisconnectedComponentDoesNotModifyGraph() {
        var graph = GraphFixtures.routes();
        GraphFixtures.edge(graph, "X", "Y", 1);
        GraphFixtures.edge(graph, "Y", "X", 1);
        var vertices = graph.vertices();
        var edges = graph.aristas();
        assertTrue(graph.tieneCiclos());
        assertTrue(graph.tieneCiclos());
        assertEquals(vertices, graph.vertices());
        assertEquals(edges, graph.aristas());
    }

    public void testTraversalsThroughBothAlgorithmClasses() {
        for (IDirectedGraphAlgorithms alg : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            var graph = GraphFixtures.routes();
            GraphFixtures.edge(graph, "D", "A", 1);
            GraphFixtures.edge(graph, "B", "B", 1);
            graph.agregarVertice("Z");
            var edges = graph.aristas();
            for (int run = 0; run < 2; run++) {
                List<String> depth = new ArrayList<>();
                List<String> breadth = new ArrayList<>();
                alg.recorridoEnProfundidad(graph, depth::add);
                alg.recorridoEnAmplitud(graph, breadth::add);
                assertOnce(graph.vertices(), depth);
                assertOnce(graph.vertices(), breadth);
                depth.clear();
                breadth.clear();
                alg.recorridoEnProfundidad(graph, graph.construirComparable("A"), depth::add);
                alg.recorridoEnAmplitud(graph, graph.construirComparable("A"), breadth::add);
                assertOnce(Set.of("A", "B", "C", "D"), depth);
                assertOnce(Set.of("A", "B", "C", "D"), breadth);
                assertEquals("A", depth.get(0));
                assertEquals("A", breadth.get(0));
                assertEquals(Set.of("B", "C"), new HashSet<>(breadth.subList(1, 3)));
                assertEquals("D", breadth.get(3));
            }
            assertEquals(edges, graph.aristas());
        }
    }

    public void testEmptyTraversalsAndMissingOriginsDoNotVisitAnything() {
        for (IDirectedGraphAlgorithms alg : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            var graph = GraphFixtures.graph();
            List<String> visited = new ArrayList<>();
            alg.recorridoEnProfundidad(graph, visited::add);
            alg.recorridoEnAmplitud(graph, visited::add);
            graph.agregarVertice("A");
            alg.recorridoEnProfundidad(graph, graph.construirComparable("Z"), visited::add);
            alg.recorridoEnAmplitud(graph, graph.construirComparable("Z"), visited::add);
            assertTrue(visited.isEmpty());
        }
    }

    public void testStrongConnectivityStillUsesSourceTraversal() {
        var graph = GraphFixtures.graph("A", "B");
        GraphFixtures.edge(graph, "A", "B", 1);
        assertFalse(graph.esConexo());
        GraphFixtures.edge(graph, "B", "A", 1);
        assertTrue(graph.esConexo());
        graph.agregarVertice("Z");
        assertFalse(graph.esConexo());
    }

    private void assertOnce(Set<String> expected, List<String> actual) {
        assertEquals(expected, new HashSet<>(actual));
        assertEquals(expected.size(), actual.size());
    }
}
