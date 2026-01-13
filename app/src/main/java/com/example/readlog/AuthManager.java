package com.example.readlog;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private final CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface AuthResultListener {
        void onAuthSuccess(String idToken);
        void onAuthFailure(String errorMessage);
    }

    private AuthManager(Context context) {
        this.credentialManager = CredentialManager.create(context.getApplicationContext());
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public void signInWithGoogle(Activity activity, AuthResultListener listener) {
        String serverClientId = activity.getString(R.string.google_server_client_id);
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity, request, new CancellationSignal(), executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        try {
                            GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                            String idToken = credential.getIdToken();
                            activity.runOnUiThread(() -> listener.onAuthSuccess(idToken));
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear la credencial de Google.", e);
                            activity.runOnUiThread(() -> listener.onAuthFailure("Error al procesar la respuesta de Google."));
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        String errorMessage;
                        if (e instanceof GetCredentialCancellationException) {
                            errorMessage = "El usuario canceló el inicio de sesión.";
                        } else {
                            errorMessage = "Error al obtener la credencial: " + e.getClass().getSimpleName();
                        }
                        Log.e(TAG, errorMessage, e);
                        activity.runOnUiThread(() -> listener.onAuthFailure(errorMessage));
                    }
                });
    }

    public void signOut(Activity activity, Runnable onSignOutComplete) {
        ClearCredentialStateRequest request = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                request,
                null, // CancellationSignal
                executor,
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        Log.d(TAG, "Estado de credenciales limpiado con éxito.");
                        activity.runOnUiThread(onSignOutComplete);
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Error al limpiar el estado de las credenciales.", e);
                        // Aun así, ejecuta el callback para limpiar la UI localmente
                        activity.runOnUiThread(onSignOutComplete);
                    }
                });
    }
}
