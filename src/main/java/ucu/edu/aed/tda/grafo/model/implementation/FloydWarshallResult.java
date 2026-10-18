package ucu.edu.aed.tda.grafo.model.implementation;

import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FloydWarshallResult<V> implements IFloydWarshallResult<V> {
    public enum Representation { COSTS, REACHABILITY }

    public final Map<V, Map<V, Double>> dist;
    public final Map<V, Map<V, V>> next;
    private final Representation representation;

    public FloydWarshallResult(Map<V, Map<V, Double>> dist, Map<V, Map<V, V>> next) {
        this(dist, next, Representation.COSTS);
    }

    public FloydWarshallResult(Map<V, Map<V, Double>> dist, Map<V, Map<V, V>> next,
                              Representation representation) {
        this.dist = dist;
        this.next = next;
        this.representation = Objects.requireNonNull(representation);
    }

    public List<V> getPath(V u, V v) {
        if (!connected(u, v)) return List.of();
        List<V> p = new ArrayList<>();
        V at = u;
        while (!Objects.equals(at, v)) {
            // Las matrices son publicas: una cadena alterada no debe generar un bucle.
            if (p.size() >= dist.size()) {
                throw new IllegalStateException("Matriz de siguientes inconsistente");
            }
            p.add(at);
            Map<V, V> row = next.get(at);
            at = row == null ? null : row.get(v);
            if (at == null) {
                return List.of();
            }
        }
        p.add(v);
        return p;
    }

    public double getCost(V source, V target) {
        Map<V, Double> row = dist.get(source);
        return row == null ? Double.POSITIVE_INFINITY : row.getOrDefault(target, Double.POSITIVE_INFINITY);
    }

    public boolean connected(V source, V target) {
        Map<V, Double> row = dist.get(source);
        if (row == null) return false;
        Double v = row.get(target);
        if (v == null) return false;
        // En Floyd cero es un costo valido; en Warshall significa inaccesibilidad.
        return representation == Representation.REACHABILITY ? v == 1d : Double.isFinite(v);
    }
}
