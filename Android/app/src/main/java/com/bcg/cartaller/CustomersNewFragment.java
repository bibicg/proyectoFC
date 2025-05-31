package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Models.Customer;
import com.bcg.cartaller.Repositories.CustomerRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá crear un nuevo customer cubriendo un formulario
 * con nombre, apellidos, dni, domicilio, teléfono y vehículos a su nombre.
 *
 * También desde el mismo formulario podrá editar los datos de un customer que ya existe en BD.
 * Para ello, los botones se muestran/ocultan.
 */
public class CustomersNewFragment extends Fragment {
    private EditText etName, etSurname, etDni, etPhone, etMail, etAddress;
    private Button btnSaveCustomer, btnModifyCustomer;

    //ya no es necesario porque se hace en el repository:
    //private RequestQueue queue;
    //private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    //private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";

    //Creo una instancia del repo para poder separar la lógica de la UI:
    private CustomerRepository customerRepository;

    public CustomersNewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers_new, container, false);

        etName = view.findViewById(R.id.editTextName);
        etSurname = view.findViewById(R.id.editTextSurname);
        etDni = view.findViewById(R.id.editTextDni);
        etPhone = view.findViewById(R.id.editTextPhone);
        etMail = view.findViewById(R.id.editTextMail);
        etAddress = view.findViewById(R.id.editTextAddress);
        btnSaveCustomer = view.findViewById(R.id.saveCustomerButton);
        btnModifyCustomer = view.findViewById(R.id.modifyCustomerButton);

        //Ya no es necesario pq se hace en el repository:
        //queue = Volley.newRequestQueue(requireContext());

        //inicializo el repo:
        customerRepository = new CustomerRepository(requireContext());

        /**
         * PARA MODIFICAR UN CLIENTE QUE YA EXISTE EN BD:
         * se pasan los datos desde el customer encontrado en clientsSearchFragment:
         */
        if (getArguments() != null && getArguments().containsKey("dni")) {
            loadClientForModification(getArguments());
        }


        btnSaveCustomer.setOnClickListener(v -> {
            btnSaveCustomer.setEnabled(false);
            /**
            //AHORA LA VERIFICACIÓN DEL DNI SE HACE EN EL CUSTOMER REPOSITOTY.
            String dni = etDni.getText().toString().trim();
            String url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + Uri.encode(dni);

            //primero hay que comprobar que el cliente no existe en BD. Se hace a través del dni:
            JsonArrayRequest checkDni = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        if (response.length() > 0) {
                            Toast.makeText(getContext(), "Ya existe un customer con ese DNI", Toast.LENGTH_SHORT).show();
                            btnSaveCustomer.setEnabled(true);
                        } else {
                            //Cuando se ha comprobado que el dni no existe en BD, se llama al método de guardar customer:
                            //saveClient();

                            //AHORA SE LLAMA AL REPOSITORIO, QUE ES QUIEN HACE LA CONSULTA HTTP - POST:
                            // Extraer datos del formulario
                            String name = etName.getText().toString().trim();
                            String surname = etSurname.getText().toString().trim();
                            String phone = etPhone.getText().toString().trim();
                            String email = etMail.getText().toString().trim();
                            String address = etAddress.getText().toString().trim();

                            if (name.isEmpty() || surname.isEmpty() || dni.isEmpty()) {
                                Toast.makeText(getContext(), "Nombre, apellidos y DNI son obligatorios", Toast.LENGTH_SHORT).show();
                                btnSaveCustomer.setEnabled(true);
                                return;
                            }

                            // Crear objeto Customer sin id
                            Customer newCustomer = new Customer();
                            newCustomer.setDni(dni);
                            newCustomer.setName(name);
                            newCustomer.setSurname(surname);
                            newCustomer.setPhone(phone);
                            newCustomer.setEmail(email);
                            newCustomer.setAddress(address);

                            // Llamar al repositorio para guardar el customer
                            customerRepository.saveCustomer(newCustomer, new CustomerRepository.CustomerSaveCallback() {
                                @Override
                                public void onSuccess() {
                                    btnSaveCustomer.setEnabled(true);
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Cliente guardado")
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                dialog.dismiss();
                                                cleanForm();
                                            })
                                            .show();
                                }

                                @Override
                                public void onError(String message) {
                                    btnSaveCustomer.setEnabled(true);
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Error")
                                            .setMessage(message)
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            });
                        }
                    },
                    error -> {
                        Toast.makeText(getContext(), "Error al verificar DNI", Toast.LENGTH_SHORT).show();
                        btnSaveCustomer.setEnabled(true);
                        if (error.networkResponse != null) {
                            Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                            Log.e("SUPABASE", new String(error.networkResponse.data));
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("access_token", "");

                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", API_ANON_KEY);
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };*/

            String dni = etDni.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String surname = etSurname.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etMail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (dni.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                Toast.makeText(getContext(), "Nombre, apellidos y DNI son obligatorios", Toast.LENGTH_SHORT).show();
                btnSaveCustomer.setEnabled(true);
                return;
            }

            customerRepository.checkDniExists(dni, new CustomerRepository.DniCheckCallback() {
                @Override
                public void onExists() {
                    Toast.makeText(getContext(), "Ya existe un cliente con ese DNI", Toast.LENGTH_SHORT).show();
                    btnSaveCustomer.setEnabled(true);
                }

                @Override
                public void onNotExists() {
                    Customer newCustomer = new Customer();
                    newCustomer.setDni(dni);
                    newCustomer.setName(name);
                    newCustomer.setSurname(surname);
                    newCustomer.setPhone(phone);
                    newCustomer.setEmail(email);
                    newCustomer.setAddress(address);

                    customerRepository.saveCustomer(newCustomer, new CustomerRepository.CustomerSaveCallback() {
                        @Override
                        public void onSuccess() {
                            btnSaveCustomer.setEnabled(true);
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Cliente guardado con éxito")
                                    .setMessage("DNI Cliente: " + dni)
                                    .setIcon(R.drawable.ok_dialog)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        cleanForm();
                                    })
                                    .show();
                        }

                        @Override
                        public void onError(String message) {
                            btnSaveCustomer.setEnabled(true);
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Error")
                                    .setMessage("El cliente no se ha guardado")
                                    .setIcon(R.drawable.error_dialog)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    btnSaveCustomer.setEnabled(true);
                }
            });
        });

        return view;
    }

    /**
     * MÉTODO QUE GUARDA EL CLIENTE EN BD DE SUPABASE.
     * Ahora en CustomerRepository (saveCustomer)

    private void saveClient() {
        // 1 - extraigo los datos de los campos del formulario
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etMail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // 2 - compruebo que los obligatorios están cubiertos
        if (name.isEmpty() || surname.isEmpty() || dni.isEmpty()) {
            Toast.makeText(getContext(), "Nombre, apellidos y DNI son obligatorios", Toast.LENGTH_SHORT).show();
            btnSaveCustomer.setEnabled(true);
            return;
        }

        // 3 - creo el objeto cliente con todos sus datos
        JSONObject customerJson = new JSONObject();
        try {
            customerJson.put("nombre", name);
            customerJson.put("apellidos", surname);
            customerJson.put("dni", dni);
            customerJson.put("telefono", phone);
            customerJson.put("email", email);
            customerJson.put("direccion", address);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error preparando datos", Toast.LENGTH_SHORT).show();
            btnSaveCustomer.setEnabled(true);
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/clientes";


        //OPCION CON STRING REQUEST, POR SI NO SE RECIBE UN JSON, PORQUE VOLLEY LO ESPERA

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    btnSaveCustomer.setEnabled(true);
                    //Toast.makeText(getContext(), "Cliente guardado con éxito: " + response.optString("dni"), Toast.LENGTH_SHORT).show();
                    //muestra el mensaje de éxito en un dialog en lugar del toast:
                    new AlertDialog.Builder(getContext())
                            .setTitle("Cliente guardado")
                            //.setMessage("El cliente se ha guardado con éxito: " + response.optString("dni"))
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                cleanForm(); // Limpia el formulario al cerrar
                            })
                            .show();


                    cleanForm();
                },
                error -> {
                    btnSaveCustomer.setEnabled(true);
                    //Toast.makeText(getContext(), "Error al guardar customer", Toast.LENGTH_SHORT).show();
                    //Lo mismo, dialog para el mensaje al usuario en lugar de un toast:
                    new AlertDialog.Builder(getContext())
                            .setTitle("Error")
                            .setMessage("Ha habido un problema al intentar guardar el cliente. Inténtalo de nuevo.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();

                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return customerJson.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

        /**OPCION CON JSON OBJECT REQUEST:
        // Hay que añadir return=representation en los headers,para que devuelva:
        Status: 201 Created
        Body: [
        {
            "id": 12,
             "dni": "33333333A",
            ...
        }
        ]
         Sino, devuleve un body vacio y volley lo interpreta como error, entonces el customer
         se guarda en supabase, pero la app no lo muestra
         */

        /**
        JsonObjectRequest request = new JsonObjectRequest(
    }*/

    private void cleanForm(){
        etName.setText("");
        etSurname.setText("");
        etDni.setText("");
        etPhone.setText("");
        etMail.setText("");
        etAddress.setText("");

    }

    private void loadClientForModification(Bundle args) {
        etDni.setText(args.getString("dni"));
        etDni.setEnabled(false); // no permitir modificar DNI
        etName.setText(args.getString("nombre"));
        etSurname.setText(args.getString("apellidos"));
        etPhone.setText(args.getString("telefono"));
        etMail.setText(args.getString("email"));
        etAddress.setText(args.getString("direccion"));

        btnSaveCustomer.setVisibility(View.GONE);
        btnModifyCustomer.setVisibility(View.VISIBLE);

        btnModifyCustomer.setOnClickListener(v -> {
            //modifyClient(args.getString("dni")); // PATCH

            //Ahora, el método para modificar cliente está en el Customer Repositoty:
            String nombre = etName.getText().toString().trim();
            String apellidos = etSurname.getText().toString().trim();
            String telefono = etPhone.getText().toString().trim();
            String email = etMail.getText().toString().trim();
            String direccion = etAddress.getText().toString().trim();

            if (nombre.isEmpty() || apellidos.isEmpty()) {
                Toast.makeText(getContext(), "Nombre y apellidos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            Customer updatedCustomer = new Customer();
            updatedCustomer.setName(nombre);
            updatedCustomer.setSurname(apellidos);
            updatedCustomer.setPhone(telefono);
            updatedCustomer.setEmail(email);
            updatedCustomer.setAddress(direccion);

            customerRepository.modifyCustomer(args.getString("dni"), updatedCustomer, new CustomerRepository.CustomerModifyCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show();
                    cleanForm();
                    btnModifyCustomer.setVisibility(View.GONE);
                    btnSaveCustomer.setVisibility(View.VISIBLE);
                    etDni.setEnabled(true);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    /**
     * Modificar cliente. Ahora en el CustomerRepository.
     * @param dniOriginal

    private void modifyClient(String dniOriginal) {
        String nombre = etName.getText().toString().trim();
        String apellidos = etSurname.getText().toString().trim();
        String telefono = etPhone.getText().toString().trim();
        String email = etMail.getText().toString().trim();
        String direccion = etAddress.getText().toString().trim();

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y apellidos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject updateCustomer = new JSONObject();
        try {
            updateCustomer.put("nombre", nombre);
            updateCustomer.put("apellidos", apellidos);
            updateCustomer.put("telefono", telefono);
            updateCustomer.put("email", email);
            updateCustomer.put("direccion", direccion);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error preparando datos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + Uri.encode(dniOriginal);

        /**
         * En SUPABASE es mejor usar PATCH que PUT, ya que PUT afecta a toda la fila, PATH solo al campo concreto que se
         * cambia. Por eso, si usas PUT y hay algún error en algún campo, puedes llegar a borrar ese registro de BD!!!
         *
         * Uso StringRequest pq con JsonObjectRequest, al igual que en los POST, tb da erro la app
         */
/**        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Toast.makeText(getContext(), "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show();
                    cleanForm();
                    btnModifyCustomer.setVisibility(View.GONE);
                    btnSaveCustomer.setVisibility(View.VISIBLE);
                    etDni.setEnabled(true);
                },
                error -> {
                    Toast.makeText(getContext(), "Error al actualizar cliente", Toast.LENGTH_SHORT).show();
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return updateCustomer.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Prefer", "return=representation");
                return headers;
            }
        };
        queue.add(request);
    }*/
}

