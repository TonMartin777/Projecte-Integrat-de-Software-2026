package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("NOM_USUARI", binding.etNom.getText().toString().trim());
                intent.putExtra("CORREO_USUARI", binding.etCorreo.getText().toString().trim());
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