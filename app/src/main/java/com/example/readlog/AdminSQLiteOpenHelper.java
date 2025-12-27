package com.example.readlog;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "libros_db";
    private static final int DATABASE_VERSION = 1;

    // AÑADIDOS: leido (INTEGER 1/0) y pagina_actual (INTEGER)
    private static final String TABLE_CREATE =
            "CREATE TABLE libros (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "titulo TEXT, " +
                    "autor TEXT, " +
                    "descripcion TEXT, " +
                    "leido INTEGER, " +
                    "pagina_actual INTEGER)";

    public AdminSQLiteOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS libros");
        onCreate(db);
    }

    public void actualizarLibro(int id, String titulo, String autor, String descripcion, int leido, int paginaActual) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues registro = new ContentValues();

        registro.put("titulo", titulo);
        registro.put("autor", autor);
        registro.put("descripcion", descripcion);
        registro.put("leido", leido);
        registro.put("pagina_actual", paginaActual);

        db.update("libros", registro, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void eliminarLibro(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // "DELETE FROM libros WHERE id = (el numero)"
        db.delete("libros", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}