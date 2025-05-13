package com.bcg.cartaller.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bcg.cartaller.Models.Cliente;
import com.bcg.cartaller.R;
import java.util.List;

/**
 * Para ver los clientes localizados en el clientSearchFragment.
 */
public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {

    public interface OnClienteClickListener {
        void onDetalleClienteClick(Cliente cliente);
        void onAgregarVehiculoClick(Cliente cliente);
    }

    private final List<Cliente> clientes;
    private final OnClienteClickListener listener;

    public ClienteAdapter(List<Cliente> clientes, OnClienteClickListener listener) {
        this.clientes = clientes;
        this.listener = listener;
    }

    public class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView txtDni, txtNombre, txtTelefono, txtVehiculos;
        Button btnAñadirVehiculo;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDni = itemView.findViewById(R.id.textClienteDni);
            txtNombre = itemView.findViewById(R.id.textClienteNombre);
            txtTelefono = itemView.findViewById(R.id.textClienteTelefono);
            txtVehiculos = itemView.findViewById(R.id.textClienteVehiculos);
            btnAñadirVehiculo = itemView.findViewById(R.id.anadirVehiculoButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDetalleClienteClick(clientes.get(position));
                }
            });

            btnAñadirVehiculo.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAgregarVehiculoClick(clientes.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = clientes.get(position);
        holder.txtDni.setText("DNI: " + cliente.dni);
        holder.txtNombre.setText("Nombre: " + cliente.nombre);
        holder.txtTelefono.setText("Teléfono: " + cliente.telefono);
        holder.txtVehiculos.setText("Vehículos: " + cliente.numVehiculos);
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }
}
