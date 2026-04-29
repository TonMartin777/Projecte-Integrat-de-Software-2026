package edu.ub.pis2526.projecte;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

public class EventParticipantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirestoreEventRepository repo;
    private String eventId;
    private List<Map<String, String>> participantes = new ArrayList<>();
    private ParticipantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        eventId = getIntent().getStringExtra("EVENT_ID");
        repo = new FirestoreEventRepository();

        recyclerView = findViewById(R.id.recyclerParticipantes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantAdapter(participantes);
        recyclerView.setAdapter(adapter);

        Button btnCopiar = findViewById(R.id.btnCopiarCorreos);
        btnCopiar.setOnClickListener(v -> copiarCorreos());

        cargarParticipantes();
    }

    private void cargarParticipantes() {
        repo.getParticipantesConCorreos(eventId, new FirestoreEventRepository.OnParticipantesConCorreosListener() {
            @Override
            public void onSuccess(List<Map<String, String>> lista) {
                participantes.clear();
                participantes.addAll(lista);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventParticipantsActivity.this, "Error cargando participantes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copiarCorreos() {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> p : participantes) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(p.get("correo"));
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("correos", sb.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Correos copiados al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {
        private List<Map<String, String>> list;

        ParticipantAdapter(List<Map<String, String>> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, String> p = list.get(position);
            holder.text1.setText(p.get("nom"));
            holder.text2.setText(p.get("correo"));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}