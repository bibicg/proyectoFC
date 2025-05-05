package com.bcg.cartaller;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleTrabajoActivity extends AppCompatActivity {

    private TextView textId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_trabajo);

        textId = findViewById(R.id.textTrabajoId);

        // Recuperar el ID del trabajo desde el intent
        String trabajoId = getIntent().getStringExtra("trabajo_id");
        if (trabajoId != null) {
            textId.setText("ID trabajo: " + trabajoId);
        }
    }
}
