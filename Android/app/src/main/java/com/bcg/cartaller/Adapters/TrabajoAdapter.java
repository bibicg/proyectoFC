package com.bcg.cartaller.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bcg.cartaller.R;
import com.bcg.cartaller.Models.Trabajo;
import java.util.List;

/**
 * Mismo funcionamiento que ClientesAdapter pero para los trabajos
 */
public class TrabajoAdapter extends RecyclerView.Adapter<TrabajoAdapter.TrabajoViewHolder> {

    private final List<Trabajo> trabajos;

    //Para abrir detalle de trabajo en otro fragment cuando se clica sobre un item:
    //conveniente con Listener en fragment (con intent para activitys):
    public interface OnTrabajoClickListener {
        void onModificarTrabajoClick(Trabajo trabajo);
    }

    private final OnTrabajoClickListener listener;

    //tengo que añadire el listener que he creado en el adapter, porque antes solo tenia el listado de trabajos:
    public TrabajoAdapter(List<Trabajo> trabajos, OnTrabajoClickListener listener) {
        this.trabajos = trabajos;
        this.listener = listener;
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
            /**
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
                    .addToBackStack(null) //para poder volver atrás con el btn de retroceso
                    .commit();
                    }
                }
            });*/
            //USA LISTENER:
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onModificarTrabajoClick(trabajos.get(position));
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
        holder.txtId.setText("ID: " + trabajo.getId());
        holder.txtEstado.setText("Estado: " + trabajo.getEstado());
        holder.txtMatricula.setText("Matrícula: " + trabajo.getVehiculo().getMatricula());
        holder.txtDni.setText("DNI Cliente: " + trabajo.getVehiculo().getCliente().getDni());
    }

    @Override
    public int getItemCount() {
        return trabajos.size();
    }
}
