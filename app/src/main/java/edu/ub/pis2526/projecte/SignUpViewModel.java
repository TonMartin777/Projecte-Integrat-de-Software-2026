package edu.ub.pis2526.projecte;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreUserRepository;

public class SignUpViewModel extends ViewModel {

    // Model
    private final FirestoreUserRepository userRepository;

    // Observable
    private final MutableLiveData<SignUpState> signUpState;

    public SignUpViewModel() {
        userRepository = new FirestoreUserRepository();
        signUpState    = new MutableLiveData<>();
    }

    public LiveData<SignUpState> getSignUpState() {
        return signUpState;
    }

    /**
     * Valida les dades i delega el registre al repositori.
     */
    public void signUp(String nom, String correo,
                       String contrasenya, String confirmaContrasenya) {

        // Validacions bàsiques al ViewModel
        if (nom.isEmpty() || correo.isEmpty() || contrasenya.isEmpty()) {
            signUpState.postValue(new SignUpState(false, "Omple tots els camps"));
            return;
        }
        if (!contrasenya.equals(confirmaContrasenya)) {
            signUpState.postValue(new SignUpState(false, "Les contrasenyes no coincideixen"));
            return;
        }
        if (contrasenya.length() < 6) {
            signUpState.postValue(new SignUpState(false, "La contrasenya ha de tenir mínim 6 caràcters"));
            return;
        }

        userRepository.signUp(nom, correo, contrasenya,
                new FirestoreUserRepository.OnSignUpListener() {
                    @Override
                    public void onSignUpSuccess() {
                        signUpState.postValue(new SignUpState(true, null));
                    }
                    @Override
                    public void onSignUpError(Exception e) {
                        signUpState.postValue(new SignUpState(false, e.getMessage()));
                    }
                }
        );
    }

    // Classe interna d'estat
    public static final class SignUpState {
        public final boolean success;
        public final String  errorMessage;

        public SignUpState(boolean success, String errorMessage) {
            this.success      = success;
            this.errorMessage = errorMessage;
        }
    }
}