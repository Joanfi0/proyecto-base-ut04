# Carga de aeropuertos y conexiones

Los cuatro archivos se encuentran en `src/main/resources/aeropuertos`. Se movieron
desde `src/main/java/ucu/edu/aed/aeropuertos` sin alterar sus bytes (hashes comparados).
Maven los copia al classpath al compilar, incluso desde una compilación limpia.

```java
CargadorGrafo cargador = new CargadorGrafo();
DirectedGraph<String, WeightedEdge> grafo = cargador.cargarRecursos(
    "aeropuertos/aeropuertos.txt", "aeropuertos/conexiones.txt");

DirectedGraph<String, WeightedEdge> segundo = cargador.cargarRecursos(
    "aeropuertos/aeropuertos_2.txt", "aeropuertos/conexiones_2.txt");
```

Importar `ucu.edu.aed.aeropuertos.CargadorGrafo`, `DirectedGraph` del paquete
`ucu.edu.aed.tda.grafo.classes` y `WeightedEdge` del paquete
`ucu.edu.aed.tda.grafo.model.edge`. Los métodos declaran IOException, que debe
capturarse o declararse en el método llamador.

También se pueden cargar archivos externos usando rutas:

```java
var grafo = cargador.cargar(Path.of("aeropuertos.txt"), Path.of("conexiones.txt"));
```

`Path` pertenece a `java.nio.file`. Las rutas relativas dependen del directorio de
ejecución; los recursos del classpath no dependen de él.

## Formato y validaciones

- UTF-8, con BOM inicial opcional; se ignoran líneas en blanco.
- Aeropuertos: un nombre por línea. Se recortan espacios externos, conservando
  mayúsculas, acentos, espacios internos y guiones bajos. No se aceptan comas ni
  nombres duplicados.
- Conexiones: exactamente `origen,destino,peso`, con espacios externos opcionales.
  No es un lector CSV general: no interpreta campos entre comillas.
- Los dos aeropuertos deben estar declarados. Se cargan primero todos los vértices
  para conservar los aislados; no se crean aeropuertos por errores de escritura.
- Pesos numéricos finitos y no negativos; cero y decimales son válidos. Esta
  restricción corresponde a las distancias de la aerolínea y permite usar Dijkstra.
- Las conexiones son dirigidas. Se rechazan duplicados del mismo par, incluso si
  tienen distinto peso, para evitar que se descarte silenciosamente una línea.
- Un archivo mal formado produce IOException con archivo, número de línea y motivo.
  Un archivo o recurso ausente también produce IOException.
- Cada carga construye un grafo nuevo y solo lo devuelve al finalizar correctamente.
  Los lectores se cierran tanto al terminar como ante errores.

## Escenarios verificados

| Archivos | Vértices | Aristas | Ciclos |
| --- | --- | --- | --- |
| aeropuertos.txt + conexiones.txt | 8 | 24 | Sí |
| aeropuertos_2.txt + conexiones_2.txt | 9 | 15 | Sí |

Se preserva Montevideo -> San_Pablo con peso 2000 en el primer archivo, aunque la
tabla del PDF tenga otro valor. En el segundo escenario se verifica la línea con
espacios `Curitiba, Montevideo, 1400`.

Pruebas del cargador: `mvn -Dtest=CargadorGrafoTest test`. La aplicación de consola
se incorporó en la etapa 10; comandos de ejecución y menú en README.md.
