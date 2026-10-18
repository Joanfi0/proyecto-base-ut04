package ucu.edu.aed.tda.grafo;

import java.io.*;
import java.util.*;
import junit.framework.TestCase;
import ucu.edu.aed.aeropuertos.*;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.interfaces.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

public class AplicacionAerolineaTest extends TestCase {
    private String session(String input) throws IOException {
        return session(input, new CargadorGrafo(), new GraphAlgorithms());
    }

    private String session(String input, CargadorGrafo loader, GraphAlgorithms algorithms) throws IOException {
        StringWriter output = new StringWriter();
        new AplicacionAerolinea(new StringReader(input), output, loader, algorithms).ejecutar();
        return output.toString();
    }

    public void testRealResourcesDistanceAndRouteWithSpacesAndLowercase() throws IOException {
        String out = session("2\nmontevideo\nbuenos aires\n3\nMontevideo\nBuenos_Aires\n0\n");
        assertTrue(out.contains("Escenario 1 cargado: 8 aeropuertos, 24 conexiones."));
        assertTrue(out.contains("Distancia minima: 200.0"));
        assertTrue(out.contains("Itinerario: Montevideo -> Buenos_Aires"));
        assertTrue(out.contains("Hasta luego."));
    }

    public void testSecondScenarioCyclesUnreachableAndNoCenter() throws IOException {
        String out = session("1\n2\n6\n2\nMontevideo\nAsuncion\n4\n0\n");
        assertTrue(out.contains("Escenario 2 cargado: 9 aeropuertos, 15 conexiones."));
        assertTrue(out.contains("El grafo contiene ciclos."));
        assertTrue(out.contains("No hay camino de Montevideo a Asuncion."));
        assertTrue(out.contains("No existe un centro accesible desde todos los aeropuertos."));
    }

    public void testInvalidInputsRecoverWithoutChangingScenario() throws IOException {
        String out = session("hola\n1\n99\n3\nAtlantis\n3\nMontevideo\nAtlantis\n8\n0\n");
        assertTrue(out.contains("Opcion invalida."));
        assertTrue(out.contains("Escenario invalido."));
        assertTrue(out.contains("Ciudad inexistente: Atlantis"));
        assertTrue(out.contains("Escenario actual: 1"));
        assertTrue(out.endsWith("Hasta luego." + System.lineSeparator()));
    }

    public void testEofAtMenuAndInPromptsExitsCleanly() throws IOException {
        for (String input : List.of("", "3\n", "3\nMontevideo\n", "1\n")) {
            assertTrue(session(input).contains("Fin de entrada. Hasta luego."));
        }
    }

    public void testAllPathsCostsCenterAndSameCity() throws IOException {
        CargadorGrafo loader = new CargadorGrafo() {
            @Override public DirectedGraph<String, WeightedEdge> cargarRecursos(String a, String b) {
                return GraphFixtures.routes();
            }
        };
        String out = session("5\nA\nD\n4\n3\nA\nA\n6\n0\n", loader, new GraphAlgorithms());
        assertTrue(out.contains("Caminos simples encontrados: 2"));
        assertTrue(out.contains("A -> B -> C -> D | Costo: 3.0"));
        assertTrue(out.contains("A -> C -> D | Costo: 11.0"));
        assertTrue(out.contains("Centro de mantenimiento: D"));
        assertTrue(out.contains("Distancia minima: 0.0"));
        assertTrue(out.contains("Itinerario: A"));
        assertTrue(out.contains("El grafo no contiene ciclos."));
    }

    public void testFloydIsReusedUntilScenarioChanges() throws IOException {
        CountingAlgorithms algorithms = new CountingAlgorithms();
        String out = session("2\nMontevideo\nBuenos_Aires\n4\n3\nMontevideo\nBuenos_Aires\n1\n2\n4\n0\n",
                new CargadorGrafo(), algorithms);
        assertEquals(2, algorithms.calls);
        assertTrue(out.contains("Centro de mantenimiento:"));
        assertTrue(out.contains("No existe un centro accesible"));
    }

    public void testFailedLoadKeepsPreviousScenarioAndCachedDistances() throws IOException {
        CargadorGrafo loader = new CargadorGrafo() {
            @Override public DirectedGraph<String, WeightedEdge> cargarRecursos(String a, String b) throws IOException {
                if (a.contains("_2")) throw new IOException("archivo invalido de prueba");
                return super.cargarRecursos(a, b);
            }
        };
        CountingAlgorithms algorithms = new CountingAlgorithms();
        String out = session("1\n2\n2\nMontevideo\nBuenos_Aires\n0\n", loader, algorithms);
        assertTrue(out.contains("No se pudo cargar el escenario: archivo invalido de prueba"));
        assertTrue(out.contains("Se conserva el escenario 1."));
        assertTrue(out.contains("Distancia minima: 200.0"));
        assertEquals(1, algorithms.calls);
    }

    public void testInitialLoadFailureAllowsRetryAndPreventsQueries() throws IOException {
        CargadorGrafo loader = new CargadorGrafo() {
            int calls;
            @Override public DirectedGraph<String, WeightedEdge> cargarRecursos(String a, String b) throws IOException {
                if (++calls == 1) throw new IOException("recurso ausente de prueba");
                return super.cargarRecursos(a, b);
            }
        };
        String out = session("4\n1\n2\n6\n0\n", loader, new GraphAlgorithms());
        assertTrue(out.contains("No hay un escenario cargado."));
        assertTrue(out.contains("Escenario 2 cargado"));
        assertTrue(out.contains("El grafo contiene ciclos."));
    }

    public void testTaskDemoDoesNotReplaceFlightGraph() throws IOException {
        String out = session("7\n2\nMontevideo\nBuenos_Aires\n0\n");
        assertTrue(out.contains("Orden topologico:"));
        assertTrue(out.contains("Duracion del proyecto: 10.0 dias"));
        assertTrue(out.contains("Inicio -> Diseno -> Construccion -> Fin | Costo: 10.0"));
        assertTrue(out.contains("Holgura: 0.0 | CRITICA"));
        assertTrue(out.contains("Inicio -> Diseno -> Documentacion -> Fin | Costo: 5.0"));
        assertTrue(out.contains("Holgura: 5.0 | No critica"));
        assertTrue(out.contains("Distancia minima: 200.0"));
    }

    public void testDemoTopologicalOrderAndDuration() {
        var graph = ProyectoDemostracion.crear();
        var algorithms = new GraphAlgorithms();
        var order = algorithms.calcularClasificacionTopologica(graph);
        assertEquals(graph.vertices(), new HashSet<>(order));
        for (var edge : graph.aristas()) assertTrue(order.indexOf(edge.source()) < order.indexOf(edge.target()));
        assertEquals(10d, algorithms.calcularCaminoCritico(graph).getDuracion(), 0d);
    }

    private static class CountingAlgorithms extends GraphAlgorithms {
        int calls;
        @Override public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> graph) {
            calls++;
            return super.floyd(graph);
        }
    }
}
