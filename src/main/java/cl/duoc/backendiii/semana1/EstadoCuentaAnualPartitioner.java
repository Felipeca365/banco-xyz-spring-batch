package cl.duoc.backendiii.semana1;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reparte el archivo cuentas_anuales.csv en rangos de filas para que
 * el Step del Job 3 los procese en paralelo, uno por partición.
 *
 * A diferencia del multi-threading de Job 1 y 2 (varios hilos
 * compitiendo por UN reader compartido con SynchronizedItemStreamReader),
 * acá cada partición tiene su PROPIO reader leyendo su propio rango,
 * sin necesidad de sincronización entre ellos.
 */
@Component
public class EstadoCuentaAnualPartitioner implements Partitioner {

    private final int totalRecords;

    public EstadoCuentaAnualPartitioner(
            @Value("${app.estado-anual.total-records}") int totalRecords) {
        this.totalRecords = totalRecords;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> particiones = new LinkedHashMap<>();

        int tamanoPorticion = (int) Math.ceil((double) totalRecords / gridSize);
        int inicio = 0;

        for (int i = 0; i < gridSize && inicio < totalRecords; i++) {
            int fin = Math.min(inicio + tamanoPorticion - 1, totalRecords - 1);

            ExecutionContext contexto = new ExecutionContext();
            contexto.putInt("start", inicio);
            contexto.putInt("end", fin);
            contexto.putString("partitionName", "particion" + i);

            particiones.put("particion" + i, contexto);

            inicio = fin + 1;
        }

        return particiones;
    }
}