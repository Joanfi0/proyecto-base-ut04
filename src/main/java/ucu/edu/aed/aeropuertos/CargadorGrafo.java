package ucu.edu.aed.aeropuertos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

/** Carga los archivos de la aerolinea sin mezclar lectura con algoritmos del grafo. */
public class CargadorGrafo {
    public DirectedGraph<String, WeightedEdge> cargar(Path aeropuertos, Path conexiones) throws IOException {
        try (BufferedReader vertices = Files.newBufferedReader(aeropuertos, StandardCharsets.UTF_8);
             BufferedReader aristas = Files.newBufferedReader(conexiones, StandardCharsets.UTF_8)) {
            return cargar(vertices, aristas, aeropuertos.toString(), conexiones.toString());
        }
    }

    /** Nombres relativos al classpath, por ejemplo aeropuertos/aeropuertos.txt. */
    public DirectedGraph<String, WeightedEdge> cargarRecursos(String aeropuertos, String conexiones) throws IOException {
        try (BufferedReader vertices = abrirRecurso(aeropuertos);
             BufferedReader aristas = abrirRecurso(conexiones)) {
            return cargar(vertices, aristas, aeropuertos, conexiones);
        }
    }

    private BufferedReader abrirRecurso(String nombre) throws IOException {
        String ruta = nombre.startsWith("/") ? nombre : "/" + nombre;
        InputStream input = CargadorGrafo.class.getResourceAsStream(ruta);
        if (input == null) throw new IOException("No se encontro el recurso: " + nombre);
        return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    private DirectedGraph<String, WeightedEdge> cargar(BufferedReader vertices, BufferedReader aristas,
            String archivoVertices, String archivoAristas) throws IOException {
        // El grafo se devuelve solo si ambos archivos son validos.
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        String linea;
        int numero = 0;
        while ((linea = vertices.readLine()) != null) {
            linea = limpiar(linea, ++numero);
            if (linea.isEmpty()) continue;
            if (linea.contains(",")) throw error(archivoVertices, numero, "se esperaba un aeropuerto por linea");
            if (!grafo.agregarVertice(linea)) throw error(archivoVertices, numero, "aeropuerto duplicado: " + linea);
        }

        numero = 0;
        while ((linea = aristas.readLine()) != null) {
            linea = limpiar(linea, ++numero);
            if (linea.isEmpty()) continue;
            String[] campos = linea.split(",", -1);
            if (campos.length != 3) throw error(archivoAristas, numero, "se esperaba origen,destino,peso");
            String origen = campos[0].trim();
            String destino = campos[1].trim();
            String pesoTexto = campos[2].trim();
            if (origen.isEmpty() || destino.isEmpty() || pesoTexto.isEmpty()) {
                throw error(archivoAristas, numero, "hay campos vacios");
            }
            if (!grafo.existeVertice(grafo.construirComparable(origen)) ||
                    !grafo.existeVertice(grafo.construirComparable(destino))) {
                throw error(archivoAristas, numero, "aeropuerto no declarado: " + origen + " -> " + destino);
            }
            double peso;
            try {
                peso = Double.parseDouble(pesoTexto);
            } catch (NumberFormatException ex) {
                throw error(archivoAristas, numero, "peso no numerico: " + pesoTexto);
            }
            // Las distancias de vuelos deben poder usarse tambien con Dijkstra.
            if (!Double.isFinite(peso) || peso < 0) {
                throw error(archivoAristas, numero, "el peso debe ser finito y no negativo");
            }
            if (!grafo.agregarArista(origen, destino, new WeightedEdge(peso))) {
                throw error(archivoAristas, numero, "conexion duplicada: " + origen + " -> " + destino);
            }
        }
        return grafo;
    }

    private String limpiar(String linea, int numero) {
        if (numero == 1 && linea.startsWith("\uFEFF")) linea = linea.substring(1);
        return linea.trim();
    }

    private IOException error(String archivo, int linea, String detalle) {
        return new IOException(archivo + ": linea " + linea + ": " + detalle);
    }
}
