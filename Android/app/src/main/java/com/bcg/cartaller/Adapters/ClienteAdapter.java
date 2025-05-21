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
import com.google.android.material.button.MaterialButton;

/**
 * Adapter para ver los clientes localizados en el clientSearchFragment.
 *
 * Realmente esto no es necesario: no se necesita un RV porque solo se recibe un cliente por búsqueda.
 * Lo hice asi al principio y no lo medité bien. Lo ideal sería cargarlo directamente en el fragment quizás.
 * Si me da tiempo lo puedo modificar (no cargar en el formulario directamente ya que hay que añadir el botón de vehículo
 * y además mi idea no es que se vea toda la información de golpe).
 */
public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {

    //interfaz para manejar eventos de clics en los items de clientes del RV:
    public interface OnClienteClickListener {
        void onDetalleClienteClick(Cliente cliente);
        void onAgregarVehiculoClick(Cliente cliente);
        void onModificarClienteClick(Cliente cliente);
    }

    //lista de clientes que se mostrarán en el RV:
    private final List<Cliente> clientes;
    //listener que implementa el fragment:
    private final OnClienteClickListener listener;

    //cnstructor del adaptador que recibe la lista de clientes y el listener de clics:
    public ClienteAdapter(List<Cliente> clientes, OnClienteClickListener listener) {
        this.clientes = clientes;
        this.listener = listener;
    }

    //viewHolder interno que representa cada elemento de la lista del RV:
    public class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView txtDni, txtNombre, txtTelefono, txtVehiculos;
        Button btnAnadirVehiculo, btnModificarCliente;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDni = itemView.findViewById(R.id.textClientDni);
            txtNombre = itemView.findViewById(R.id.textClientNombre);
            txtTelefono = itemView.findViewById(R.id.textClientTelefono);
            txtVehiculos = itemView.findViewById(R.id.textClientVehiculos);
            btnAnadirVehiculo = itemView.findViewById(R.id.anadirVehButton);
            btnModificarCliente = itemView.findViewById(R.id.modificarClientButton);

            //Listener para cuando se toca el item completo:
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDetalleClienteClick(clientes.get(position));
                }
            });

            //Listener para el botón de AÑADIR VEHÍCULO:
            btnAnadirVehiculo.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAgregarVehiculoClick(clientes.get(position));
                }
            });

            //Listener para el botón de MODIFICAR CLIENTE:
            btnModificarCliente.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onModificarClienteClick(clientes.get(position));
                }
            });
        }
    }

    //crea viewholder inflando el xml del item_cliente:
    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(view);
    }
    //vincula los datos del cliente al viewholder:
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
