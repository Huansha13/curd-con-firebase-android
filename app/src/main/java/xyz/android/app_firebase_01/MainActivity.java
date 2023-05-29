package xyz.android.app_firebase_01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import xyz.android.app_firebase_01.model.Luchador;

public class MainActivity extends AppCompatActivity {

    private EditText txtid, txtnom;
    private Button btnbus, btnmod, btnreg, btneli;
    private ListView lvDatos;
    FirebaseDatabase db;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(Luchador.class.getSimpleName());

        // Si el nombre de tu clase es diferente
        // dbRef = db.getReference().child("Luchador");

        txtid   = findViewById(R.id.txtid);
        txtnom  = findViewById(R.id.txtnom);
        btnbus  = findViewById(R.id.btnbus);
        btnmod  = findViewById(R.id.btnmod);
        btnreg  = findViewById(R.id.btnreg);
        btneli  = findViewById(R.id.btneli);
        lvDatos = findViewById(R.id.lvDatos);

        botonBuscar();
        botonModificar();
        botonRegistrar();
        botonEliminar();
        listarLuchadores();

    }

    private void botonBuscar(){
        btnbus.setOnClickListener(v -> {
            String idtxt = txtid.getText().toString();
            if (idtxt.isEmpty()) {
                ocultarTeclado();
                Toast.makeText(this, "Ingrese el id del luchador", Toast.LENGTH_SHORT).show();
                return;
            }


            Integer id = Integer.parseInt(idtxt);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ocultarTeclado();
                    String nombreLuchador = StreamSupport.stream(snapshot.getChildren().spliterator(), false)
                            .filter(l -> l.child("id").getValue().toString().equals(id.toString()))
                            .map(x -> x.child("nombre").getValue().toString())
                            .findFirst()
                            .orElse("");

                    txtnom.setText(nombreLuchador);
                    if ("".equals(nombreLuchador))
                        Toast.makeText(MainActivity.this, "No se encontro luchador", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void botonModificar(){
        btnmod.setOnClickListener(v -> {
            String idtxt = txtid.getText().toString();
            String nombre = txtnom.getText().toString();
            if (idtxt.isEmpty() || nombre.isEmpty()) {
                ocultarTeclado();
                Toast.makeText(this, "Falta el id o nombre del luchador", Toast.LENGTH_SHORT).show();
                return;
            }


            Integer id = Integer.parseInt(idtxt);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ocultarTeclado();
                    DataSnapshot data = StreamSupport.stream(snapshot.getChildren().spliterator(), false)
                            .filter(l -> l.child("id").getValue().toString().equals(id.toString()))
                            .findFirst()
                            .orElse(null);

                    if (data == null) {
                        Toast.makeText(MainActivity.this, "Registro no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ocultarTeclado();
                    data.getRef().child("nombre").setValue(nombre);

                    listarLuchadores();
                    txtnom.setText("");
                    txtid.setText("");

                    Toast.makeText(MainActivity.this, "Registro actualizado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void botonRegistrar(){
        btnreg.setOnClickListener(v -> {
            if (txtid.getText().toString().trim().isEmpty()
                    || txtnom.getText().toString().trim().isEmpty()) {

                ocultarTeclado();
                Toast.makeText(this, "Complete los campos faltantes", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer idLuchador = Integer.parseInt(txtid.getText().toString());
            String nombreLuchador = txtnom.getText().toString();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isDuplicado = StreamSupport.stream(snapshot.getChildren().spliterator(), false)
                            .map(dataSnapshot -> dataSnapshot.child("id").getValue().toString())
                            .anyMatch(taskId -> taskId.equals(idLuchador.toString()));

                    if (isDuplicado) {
                        String mss = String.format("Error el luchador ya existe", idLuchador);
                        Toast.makeText(MainActivity.this, mss, Toast.LENGTH_SHORT).show();
                        return;
                    }


                    Luchador  luchador = new Luchador(idLuchador, nombreLuchador);
                    dbRef.push().setValue(luchador);
                    ocultarTeclado();

                    Toast.makeText(MainActivity.this, "Luchador registrado correctamente", Toast.LENGTH_SHORT).show();
                    txtid.setText("");
                    txtnom.setText("");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    private void botonEliminar(){
        btneli.setOnClickListener(v -> {
            String idtxt = txtid.getText().toString();
            if (idtxt.isEmpty()) {
                ocultarTeclado();
                Toast.makeText(this, "Ingrese el id del luchador", Toast.LENGTH_SHORT).show();
                return;
            }


            Integer id = Integer.parseInt(idtxt);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ocultarTeclado();
                    DataSnapshot data = StreamSupport.stream(snapshot.getChildren().spliterator(), false)
                            .filter(l -> l.child("id").getValue().toString().equals(id.toString()))
                            .findFirst()
                            .orElse(null);

                    if (data == null) {
                        Toast.makeText(MainActivity.this, "Registro no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setCancelable(true);
                    dialog.setTitle("Eliminar");
                    dialog.setMessage("¿Estas seguro de eliminar el registro?");
                    dialog.setNegativeButton("Cancelar", (d, w) -> {});
                    dialog.setPositiveButton("Sí", (d, i) -> {

                        ocultarTeclado();
                        data.getRef().removeValue();

                        listarLuchadores();
                        txtnom.setText("");
                        txtid.setText("");

                        Toast.makeText(MainActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                    });
                    dialog.show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void listarLuchadores() {
        ArrayList<Luchador> luchadores = new ArrayList<>();
        ArrayAdapter<Luchador> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, luchadores);
        lvDatos.setAdapter(adapter);

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Luchador luchador = snapshot.getValue(Luchador.class);
                luchadores.add(luchador);
                adapter.notifyDataSetChanged(); // Refrescar la lista
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        lvDatos.setOnItemClickListener((adapterView, view, i, l) -> {
            Luchador luchador = luchadores.get(i);
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setCancelable(true);
            dialog.setTitle("Luchador Seleccionado");

            String mss = String.format("Id: %s\nNombre: %s", luchador.getId(), luchador.getNombre());
            dialog.setMessage(mss);
            dialog.show();
        });
    }

    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}