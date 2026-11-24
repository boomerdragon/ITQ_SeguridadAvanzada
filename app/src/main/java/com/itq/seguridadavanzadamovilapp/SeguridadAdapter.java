package com.itq.seguridadavanzadamovilapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SeguridadAdapter extends RecyclerView.Adapter<SeguridadAdapter.SeguridadViewHolder> {

    private List<CredencialSeguraDto> lista = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setCredenciales(List<CredencialSeguraDto> nuevasCredenciales) {
        this.lista = nuevasCredenciales;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeguridadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seguridad, parent, false);
        return new SeguridadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeguridadViewHolder holder, int position) {
        CredencialSeguraDto dto = lista.get(position);
        Context context = holder.itemView.getContext();
        holder.tvAlias.setText(dto.getAliasServicio());
        holder.tvUsuario.setText(context.getString(R.string.fmt_usuario, dto.getUsuarioCifrado()));
        holder.tvToken.setText(context.getString(R.string.fmt_token, dto.getTokenCifrado()));
        holder.tvAlgoritmo.setText(context.getString(R.string.fmt_algoritmo, dto.getAlgoritmoUsado()));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class SeguridadViewHolder extends RecyclerView.ViewHolder {

        TextView tvAlias;
        TextView tvUsuario;
        TextView tvToken;
        TextView tvAlgoritmo;

        public SeguridadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlias = itemView.findViewById(R.id.tvAliasServicio);
            tvUsuario = itemView.findViewById(R.id.tvUsuarioCifrado);
            tvToken = itemView.findViewById(R.id.tvTokenCifrado);
            tvAlgoritmo = itemView.findViewById(R.id.tvAlgoritmoUsado);
        }
    }
}
