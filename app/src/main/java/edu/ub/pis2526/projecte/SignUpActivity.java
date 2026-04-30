package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import edu.ub.pis2526.projecte.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private SignUpViewModel signUpViewModel;
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // En onCreate, después de inicializar el binding
        Spinner spinnerRol = findViewById(R.id.spinnerRol);

// En el listener del botón registrar:
        String rol = spinnerRol.getSelectedItemPosition() == 0 ? "asistente" : "banda";
        signUpViewModel.signUp(
                binding.etNom.getText().toString().trim(),
                binding.etCorreo.getText().toString().trim(),
                binding.etContrasenya.getText().toString(),
                binding.etConfirmaContrasenya.getText().toString(),
                rol
        );

        initViewModel();
        initWidgetListeners();
    }

    private void initViewModel() {
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        initObservers();
    }

    private void initObservers() {
        signUpViewModel.getSignUpState().observe(this, state -> {
            if (state.success) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(state.errorMessage);
            }
        });
    }

    private void initWidgetListeners() {
        binding.btnRegistrar.setOnClickListener(v -> {
            binding.tvError.setVisibility(View.GONE);

            signUpViewModel.signUp(
                    binding.etNom.getText().toString().trim(),
                    binding.etCorreo.getText().toString().trim(),
                    binding.etContrasenya.getText().toString(),
                    binding.etConfirmaContrasenya.getText().toString()
            );
        });
    }
}