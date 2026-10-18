package ucu.edu.aed.tda.grafo.model.result;

import java.util.ArrayList;
import java.util.List;

/** Duracion del proyecto y sus secuencias completas, con holgura por camino. */
public final class ResultadoCaminoCritico<V> {
    private final double duracion;
    private final List<Secuencia<V>> secuencias;

    public ResultadoCaminoCritico(double duracion, List<Path<V>> caminos) {
        this.duracion = duracion;
        List<Secuencia<V>> resultado = new ArrayList<>();
        for (Path<V> camino : caminos) {
            double holgura = duracion - camino.getCost();
            // Evita distinguir empates que solo difieren por redondeo de double.
            double tolerancia = 1e-9 * Math.max(Math.abs(duracion), Math.abs(camino.getCost()));
            if (Math.abs(holgura) <= tolerancia) holgura = 0;
            resultado.add(new Secuencia<>(new Path<>(List.copyOf(camino.getPath()), camino.getCost()), holgura));
        }
        this.secuencias = List.copyOf(resultado);
    }

    public double getDuracion() { return duracion; }
    public List<Secuencia<V>> getSecuencias() { return secuencias; }

    public List<Path<V>> getCaminosCriticos() {
        List<Path<V>> criticos = new ArrayList<>();
        for (Secuencia<V> secuencia : secuencias) {
            if (secuencia.esCritica()) criticos.add(secuencia.getCamino());
        }
        return List.copyOf(criticos);
    }

    public static final class Secuencia<V> {
        private final Path<V> camino;
        private final double holgura;

        private Secuencia(Path<V> camino, double holgura) {
            this.camino = camino;
            this.holgura = holgura;
        }

        public Path<V> getCamino() { return camino; }
        public double getHolgura() { return holgura; }
        public boolean esCritica() { return holgura == 0; }
    }
}
