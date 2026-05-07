package edu.ub.pis2526.projecte;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreUserRepository;

public class LoginViewModel extends ViewModel {

    private final FirestoreUserRepository userRepository;
    private final MutableLiveData<LoginState> loginState;

    public LoginViewModel() {
        userRepository = new FirestoreUserRepository();
        loginState     = new MutableLiveData<>();
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String nom, String contrasenya) {
        if (nom.isEmpty() || contrasenya.isEmpty()) {
            loginState.postValue(new LoginState(false, null, null, null, "Omple tots els camps"));
            return;
        }

        userRepository.login(nom, contrasenya, new FirestoreUserRepository.OnLoginListener() {
            @Override
            public void onLoginSuccess(String nom, String correo, String rol) {
                loginState.postValue(new LoginState(true, nom, correo, rol, null));
            }
            @Override
            public void onLoginError(Exception e) {
                loginState.postValue(new LoginState(false, null, null, null, e.getMessage()));
            }
        });
    }

    public static final class LoginState {
        public final boolean success;
        public final String  nom;
        public final String  correo;
        public final String rol;
        public final String  errorMessage;

        public LoginState(boolean success, String nom, String correo,String rol, String errorMessage) {
            this.success      = success;
            this.nom          = nom;
            this.correo       = correo;
            this.rol = rol;
            this.errorMessage = errorMessage;

        }
    }
}