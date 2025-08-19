package com.alura.literalura;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	private final Scanner scanner = new Scanner(System.in);
	private static final String URL_BASE = "https://gutendex.com/books/?search=";

	@Autowired
	private LibroRepository libroRepository;

	@Autowired
	private AutorRepository autorRepository;

	private final ConsumoAPI consumoAPI = new ConsumoAPI();
	private final ConvierteDatos conversor = new ConvierteDatos();

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		int opcion = -1;
		while (opcion != 0) {
			mostrarMenu();
			try {
				System.out.print("Ingrese una opción: ");
				opcion = Integer.parseInt(scanner.nextLine());
				switch (opcion) {
					case 1 -> buscarLibroPorTitulo();
					case 2 -> listarLibrosRegistrados();
					case 3 -> listarAutoresRegistrados();
					case 4 -> listarAutoresVivosEnAnio();
					case 5 -> listarLibrosPorIdioma();
					case 6 -> top10LibrosMasDescargados();
					case 7 -> mostrarEstadisticas(); // ⬅ método ajustado
					case 0 -> System.out.println("Saliendo...");
					default -> System.out.println("Opción inválida.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Por favor ingrese un número válido.");
			} catch (Exception e) {
				System.out.println("Ocurrió un error: " + e.getMessage());
			}
		}
	}

	private void mostrarMenu() {
		System.out.println("""
                
                ================================================
                Elija la opción:
                1. Buscar libro por título
                2. Listar libros registrados
                3. Listar autores registrados
                4. Listar autores vivos en un año
                5. Listar libros por idioma
                6. Top 10 libros más descargados
                7. Estadísticas de descargas
                0. Salir
                ================================================
                """);
	}

	// 1 - Buscar libro por título (con confirmación para guardar)
	private void buscarLibroPorTitulo() {
		System.out.print("Título a buscar: ");
		String titulo = scanner.nextLine().trim();
		if (titulo.isEmpty()) {
			System.out.println("Debe ingresar un título.");
			return;
		}

		Optional<Libro> existente = libroRepository.findByTitulo(titulo);
		if (existente.isPresent()) {
			System.out.println("El libro ya está registrado:");
			System.out.println(existente.get());
			return;
		}

		String json = consumoAPI.obtenerDatos(URL_BASE + titulo.replace(" ", "+"));
		if (json == null || json.isBlank()) {
			System.out.println("No se pudo obtener respuesta de la API.");
			return;
		}

		Respuesta r = conversor.obtenerDatos(json, Respuesta.class);
		if (r == null || r.resultados() == null || r.resultados().isEmpty()) {
			System.out.println("No se encontraron resultados en Gutendex.");
			return;
		}

		DatosLibro datos = r.resultados().stream()
				.filter(dl -> dl.titulo() != null && dl.titulo().toLowerCase().contains(titulo.toLowerCase()))
				.findFirst()
				.orElse(r.resultados().get(0));

		System.out.println("\nResultado encontrado:");
		System.out.println("Título: " + datos.titulo());
		String idioma = (datos.idiomas() != null && !datos.idiomas().isEmpty()) ? datos.idiomas().get(0) : "desconocido";
		System.out.println("Idioma: " + idioma);
		System.out.println("Descargas: " + datos.numeroDescargas());
		String autorNombre = (datos.autores() != null && !datos.autores().isEmpty()) ? datos.autores().get(0).nombre() : "Autor desconocido";
		System.out.println("Autor: " + autorNombre);

		System.out.print("¿Desea guardarlo? (S/N): ");
		String resp = scanner.nextLine().trim();
		if (!resp.equalsIgnoreCase("S")) {
			System.out.println("No se guardó el libro.");
			return;
		}

		Autor autor = null;
		if (datos.autores() != null && !datos.autores().isEmpty()) {
			DatosAutor da = datos.autores().get(0);
			autor = autorRepository.findFirstByNombreIgnoreCase(da.nombre())
					.orElseGet(() -> autorRepository.save(new Autor(da)));
		}

		Libro libro = new Libro(datos, autor);
		libroRepository.save(libro);
		System.out.println("✅ Libro guardado correctamente.");
	}

	// 2 - Listar libros registrados
	private void listarLibrosRegistrados() {
		List<Libro> libros = libroRepository.findAll();
		if (libros.isEmpty()) {
			System.out.println("No hay libros registrados.");
			return;
		}
		libros.forEach(System.out::println);
	}

	// 3 - Listar autores registrados
	private void listarAutoresRegistrados() {
		List<Autor> autores = autorRepository.findAll();
		if (autores.isEmpty()) {
			System.out.println("No hay autores registrados.");
			return;
		}
		autores.forEach(System.out::println);
	}

	// 4 - Listar autores vivos en un año
	private void listarAutoresVivosEnAnio() {
		try {
			System.out.print("Ingrese el año: ");
			int anio = Integer.parseInt(scanner.nextLine());
			List<Autor> vivos = autorRepository.findAutoresVivosEnAnio(anio);
			if (vivos.isEmpty()) {
				System.out.println("No hay autores vivos en ese año.");
			} else {
				vivos.forEach(System.out::println);
			}
		} catch (NumberFormatException e) {
			System.out.println("Año inválido.");
		}
	}

	// 5 - Listar libros por idioma (y cantidad)
	private void listarLibrosPorIdioma() {
		System.out.print("Ingrese código de idioma (es, en, fr, pt, ...): ");
		String idioma = scanner.nextLine().trim().toLowerCase();
		List<Libro> libros = libroRepository.findByIdioma(idioma);
		long cantidad = libroRepository.countByIdioma(idioma);
		if (libros.isEmpty()) {
			System.out.println("No se encontraron libros en ese idioma.");
			return;
		}
		System.out.println("Cantidad de libros en '" + idioma + "': " + cantidad);
		libros.forEach(System.out::println);
	}

	// 6 - Top 10 por descargas
	private void top10LibrosMasDescargados() {
		List<Libro> top = libroRepository.findTop10ByOrderByNumeroDescargasDesc();
		if (top.isEmpty()) {
			System.out.println("No hay libros registrados.");
			return;
		}
		for (int i = 0; i < top.size(); i++) {
			Libro l = top.get(i);
			System.out.printf("%d) %s - %d descargas%n", i + 1, l.getTitulo(), l.getNumeroDescargas());
		}
	}

	// 7 - Estadísticas (máximo, mínimo, promedio)  ⬅ MÉTODO AJUSTADO
	private void mostrarEstadisticas() {
		long total = libroRepository.count();
		if (total == 0) {
			System.out.println("Aún no hay libros cargados. Guarda alguno (opción 1) y vuelve a intentar.");
			return;
		}

		List<Object[]> filas = libroRepository.obtenerEstadisticasDescargas();
		if (filas == null || filas.isEmpty()) {
			System.out.println("No hay datos suficientes para estadísticas.");
			return;
		}

		Object[] stats = filas.get(0);
		if (stats.length < 3) {
			System.out.println("No hay datos suficientes para estadísticas.");
			return;
		}

		Number max = (Number) stats[0];
		Number min = (Number) stats[1];
		Number avg = (Number) stats[2];

		System.out.println("\n=== ESTADÍSTICAS DE DESCARGAS ===");
		System.out.println("Máximo de descargas: " + (max != null ? max.longValue() : 0));
		System.out.println("Mínimo de descargas: " + (min != null ? min.longValue() : 0));
		System.out.printf("Promedio de descargas: %.2f%n", (avg != null ? avg.doubleValue() : 0.0));
	}
}