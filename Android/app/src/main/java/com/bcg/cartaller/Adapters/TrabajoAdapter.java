package com.bcg.cartaller.Adapters;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bcg.cartaller.DetalleTrabajoActivity;
import com.bcg.cartaller.JobsNewFragment;
import com.bcg.cartaller.R;
import com.bcg.cartaller.Models.Trabajo;
import java.util.List;

public class TrabajoAdapter extends RecyclerView.Adapter<TrabajoAdapter.TrabajoViewHolder> {

    private final List<Trabajo> trabajos;

    public TrabajoAdapter(List<Trabajo> trabajos) {
        this.trabajos = trabajos;
    }

    public class TrabajoViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtEstado, txtMatricula, txtDni;

        public TrabajoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtTrabajoId);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtMatricula = itemView.findViewById(R.id.txtMatricula);
            txtDni = itemView.findViewById(R.id.txtDniCliente);

            /** CARGA UNA ACIVITY. Esto lo hice al principio para que no diera error.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Trabajo trabajo = trabajos.get(position);
                        Intent intent = new Intent(v.getContext(), DetalleTrabajoActivity.class);
                        intent.putExtra("trabajo_id", trabajo.id);
                        v.getContext().startActivity(intent);
                    }
                }
            });  */

            //CARGA UN FRAGMENT, el de JobsNewFragment, así se aprovecha el mismo formulario
            //para crear un nuevo trabajo y para editarlo (y verlo):
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Trabajo trabajo = trabajos.get(position);

                        //crea la instancia del nuevo fragment:
                        JobsNewFragment fragment = new JobsNewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("trabajo_id", trabajo.getId());
                        fragment.setArguments(bundle);

                        //obtiene  el FragmentManager y reemplaza el fragment:
                        FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment) //se carga en el contendedor de fragments del main
                                .addToBackStack(null) //esto permite volver atrás con el botón de retroceso
                                .commit();
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public TrabajoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trabajo, parent, false);
        return new TrabajoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabajoViewHolder holder, int position) {
        Trabajo trabajo = trabajos.get(position);
        holder.txtId.setText("ID: " + trabajo.id);
        holder.txtEstado.setText("Estado: " + trabajo.estado);
        holder.txtMatricula.setText("Matrícula: " + trabajo.vehiculo.matricula);
        holder.txtDni.setText("DNI Cliente: " + trabajo.vehiculo.cliente.dni);
    }

    @Override
    public int getItemCount() {
        return trabajos.size();
    }
}