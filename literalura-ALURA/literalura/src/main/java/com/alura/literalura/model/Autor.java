package com.alura.literalura.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
        name = "autor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_autor_nombre_nacimiento", columnNames = {"nombre", "nacimiento"})
        }
)
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true) // puede ser null en Gutendex
    private String nombre;

    @Column(nullable = true) // puede venir null
    private Integer nacimiento;

    @Column(nullable = true) // puede venir null
    private Integer fallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Libro> libros;

    public Autor() {}

    public Autor(DatosAutor datos) {
        this.nombre = datos.nombre();
        this.nacimiento = datos.nacimiento();
        this.fallecimiento = datos.fallecimiento();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getNacimiento() { return nacimiento; }
    public Integer getFallecimiento() { return fallecimiento; }
    public List<Libro> getLibros() { return libros; }
    public void setLibros(List<Libro> libros) { this.libros = libros; }

    @Override
    public String toString() {
        return "Autor{" +
                "nombre='" + nombre + '\'' +
                ", nacimiento=" + (nacimiento != null ? nacimiento : "—") +
                ", fallecimiento=" + (fallecimiento != null ? fallecimiento : "—") +
                '}';
    }
}
