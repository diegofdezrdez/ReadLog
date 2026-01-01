package com.example.readlog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "libros_db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_CREATE =
            "CREATE TABLE libros (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE libros ADD COLUMN favorito INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE libros ADD COLUMN estado TEXT DEFAULT 'pendiente'");
            db.execSQL("ALTER TABLE libros ADD COLUMN fecha_actualizacion INTEGER");
            // Actualizar estados basados en datos existentes
            db.execSQL("UPDATE libros SET estado = 'leido' WHERE leido = 1");
            db.execSQL("UPDATE libros SET estado = 'en_progreso' WHERE leido = 0 AND pagina_actual > 0");
            db.execSQL("UPDATE libros SET estado = 'pendiente' WHERE leido = 0 AND pagina_actual = 0");
        }
    }

    public void actualizarLibro(int id, String titulo, String autor, String notas, int leido, int paginaActual, int paginasTotales, int favorito, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues registro = new ContentValues();

        registro.put("titulo", titulo);
        registro.put("autor", autor);
        registro.put("notas", notas);
        registro.put("leido", leido);
        registro.put("pagina_actual", paginaActual);
        registro.put("paginas_totales", paginasTotales);
        registro.put("favorito", favorito);
        registro.put("estado", estado);
        registro.put("fecha_actualizacion", System.currentTimeMillis());

        db.update("libros", registro, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void eliminarLibro(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("libros", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- NUEVO MÉTODO PARA LAS ESTADÍSTICAS ---
    // Devuelve un array de 3 enteros: [Total Libros, Total Leídos, Total Páginas Leídas]
    public int[] obtenerEstadisticas() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] resultados = new int[3]; // Pos 0: Total, Pos 1: Leídos, Pos 2: Páginas

        // 1. Contar total de libros
        Cursor cTotal = db.rawQuery("SELECT count(*) FROM libros", null);
        if (cTotal.moveToFirst()) {
            resultados[0] = cTotal.getInt(0);
        }
        cTotal.close();

        // 2. Contar libros leídos
        Cursor cLeidos = db.rawQuery("SELECT count(*) FROM libros WHERE leido=1", null);
        if (cLeidos.moveToFirst()) {
            resultados[1] = cLeidos.getInt(0);
        }
        cLeidos.close();

        // 3. Calcular páginas leídas totales
        // Suma 'paginas_totales' si el libro está leído, o 'pagina_actual' si no lo está.
        // COALESCE es para que si devuelve null (sin libros), devuelva 0.
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