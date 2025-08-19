package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    // Devuelve el primer autor cuyo nombre coincida (ignora mayúsculas/minúsculas).
    Optional<Autor> findFirstByNombreIgnoreCase(String nombre);

    List<Autor> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT a FROM Autor a " +
            "WHERE (a.nacimiento IS NULL OR a.nacimiento <= :anio) " +
            "AND   (a.fallecimiento IS NULL OR a.fallecimiento >= :anio)")
    List<Autor> findAutoresVivosEnAnio(int anio);
}