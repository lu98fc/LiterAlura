package com.alura.literalura.model;

import jakarta.persistence.*;

@Entity
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String idioma;

    // Opcional pero recomendado: forzar NOT NULL en la BD.
    @Column(name = "numero_descargas", nullable = false)
    private Integer numeroDescargas;

    @ManyToOne
    private Autor autor;

    public Libro() {}

    public Libro(DatosLibro datos, Autor autor) {
        this.titulo = datos.titulo();
        this.idioma = (datos.idiomas() != null && !datos.idiomas().isEmpty())
                ? datos.idiomas().get(0)
                : "desconocido";
        // ⬇️ Clave: si Gutendex no trae download_count, guardamos 0
        this.numeroDescargas = (datos.numeroDescargas() != null) ? datos.numeroDescargas() : 0;
        this.autor = autor;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getIdioma() { return idioma; }
    public Integer getNumeroDescargas() { return numeroDescargas; }
    public Autor getAutor() { return autor; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public void setNumeroDescargas(Integer numeroDescargas) { this.numeroDescargas = numeroDescargas; }
    public void setAutor(Autor autor) { this.autor = autor; }

    @Override
    public String toString() {
        return "Libro{" +
                "titulo='" + titulo + '\'' +
                ", idioma='" + idioma + '\'' +
                ", numeroDescargas=" + numeroDescargas +
                ", autor=" + (autor != null ? autor.getNombre() : "N/A") +
                '}';
    }
}
