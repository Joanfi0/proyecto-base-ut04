package ucu.edu.aed.tda.grafo.model.implementation;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DijkstraResult<V> implements IDijkstraResult<V> {
    private final Map<V, Double> costos;
    private final Map<V, V> predecesores;

    public DijkstraResult(Map<V, Double> weights, Map<V, V> predecessors) {
        this.costos = weights;
        this.predecesores = predecessors;
    }

    @Override
    public double getCost(V otherVertex) {
        if (!costos.containsKey(otherVertex)) return Double.POSITIVE_INFINITY;
        return costos.get(otherVertex);
    }

    @Override
    public List<V> getPath(V otherVertex) {
        if (!Double.isFinite(getCost(otherVertex))) return List.of();
        List<V> camino = new ArrayList<V>();
        V actual = otherVertex;
        while (actual != null) {
            camino.add(actual);
            actual = predecesores.get(actual); // va al predecesor
        }
        Collections.reverse(camino);
        return camino;
    }
}
