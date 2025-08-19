package com.alura.literalura.repository;

import com.alura.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByTitulo(String titulo);

    List<Libro> findByIdioma(String idioma);

    Long countByIdioma(String idioma);

    List<Libro> findTop10ByOrderByNumeroDescargasDesc();

    // Devuelve SIEMPRE una fila (aunque con 0s). Evita nulls.
    @Query("""
           SELECT 
             COALESCE(MAX(l.numeroDescargas), 0),
             COALESCE(MIN(l.numeroDescargas), 0),
             COALESCE(AVG(l.numeroDescargas), 0)
           FROM Libro l
           """)
    List<Object[]> obtenerEstadisticasDescargas();
}