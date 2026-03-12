package edu.ub.pis2526.projecte;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.ub.pis2526.projecte.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
  }
}