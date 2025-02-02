package consulerdos.consumer_alerts_dos;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class ConsumerAlertsDosApplication {

	private final static String QUEUE_NAME = "myQueueJSON"; // Nombre de la cola
	private final ObjectMapper objectMapper = new ObjectMapper();  // Instancia de Jackson para convertir a JSON

	public static void main(String[] args) {
		SpringApplication.run(ConsumerAlertsDosApplication.class, args);
	}

	@RabbitListener(queues = QUEUE_NAME)

	public void receiveMessage(String message) {
		try {
			// Crear una ruta con la fecha actual: YYYY/MM/DD
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = dateFormat.format(new Date());  // Generar la fecha con el formato deseado

			// Crear el directorio completo en base a la fecha dentro de src\JSON
            File dir = new File("src/JSON/" + datePath);
            if (!dir.exists()) {
                dir.mkdirs();  // Si no existe, crear el directorio
            }

			// Crear el nombre del archivo como "Correlativo01.json"
            int correlativo = getNextCorrelativo(dir);  // Obtener el siguiente número disponible
            String filename = String.format("Correlativo%02d.json", correlativo);  // Ejemplo: Correlativo01.json

			// Guardar el archivo JSON en la ruta completa
            File file = new File(dir, filename);
            objectMapper.writeValue(file, message);  // Escribir el mensaje como JSON en el archivo

            // Mensajes de log
            System.out.println(" [x] Received '" + message + "'");
            System.out.println(" [x] Message saved as JSON: " + file.getAbsolutePath());
			
		} catch (Exception e) {
			System.err.println("Error al guardar el archivo JSON: " + e.getMessage());
		}
       
    }

	// Método para obtener el siguiente correlativo (número secuencial) basado en los archivos existentes
    private int getNextCorrelativo(File dir) {
        File[] files = dir.listFiles((d, name) -> name.matches("Correlativo\\d{2}.json"));
        int maxCorrelativo = 0;

        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                try {
                    // Extraer el número correlativo del nombre del archivo (ej. Correlativo01.json -> 01)
                    String numberPart = filename.replace("Correlativo", "").replace(".json", "");
                    int correlativo = Integer.parseInt(numberPart);
                    maxCorrelativo = Math.max(maxCorrelativo, correlativo);
                } catch (NumberFormatException e) {
                    // Si no se puede parsear el número, simplemente lo ignoramos
                    continue;
                }
            }
        }

        // El siguiente correlativo será el máximo encontrado + 1
        return maxCorrelativo + 1;
    }

}
