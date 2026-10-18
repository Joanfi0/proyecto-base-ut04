package ucu.edu.aed.aeropuertos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import ucu.edu.aed.tda.grafo.algorithms.GraphAlgorithms;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

/** Consola de consultas; el calculo permanece en las clases de algoritmos. */
public class AplicacionAerolinea {
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private final CargadorGrafo cargador;
    private final GraphAlgorithms algoritmos;
    private DirectedGraph<String, WeightedEdge> grafo;
    private IFloydWarshallResult<String> distancias;
    private String escenario;

    public AplicacionAerolinea(Reader entrada, Writer salida) {
        this(entrada, salida, new CargadorGrafo(), new GraphAlgorithms());
    }

    public AplicacionAerolinea(Reader entrada, Writer salida, CargadorGrafo cargador, GraphAlgorithms algoritmos) {
        this.entrada = new BufferedReader(entrada);
        this.salida = new PrintWriter(salida, true);
        this.cargador = cargador;
        this.algoritmos = algoritmos;
    }

    public static void main(String[] args) {
        var salida = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        var aplicacion = new AplicacionAerolinea(new InputStreamReader(System.in, StandardCharsets.UTF_8), salida);
        try {
            aplicacion.ejecutar();
        } catch (IOException error) {
            aplicacion.salida.println("No se pudo continuar leyendo la entrada: " + error.getMessage());
        }
    }

    public void ejecutar() throws IOException {
        salida.println("VUELE SEGURO - Consultas de vuelos");
        cargarEscenario("1");
        try {
            while (true) {
                mostrarMenu();
                String opcion = leer("Opcion: ");
                switch (opcion) {
                    case "0": salida.println("Hasta luego."); return;
                    case "1":
                        String elegido = leer("Escenario (1: ocho ciudades, 2: nueve ciudades): ");
                        if (!elegido.equals("1") && !elegido.equals("2")) salida.println("Escenario invalido. Use 1 o 2.");
                        else cargarEscenario(elegido);
                        break;
                    case "2": case "3": case "5":
                        if (hayGrafo()) consultarRuta(opcion);
                        break;
                    case "4": if (hayGrafo()) consultarCentro(); break;
                    case "6":
                        if (hayGrafo()) salida.println(grafo.tieneCiclos() ? "El grafo contiene ciclos." : "El grafo no contiene ciclos.");
                        break;
                    case "7": demostrarProyecto(); break;
                    case "8": if (hayGrafo()) mostrarCiudades(); break;
                    default: salida.println("Opcion invalida. Use un numero del 0 al 8.");
                }
            }
        } catch (EOFException fin) {
            salida.println("Fin de entrada. Hasta luego.");
        }
    }

    private void cargarEscenario(String elegido) {
        String sufijo = elegido.equals("2") ? "_2" : "";
        try {
            var nuevo = cargador.cargarRecursos("aeropuertos/aeropuertos" + sufijo + ".txt",
                    "aeropuertos/conexiones" + sufijo + ".txt");
            var nuevasDistancias = algoritmos.floyd(nuevo);
            // Reemplazo atomico: si la carga falla se conserva el escenario anterior.
            grafo = nuevo;
            distancias = nuevasDistancias;
            escenario = elegido;
            salida.println("Escenario " + elegido + " cargado: " + grafo.cantidadDeVertices() +
                    " aeropuertos, " + grafo.cantidadDeAristas() + " conexiones.");
            mostrarCiudades();
        } catch (IOException | IllegalArgumentException error) {
            salida.println("No se pudo cargar el escenario: " + error.getMessage());
            if (grafo != null) salida.println("Se conserva el escenario " + escenario + ".");
        }
    }

    private boolean hayGrafo() {
        if (grafo != null) return true;
        salida.println("No hay un escenario cargado. Use la opcion 1.");
        return false;
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("Escenario actual: " + (escenario == null ? "ninguno" : escenario));
        salida.println("1. Cambiar escenario");
        salida.println("2. Distancia minima entre ciudades");
        salida.println("3. Itinerario de menor costo");
        salida.println("4. Centro de mantenimiento");
        salida.println("5. Todos los caminos simples y sus costos");
        salida.println("6. Detectar ciclos");
        salida.println("7. Demostracion de tareas: orden topologico y camino critico");
        salida.println("8. Listar ciudades");
        salida.println("0. Salir");
    }

    private void mostrarCiudades() {
        List<String> ciudades = new ArrayList<>(grafo.vertices());
        Collections.sort(ciudades);
        salida.println("Ciudades: " + String.join(", ", ciudades));
        salida.println("Puede usar espacios o guiones bajos, sin distinguir mayusculas.");
    }

    private String leer(String mensaje) throws IOException {
        salida.print(mensaje);
        salida.flush();
        String linea = entrada.readLine();
        if (linea == null) throw new EOFException();
        return linea.trim();
    }

    private String leerCiudad(String mensaje) throws IOException {
        String nombre = leer(mensaje).replaceAll("\\s+", "_");
        for (String ciudad : grafo.vertices()) {
            if (ciudad.equalsIgnoreCase(nombre)) return ciudad;
        }
        salida.println("Ciudad inexistente: " + (nombre.isEmpty() ? "(vacia)" : nombre));
        return null;
    }

    private void consultarRuta(String opcion) throws IOException {
        String origen = leerCiudad("Origen: ");
        if (origen == null) return;
        String destino = leerCiudad("Destino: ");
        if (destino == null) return;
        if (!distancias.connected(origen, destino)) {
            salida.println("No hay camino de " + origen + " a " + destino + ".");
            return;
        }
        if (opcion.equals("5")) {
            var caminos = algoritmos.obtenerTodosLosCaminos(grafo.construirComparable(origen),
                    grafo.construirComparable(destino), grafo);
            caminos.sort(Comparator.comparing(p -> String.join(" -> ", p.getPath())));
            salida.println("Caminos simples encontrados: " + caminos.size());
            for (var camino : caminos) imprimirCamino(camino);
        } else {
            salida.println("Distancia minima: " + distancias.getCost(origen, destino));
            if (opcion.equals("3")) salida.println("Itinerario: " + String.join(" -> ", distancias.getPath(origen, destino)));
        }
    }

    private void consultarCentro() {
        String centro = algoritmos.obtenerCentroGrafo(grafo, distancias);
        if (centro == null) {
            salida.println("No existe un centro accesible desde todos los aeropuertos.");
        } else {
            salida.println("Centro de mantenimiento: " + centro);
        }
    }

    private void imprimirCamino(Path<String> camino) {
        salida.println(String.join(" -> ", camino.getPath()) + " | Costo: " + camino.getCost());
    }

    private void demostrarProyecto() {
        var proyecto = ProyectoDemostracion.crear();
        salida.println("Proyecto de ejemplo independiente de los vuelos. Duraciones en aristas, en dias.");
        List<String> aristas = new ArrayList<>();
        for (var arista : proyecto.aristas()) aristas.add(arista.source() + " -> " + arista.target() +
                " : " + arista.dato().getWeight());
        Collections.sort(aristas);
        aristas.forEach(salida::println);
        salida.println("Orden topologico: " + String.join(" -> ", algoritmos.calcularClasificacionTopologica(proyecto)));
        var resultado = algoritmos.calcularCaminoCritico(proyecto);
        salida.println("Duracion del proyecto: " + resultado.getDuracion() + " dias");
        salida.println("Holguras por secuencia (no por tarea):");
        for (var secuencia : resultado.getSecuencias()) {
            imprimirCamino(secuencia.getCamino());
            salida.println("Holgura: " + secuencia.getHolgura() + (secuencia.esCritica() ? " | CRITICA" : " | No critica"));
        }
    }
}
