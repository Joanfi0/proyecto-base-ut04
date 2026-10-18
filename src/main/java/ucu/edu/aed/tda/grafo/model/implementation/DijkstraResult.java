package ucu.edu.aed.tda.grafo.model.implementacion;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DijkstraResult<V> implements IDijkstraResult<V> {
    private Map<V, Double> costos;
    private Map<V, V> predecesores ;

    public DijkstraResult(Map<V, Double> weights, Map<V, V> predecessors) {
        this.costos = weights;
        this.predecesores = predecessors;
    }

    @Override
    public double getCost(V otherVertex) {
        if (!costos.containsKey(otherVertex)) return Double.POSITIVE_INFINITY;
        return costos.get(otherVertex);    }

    @Override
    public List<V> getPath(V otherVertex) {
        List<V> camino = new ArrayList<V>();
        V actual = otherVertex;
        while (predecesores.containsKey(actual)) {
            camino.add(actual);
            actual = predecesores.get(actual); // va al predecesor
        }
        camino.add(actual);
        Collections.reverse(camino);
        return camino;
    }
}