package com.example.readlog;

import com.google.firebase.firestore.Exclude;

public class Libro {
    private String id;
    private String titulo;
    private String autor;
    private String notas;
    private int leido;
    private int pagActual;
    private int pagTotales;
    private int favorito;
    private String estado;
    private long fechaActualizacion;

    // Constructor vacío requerido por Firestore
    public Libro() {}

    public Libro(String id, String titulo, String autor, String notas, int leido, int pagActual, int pagTotales, int favorito, String estado, long fechaActualizacion) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.notas = notas;
        this.leido = leido;
        this.pagActual = pagActual;
        this.pagTotales = pagTotales;
        this.favorito = favorito;
        this.estado = estado;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public int getLeido() { return leido; }
    public void setLeido(int leido) { this.leido = leido; }

    public int getPagActual() { return pagActual; }
    public void setPagActual(int pagActual) { this.pagActual = pagActual; }

    public int getPagTotales() { return pagTotales; }
    public void setPagTotales(int pagTotales) { this.pagTotales = pagTotales; }

    public int getFavorito() { return favorito; }
    public void setFavorito(int favorito) { this.favorito = favorito; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
