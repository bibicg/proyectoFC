package com.bcg.cartaller.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bcg.cartaller.Models.Job;
import com.bcg.cartaller.R;

import java.util.List;

/**
 * Mismo funcionamiento que ClientesAdapter pero para los jobs
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final List<Job> jobs;

    //Para abrir detalle de trabajo en otro fragment cuando se clica sobre un item:
    //conveniente con Listener en fragment (con intent para activitys):
    public interface OnJobClickListener {
        void onModifyJobClick(Job job);
    }

    private final OnJobClickListener listener;

    //tengo que añadire el listener que he creado en el adapter, porque antes solo tenia el listado de jobs:
    public JobAdapter(List<Job> jobs, OnJobClickListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    public class JobViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtStatus, txtLicensePlate, txtDni;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.textViewJobId);
            txtStatus = itemView.findViewById(R.id.textViewStatus);
            txtLicensePlate = itemView.findViewById(R.id.textViewLicensePlate);
            txtDni = itemView.findViewById(R.id.textViewDniCustomer);

            /** CARGA UNA ACIVITY. Esto lo hice al principio para que no diera error.
             itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Job trabajo = jobs.get(position);
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
                    Job trabajo = jobs.get(position);

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
                    listener.onModifyJobClick(jobs.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.txtId.setText("ID: " + job.getId());
        holder.txtStatus.setText("Estado: " + job.getStatus());
        holder.txtLicensePlate.setText("Matrícula: " + job.getCar().getLicensePlate());
        holder.txtDni.setText("DNI Cliente: " + job.getCar().getCustomer().getDni());
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}
