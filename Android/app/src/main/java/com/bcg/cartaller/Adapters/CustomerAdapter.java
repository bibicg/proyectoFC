package com.bcg.cartaller.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bcg.cartaller.Models.Customer;
import com.bcg.cartaller.R;
import java.util.List;

/**
 * Adapter para ver los customers localizados en el clientSearchFragment.
 *
 * Realmente esto no es necesario: no se necesita un RV porque solo se recibe un customer por búsqueda.
 * Lo hice asi al principio y no lo medité bien. Lo ideal sería cargarlo directamente en el fragment quizás.
 * Si me da tiempo lo puedo modificar (no cargar en el formulario directamente ya que hay que añadir el botón de vehículo
 * y además mi idea no es que se vea toda la información de golpe).
 */
public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    //interfaz para manejar eventos de clics en los items de customers del RV:
    public interface OnCustomerClickListener {
        void onDetailCustomerClick(Customer customer);
        void onAddCarClick(Customer customer);
        void onModifyCustomerClick(Customer customer);
    }

    //lista de customers que se mostrarán en el RV:
    private final List<Customer> customers;
    //listener que implementa el fragment:
    private final OnCustomerClickListener listener;

    //cnstructor del adaptador que recibe la lista de customers y el listener de clics:
    public CustomerAdapter(List<Customer> customers, OnCustomerClickListener listener) {
        this.customers = customers;
        this.listener = listener;
    }

    //viewHolder interno que representa cada elemento de la lista del RV:
    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView txtDni, txtName, txtPhone, txtCars;
        Button btnAddCar, btnModifyCustomer;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDni = itemView.findViewById(R.id.textViewCustomerDni);
            txtName = itemView.findViewById(R.id.textViewCustomerName);
            txtPhone = itemView.findViewById(R.id.textViewCustomerPhone);
            txtCars = itemView.findViewById(R.id.textViewCustomerCars);
            btnAddCar = itemView.findViewById(R.id.addNewCarButton);
            btnModifyCustomer = itemView.findViewById(R.id.modifyCustomerButton);

            //Listener para cuando se toca el item completo:
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDetailCustomerClick(customers.get(position));
                }
            });

            //Listener para el botón de AÑADIR VEHÍCULO:
            btnAddCar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAddCarClick(customers.get(position));
                }
            });

            //Listener para el botón de MODIFICAR CLIENTE:
            btnModifyCustomer.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onModifyCustomerClick(customers.get(position));
                }
            });
        }
    }

    //crea viewholder inflando el xml del item_cliente:
    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new CustomerViewHolder(view);
    }
    //vincula los datos del customer al viewholder:
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.txtDni.setText("DNI: " + customer.dni);
        holder.txtName.setText("Nombre: " + customer.name);
        holder.txtPhone.setText("Teléfono: " + customer.phone);
        holder.txtCars.setText("Vehículos: " + customer.numCars);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }
}
