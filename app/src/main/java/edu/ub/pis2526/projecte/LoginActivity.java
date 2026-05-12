package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import edu.ub.pis2526.projecte.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViewModel();
        initWidgetListeners();
    }

    private void initViewModel() {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        initObservers();
    }

    private void initObservers() {
        loginViewModel.getLoginState().observe(this, state -> {
            if (state.success) {
                if ("banda".equals(state.rol)) {
                    Intent intent = new Intent(this, UserActivity.class);
                    intent.putExtra("NOM_USUARI", state.nom);
                    intent.putExtra("CORREO_USUARI", state.correo);
                    intent.putExtra("ROL", state.rol);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("NOM_USUARI", state.nom);
                    intent.putExtra("CORREO_USUARI", state.correo);
                    intent.putExtra("ROL", state.rol);
                    startActivity(intent);
                }
                finish();
            } else {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(state.errorMessage);
            }
        });
    }

    private void initWidgetListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            binding.tvError.setVisibility(View.GONE);
            loginViewModel.login(
                    binding.etNom.getText().toString().trim(),
                    binding.etContrasenya.getText().toString()
            );
        });

        binding.tvAnarARegistre.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }
}