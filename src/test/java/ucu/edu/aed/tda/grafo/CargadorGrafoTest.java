package ucu.edu.aed.tda.grafo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import junit.framework.TestCase;
import ucu.edu.aed.aeropuertos.CargadorGrafo;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

public class CargadorGrafoTest extends TestCase {
    private Path directory;
    private Path airports;
    private Path connections;
    private final CargadorGrafo loader = new CargadorGrafo();

    @Override protected void setUp() throws IOException {
        directory = Files.createTempDirectory("ut04-loader-");
        airports = directory.resolve("aeropuertos.txt");
        connections = directory.resolve("conexiones.txt");
    }

    @Override protected void tearDown() throws IOException {
        Files.deleteIfExists(airports);
        Files.deleteIfExists(connections);
        Files.deleteIfExists(directory);
    }

    private DirectedGraph<String, WeightedEdge> load(String vertices, String edges) throws IOException {
        Files.writeString(airports, vertices, StandardCharsets.UTF_8);
        Files.writeString(connections, edges, StandardCharsets.UTF_8);
        return loader.cargar(airports, connections);
    }

    public void testFirstProvidedScenario() throws IOException {
        var graph = loader.cargarRecursos("aeropuertos/aeropuertos.txt", "aeropuertos/conexiones.txt");
        assertEquals(8, graph.cantidadDeVertices());
        assertEquals(24, graph.cantidadDeAristas());
        assertEquals(2000d, graph.obtenerArista(graph.construirComparable("Montevideo"),
                graph.construirComparable("San_Pablo")).dato().getWeight(), 0d);
        assertTrue(graph.tieneCiclos());
        assertEquals(200d, new GraphAlgorithms().floyd(graph).getCost("Montevideo", "Buenos_Aires"), 0d);
    }

    public void testSecondProvidedScenarioAndSpaces() throws IOException {
        var graph = loader.cargarRecursos("/aeropuertos/aeropuertos_2.txt", "/aeropuertos/conexiones_2.txt");
        assertEquals(9, graph.cantidadDeVertices());
        assertEquals(15, graph.cantidadDeAristas());
        assertEquals(1400d, graph.obtenerArista(graph.construirComparable("Curitiba"),
                graph.construirComparable("Montevideo")).dato().getWeight(), 0d);
        assertTrue(graph.tieneCiclos());
    }

    public void testUtf8BomBlankLinesIsolatedVerticesAndDirection() throws IOException {
        var graph = load("\uFEFF  Colón \r\n\r\n A \nAislado\n", "\uFEFF\n Colón , A , 0.5 \n");
        assertEquals(3, graph.cantidadDeVertices());
        assertEquals(1, graph.cantidadDeAristas());
        assertEquals(0.5d, graph.obtenerArista(graph.construirComparable("Colón"), graph.construirComparable("A")).dato().getWeight(), 0d);
        assertFalse(graph.existeArista(graph.construirComparable("A"), graph.construirComparable("Colón")));
        assertTrue(graph.existeVertice(graph.construirComparable("Aislado")));
    }

    public void testZeroWeightAndEmptyFilesAreValid() throws IOException {
        assertEquals(0, load("\n", "\n").cantidadDeVertices());
        assertEquals(1, load("A\nB", "A,B,0").cantidadDeAristas());
    }

    public void testRejectsInvalidConnectionFieldsWithFileAndLine() throws IOException {
        for (String line : new String[]{"A,B", "A,B,1,extra", ",B,1", "A,,1", "A,B,"}) {
            assertInvalid("A\nB", "\n" + line, "conexiones.txt", 2);
        }
    }

    public void testRejectsUnknownAirportsAndDuplicateData() throws IOException {
        assertInvalid("A\nB", "A,Z,1", "conexiones.txt", 1);
        assertInvalid("A\nB", "Z,A,1", "conexiones.txt", 1);
        assertInvalid("A\n A ", "", "aeropuertos.txt", 2);
        assertInvalid("A\nB", "A,B,1\nA,B,2", "conexiones.txt", 2);
        assertInvalid("A,B", "", "aeropuertos.txt", 1);
    }

    public void testRejectsNonNumericNegativeAndNonFiniteWeights() throws IOException {
        for (String weight : new String[]{"abc", "-1", "NaN", "Infinity", "-Infinity", "1e999"}) {
            assertInvalid("A\nB", "A,B," + weight, "conexiones.txt", 1);
        }
    }

    public void testMissingFilesAndResourcesProduceIOException() throws IOException {
        try {
            loader.cargar(airports, connections);
            fail("Falta el archivo");
        } catch (IOException expected) { assertNotNull(expected.getMessage()); }
        for (String[] names : new String[][]{{"missing.txt", "aeropuertos/conexiones.txt"},
                {"aeropuertos/aeropuertos.txt", "missing.txt"}}) {
            try {
                loader.cargarRecursos(names[0], names[1]);
                fail("Falta el recurso");
            } catch (IOException expected) { assertTrue(expected.getMessage().contains("missing.txt")); }
        }
    }

    public void testLoadsAreIndependentAfterAnError() throws IOException {
        var first = load("A\nB", "A,B,1");
        assertInvalid("X\nY", "X,Y,1\nY,Z,2", "conexiones.txt", 2);
        var second = load("C", "");
        assertEquals(2, first.cantidadDeVertices());
        assertEquals(1, first.cantidadDeAristas());
        assertEquals(1, second.cantidadDeVertices());
        assertFalse(second.existeVertice(second.construirComparable("X")));
    }

    private void assertInvalid(String vertices, String edges, String file, int line) throws IOException {
        try {
            load(vertices, edges);
            fail("Se esperaba rechazar los datos");
        } catch (IOException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(file + ": linea " + line + ":"));
        }
    }
}
