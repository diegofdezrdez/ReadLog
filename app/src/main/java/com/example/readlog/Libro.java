package com.example.readlog;

public class Libro {
    private final int id;
    private final String titulo;
    private final String autor;
    private final String notas;
    private final int leido;
    private final int pagActual;
    private final int pagTotales;
    private final int favorito;
    private final String estado;

    public Libro(int id, String titulo, String autor, String notas, int leido, int pagActual, int pagTotales, int favorito, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.notas = notas;
        this.leido = leido;
        this.pagActual = pagActual;
        this.pagTotales = pagTotales;
        this.favorito = favorito;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getNotas() { return notas; }
    public int getLeido() { return leido; }
    public int getPagActual() { return pagActual; }
    public int getPagTotales() { return pagTotales; }
    public int getFavorito() { return favorito; }
    public String getEstado() { return estado; }
}