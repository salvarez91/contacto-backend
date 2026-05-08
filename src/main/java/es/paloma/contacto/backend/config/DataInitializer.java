package es.paloma.contacto.backend.config;

import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.repository.InteresRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(InteresRepository repository) {
        return args -> {
            List<String> interesesOficiales = List.of(
                    "📚 Lectura y Libros",
                    "🎬 Cine y Series Clásicas",
                    "🧶 Manualidades y Ganchillo",
                    "🎵 Música",
                    "🌱 Jardinería y Plantas",
                    "🍳 Cocina y Recetas",
                    "📺 Programas de TV"
            );

            for (String nombre : interesesOficiales) {
                if (repository.findByNombre(nombre).isEmpty()) {
                    Interes nuevo = new Interes();
                    nuevo.setNombre(nombre);
                    repository.save(nuevo);
                }
            }
        };
    }
}