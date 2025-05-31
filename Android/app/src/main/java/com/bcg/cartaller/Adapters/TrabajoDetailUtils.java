package com.bcg.cartaller.Adapters;


/**
 * Creo esta clase porque se hace exactamente lo mismo desde las clases
 * ProfileFragment y JobsSearchFragment, para cargar el JobsDetailFragment con todos los detalles.
 * Para que no haya código repetido, lo encapsulo en esta clase auxiliar.
 */
public class TrabajoDetailUtils {

    /**
    public static Job fromJson(JSONObject trabajoJson) throws JSONException {
        //extraigo vehículo y customer
        JSONObject vehiculoJson = trabajoJson.getJSONObject("vehiculos");
        JSONObject clienteJson = vehiculoJson.getJSONObject("clientes");

        Customer customer = new Customer(clienteJson.getString("dni"));
        Car car = new Car(vehiculoJson.getString("matricula"), customer);


        Job job = new Job(
                String.valueOf(trabajoJson.getInt("id")),
                trabajoJson.getString("estado"),
                trabajoJson.getString("descripcion"),
                car
        );


        job.startDate = trabajoJson.optString("fecha_inicio", null);
        job.endDate = trabajoJson.optString("fecha_fin", null);
        job.comment = trabajoJson.optString("comentarios", null);
        job.image = trabajoJson.optString("imagen", null);
        job.mechanicId = trabajoJson.optString("mecanico_id", null);

        return job;
    }*/
}
