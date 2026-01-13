package com.example.readlog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "libros_db";
    // CAMBIO: Incrementar la versión para forzar la ejecución de onCreate en las instalaciones existentes
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_NAME = "libros";
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id TEXT PRIMARY KEY, " +
                    "titulo TEXT, " +
                    "autor TEXT, " +
                    "notas TEXT, " +
                    "leido INTEGER, " +
                    "pagina_actual INTEGER, " +
                    "paginas_totales INTEGER, " +
                    "favorito INTEGER DEFAULT 0, " +
                    "estado TEXT DEFAULT 'pendiente', " +
                    "fecha_actualizacion INTEGER)";

    public AdminSQLiteOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        // CAMBIO: Poblar la base de datos con datos de ejemplo correctos
        populateInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si hay una actualización de esquema, borramos la tabla y la creamos de nuevo.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void populateInitialData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        insertBook(db, "Un mundo feliz", "Aldous Huxley", "Lectura obligatoria", 1, 256, 256, 1, "leido", now);
        insertBook(db, "Rebelión en la granja", "George Orwell", "Sátira", 1, 144, 144, 0, "leido", now);
        insertBook(db, "La divina comedia", "Dante Alighieri", "Clásico", 0, 0, 720, 0, "pendiente", now);
        insertBook(db, "Rayuela", "Julio Cortázar", "Contra-novela", 0, 300, 600, 1, "en_progreso", now);
        insertBook(db, "El camino de los reyes", "Brandon Sanderson", "Fantasía épica", 0, 120, 1200, 0, "en_progreso", now);
        insertBook(db, "1984", "George Orwell", "Distopía", 0, 0, 328, 0, "pendiente", now);
        insertBook(db, "El principito", "Antoine de Saint-Exupéry", "Filosofía", 1, 96, 96, 0, "leido", now);
    }

    private void insertBook(SQLiteDatabase db, String titulo, String autor, String notas, int leido, int pagActual, int pagTotales, int favorito, String estado, long fecha) {
        ContentValues registro = new ContentValues();
        registro.put("id", UUID.randomUUID().toString()); // ID de tipo TEXT
        registro.put("titulo", titulo);
        registro.put("autor", autor);
        registro.put("notas", notas);
        registro.put("leido", leido);
        registro.put("pagina_actual", pagActual);
        registro.put("paginas_totales", pagTotales);
        registro.put("favorito", favorito);
        registro.put("estado", estado);
        registro.put("fecha_actualizacion", fecha);
        db.insert(TABLE_NAME, null, registro);
    }

    // ... El resto de los métodos (getAllBooks, actualizarLibro, etc.) permanecen igual ...

    public List<Libro> getAllBooks() {
        List<Libro> libros = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (fila.moveToFirst()) {
            int idIndex = fila.getColumnIndex("id");
            int tituloIndex = fila.getColumnIndex("titulo");
            int autorIndex = fila.getColumnIndex("autor");
            int notasIndex = fila.getColumnIndex("notas");
            int leidoIndex = fila.getColumnIndex("leido");
            int pagActualIndex = fila.getColumnIndex("pagina_actual");
            int pagTotalesIndex = fila.getColumnIndex("paginas_totales");
            int favoritoIndex = fila.getColumnIndex("favorito");
            int estadoIndex = fila.getColumnIndex("estado");
            int fechaIndex = fila.getColumnIndex("fecha_actualizacion");

            do {
                // Asegurarse de que los índices no sean -1
                if (idIndex != -1 && tituloIndex != -1 && autorIndex != -1 && notasIndex != -1 && leidoIndex != -1 && pagActualIndex != -1 && pagTotalesIndex != -1 && favoritoIndex != -1 && estadoIndex != -1 && fechaIndex != -1) {
                    libros.add(new Libro(
                            fila.getString(idIndex),
                            fila.getString(tituloIndex),
                            fila.getString(autorIndex),
                            fila.getString(notasIndex),
                            fila.getInt(leidoIndex),
                            fila.getInt(pagActualIndex),
                            fila.getInt(pagTotalesIndex),
                            fila.getInt(favoritoIndex),
                            fila.getString(estadoIndex),
                            fila.getLong(fechaIndex)
                    ));
                }
            } while (fila.moveToNext());
        }
        fila.close();
        db.close();
        return libros;
    }

    public void addBooks(List<Libro> libros) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Libro libro : libros) {
                ContentValues registro = new ContentValues();
                registro.put("id", libro.getId());
                registro.put("titulo", libro.getTitulo());
                registro.put("autor", libro.getAutor());
                registro.put("notas", libro.getNotas());
                registro.put("leido", libro.getLeido());
                registro.put("pagina_actual", libro.getPagActual());
                registro.put("paginas_totales", libro.getPagTotales());
                registro.put("favorito", libro.getFavorito());
                registro.put("estado", libro.getEstado());
                registro.put("fecha_actualizacion", libro.getFechaActualizacion());
                db.insertWithOnConflict(TABLE_NAME, null, registro, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void clearBooks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public void actualizarLibro(String id, String titulo, String autor, String notas, int leido, int paginaActual, int paginasTotales, int favorito, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues registro = new ContentValues();
        
        String finalId = (id == null) ? UUID.randomUUID().toString() : id;

        registro.put("id", finalId);
        registro.put("titulo", titulo);
        registro.put("autor", autor);
        registro.put("notas", notas);
        registro.put("leido", leido);
        registro.put("pagina_actual", paginaActual);
        registro.put("paginas_totales", paginasTotales);
        registro.put("favorito", favorito);
        registro.put("estado", estado);
        registro.put("fecha_actualizacion", System.currentTimeMillis());

        db.replace(TABLE_NAME, null, registro);
        db.close();
    }

    public void eliminarLibro(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id=?", new String[]{id});
        db.close();
    }

    public int[] obtenerEstadisticas() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] resultados = new int[3];

        Cursor cTotal = db.rawQuery("SELECT count(*) FROM libros", null);
        if (cTotal.moveToFirst()) {
            resultados[0] = cTotal.getInt(0);
        }
        cTotal.close();

        Cursor cLeidos = db.rawQuery("SELECT count(*) FROM libros WHERE leido=1", null);
        if (cLeidos.moveToFirst()) {
            resultados[1] = cLeidos.getInt(0);
        }
        cLeidos.close();

        String sqlPaginas = "SELECT COALESCE(SUM(CASE WHEN leido = 1 THEN paginas_totales ELSE pagina_actual END), 0) FROM libros";
        Cursor cPaginas = db.rawQuery(sqlPaginas, null);
        if (cPaginas.moveToFirst()) {
            resultados[2] = cPaginas.getInt(0);
        }
        cPaginas.close();

        db.close();
        return resultados;
    }
}
