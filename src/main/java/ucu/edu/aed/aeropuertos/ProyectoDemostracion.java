package ucu.edu.aed.aeropuertos;

import ucu.edu.aed.tda.grafo.classes.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

/** Ejemplo didactico independiente de los vuelos: duraciones en dias sobre aristas. */
public final class ProyectoDemostracion {
    private ProyectoDemostracion() { }

    public static DirectedGraph<String, WeightedEdge> crear() {
        var grafo = new DirectedGraph<String, WeightedEdge>();
        grafo.agregarArista("Inicio", "Diseno", new WeightedEdge(2));
        grafo.agregarArista("Diseno", "Construccion", new WeightedEdge(5));
        grafo.agregarArista("Construccion", "Fin", new WeightedEdge(3));
        grafo.agregarArista("Diseno", "Documentacion", new WeightedEdge(2));
        grafo.agregarArista("Documentacion", "Fin", new WeightedEdge(1));
        return grafo;
    }
}
