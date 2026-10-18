package ucu.edu.aed.tda.grafo;

import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.algorithms.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

public class GraphCenterTest extends TestCase {
    public void testDisconnectedVerticesHaveInfiniteEccentricity() {
        var graph = GraphFixtures.graph("A", "B");
        var algorithms = new GraphAlgorithms();
        assertEquals(Double.POSITIVE_INFINITY, algorithms.obtenerExcentricidad(graph, graph.construirComparable("A")), 0d);
        assertNull(algorithms.obtenerCentroGrafo(graph));
    }

    public void testIncomingDistancesThroughBothClasses() {
        var graph = GraphFixtures.graph("A", "B", "C");
        GraphFixtures.edge(graph, "A", "B", 2);
        GraphFixtures.edge(graph, "B", "C", 3);
        var edges = graph.aristas();
        for (IDirectedGraphAlgorithms algorithms : List.of(new GraphAlgorithms(), new DirectedGraphAlgorithms())) {
            assertEquals(Double.POSITIVE_INFINITY, algorithms.obtenerExcentricidad(graph, graph.construirComparable("A")), 0d);
            assertEquals(Double.POSITIVE_INFINITY, algorithms.obtenerExcentricidad(graph, graph.construirComparable("B")), 0d);
            assertEquals(5d, algorithms.obtenerExcentricidad(graph, graph.construirComparable("C")), 0d);
            assertEquals("C", algorithms.obtenerCentroGrafo(graph));
        }
        assertEquals(edges, graph.aristas());
    }

    public void testLectureExampleUsesColumnMaxima() {
        var graph = GraphFixtures.graph("A", "B", "C", "D", "E");
        String[] vertices = {"A", "B", "C", "D", "E"};
        double[][] costs = {{0,3,1,4,2},{6,0,7,10,3},{4,2,0,8,1},{9,7,5,0,6},{3,6,4,7,0}};
        for (int i = 0; i < 5; i++) for (int j = 0; j < 5; j++) {
            if (i != j) GraphFixtures.edge(graph, vertices[i], vertices[j], costs[i][j]);
        }
        var algorithms = new GraphAlgorithms();
        double[] expected = {9,7,7,10,6};
        for (int i = 0; i < 5; i++) {
            assertEquals(expected[i], algorithms.obtenerExcentricidad(graph, graph.construirComparable(vertices[i])), 0d);
        }
        assertEquals("E", algorithms.obtenerCentroGrafo(graph));
    }

    public void testEmptyGraphAndMissingVertex() {
        var graph = GraphFixtures.graph();
        var algorithms = new CountingAlgorithms();
        assertNull(algorithms.obtenerCentroGrafo(graph));
        assertEquals(Double.POSITIVE_INFINITY, algorithms.obtenerExcentricidad(graph, graph.construirComparable("Z")), 0d);
        graph.agregarVertice("A");
        assertEquals(Double.POSITIVE_INFINITY, algorithms.obtenerExcentricidad(graph, graph.construirComparable("Z")), 0d);
        assertEquals(0, algorithms.floydCalls);
    }

    public void testSingleVertexHasZeroEccentricity() {
        var graph = GraphFixtures.graph("A");
        var algorithms = new GraphAlgorithms();
        assertEquals(0d, algorithms.obtenerExcentricidad(graph, graph.construirComparable("A")), 0d);
        assertEquals("A", algorithms.obtenerCentroGrafo(graph));
    }

    public void testTiedCentersAndZeroWeights() {
        for (double cost : new double[]{0, 4}) {
            var graph = GraphFixtures.graph("A", "B");
            GraphFixtures.edge(graph, "A", "B", cost);
            GraphFixtures.edge(graph, "B", "A", cost);
            var algorithms = new GraphAlgorithms();
            assertEquals(cost, algorithms.obtenerExcentricidad(graph, graph.construirComparable("A")), 0d);
            assertEquals(cost, algorithms.obtenerExcentricidad(graph, graph.construirComparable("B")), 0d);
            assertTrue(graph.vertices().contains(algorithms.obtenerCentroGrafo(graph)));
        }
    }

    public void testCenterCalculatesFloydOnceAndRefreshesAfterGraphChanges() {
        var graph = GraphFixtures.graph("A", "B");
        GraphFixtures.edge(graph, "A", "B", 3);
        var algorithms = new CountingAlgorithms();
        assertEquals("B", algorithms.obtenerCentroGrafo(graph));
        assertEquals(1, algorithms.floydCalls);
        GraphFixtures.edge(graph, "B", "A", 1);
        assertEquals("A", algorithms.obtenerCentroGrafo(graph));
        assertEquals(2, algorithms.floydCalls);
        assertEquals(3d, algorithms.obtenerExcentricidad(graph, graph.construirComparable("B")), 0d);
        assertEquals(3, algorithms.floydCalls);
    }

    private static class CountingAlgorithms extends GraphAlgorithms {
        int floydCalls;
        @Override public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> graph) {
            floydCalls++;
            return super.floyd(graph);
        }
    }
}
