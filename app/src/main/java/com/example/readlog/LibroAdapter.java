package com.example.readlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private List<Libro> libros;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Libro libro);
    }

    public LibroAdapter(List<Libro> libros, Context context, OnItemClickListener listener) {
        this.libros = libros;
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLibros(List<Libro> libros) {
        this.libros = libros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro, parent, false);
        return new LibroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro libro = libros.get(position);
        holder.bind(libro, context, listener);
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        TextView tvAutor;
        TextView tvEstado;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloLibro);
            tvAutor = itemView.findViewById(R.id.tvAutorLibro);
            tvEstado = itemView.findViewById(R.id.tvEstadoLibro);
        }

        public void bind(final Libro libro, Context context, final OnItemClickListener listener) {
            // Añadir estrella si es favorito
            String tituloConEstrella = libro.getFavorito() == 1 ? "⭐ " + libro.getTitulo() : libro.getTitulo();
            tvTitulo.setText(tituloConEstrella);
            tvAutor.setText(libro.getAutor());

            // Mostrar estado según el campo estado
            String estado = libro.getEstado();
            int pagTotales = libro.getPagTotales();
            int pagActual = libro.getPagActual();

            if (estado != null && estado.equals("leido")) {
                tvEstado.setText(R.string.status_read);
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.stats_read_text));
                tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.stats_read_bg));
            } else if (estado != null && estado.equals("en_progreso")) {
                int porcentaje = 0;
                if (pagTotales > 0 && pagActual > 0) {
                    porcentaje = (pagActual * 100) / pagTotales;
                }
                tvEstado.setText(context.getString(R.string.status_in_progress, porcentaje));
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.stats_pages_text));
                tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.stats_pages_bg));
            } else {
                tvEstado.setText(R.string.status_pending);
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.stats_pending_text));
                tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.stats_pending_bg));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(libro));
        }
    }
}