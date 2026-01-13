package com.example.readlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements AuthManager.AuthResultListener {

    private static final String PREFS_NAME = "com.example.readlog.PREFS";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String TAG = "AUTH_DEBUG";

    private RecyclerView rvLibrosActivos, rvLibrosLeidos;
    private LibroAdapter adapterActivos, adapterLeidos;
    private EditText etBusqueda;
    private MaterialToolbar topAppBar;
    private LinearLayout headerLeidos;
    private ImageView arrowLeidos;
    private MaterialDivider dividerLeidos;
    private ExtendedFloatingActionButton fabNuevo;

    private AuthManager authManager;
    private BookRepository bookRepository;
    private SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        authManager = AuthManager.getInstance(this);
        bookRepository = new BookRepository(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        bindViews();
        setupRecyclerViews();
        setupHeaderLeidos();
        setupListeners();
        setupFabInsets();
        setupAuthStateListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthStateListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (this.currentUser == user && user != null) return;

            this.currentUser = user;
            Log.d(TAG, "AuthStateListener: El estado del usuario cambió -> " + (user != null ? user.getUid() : "Nulo"));

            invalidateOptionsMenu();
            loadBooksFromRepository("");

            if (user != null) {
                bookRepository.performInitialMigration();
            } else {
                clearLocalSessionData();
            }
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem accountItem = topAppBar.getMenu().findItem(R.id.btnCuenta);
        if (accountItem == null) return super.onPrepareOptionsMenu(menu);

        Log.d(TAG, "onPrepareOptionsMenu: Dibujando menú. currentUser es " + (currentUser != null ? "NO nulo" : "nulo"));

        if (currentUser != null) {
            Uri photoUri = currentUser.getPhotoUrl();
            Log.d(TAG, "onPrepareOptionsMenu: photoUri leída de FirebaseUser -> " + photoUri);

            if (photoUri != null) {
                loadProfileImage(accountItem, photoUri.toString());
            } else {
                accountItem.setIcon(R.drawable.ic_account_circle_filled);
            }
        } else {
            accountItem.setIcon(R.drawable.ic_account_circle);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadProfileImage(MenuItem accountItem, String photoUrl) {
        final int iconSize = (int) (getResources().getDisplayMetrics().density * 24);
        Log.d(TAG, "Glide: Intentando cargar imagen desde -> " + photoUrl);

        Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .circleCrop()
                .override(iconSize, iconSize)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d(TAG, "Glide: Imagen cargada y lista para mostrar.");
                        accountItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e(TAG, "Glide: FALLO al cargar la imagen de perfil.");
                        accountItem.setIcon(R.drawable.ic_account_circle_filled);
                    }
                });
    }

    @Override
    public void onAuthSuccess(String idToken) {
        Log.d(TAG, "Credential Manager obtuvo el idToken. Procediendo a iniciar sesión en Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential: ÉXITO");
                        Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
                        try {
                            String[] splitToken = idToken.split("\\.");
                            String body = new String(Base64.decode(splitToken[1], Base64.DEFAULT), StandardCharsets.UTF_8);
                            JSONObject jsonObject = new JSONObject(body);

                            sharedPreferences.edit()
                                    .putString(KEY_USER_EMAIL, jsonObject.optString("email"))
                                    .commit();

                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear el ID Token para datos de perfil", e);
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential: FALLO", task.getException());
                        onAuthFailure(task.getException().getMessage());
                    }
                });
    }

    private void clearLocalSessionData() {
        sharedPreferences.edit()
                .remove(KEY_USER_EMAIL)
                .commit();
    }

    private void loadBooksFromRepository(String query) {
        if (bookRepository == null) return;
        bookRepository.getBooks(currentUser).observe(this, libros -> {
            if (libros == null) return;
            List<Libro> filteredBooks = new ArrayList<>();
            if (query.isEmpty()) {
                filteredBooks.addAll(libros);
            } else {
                for (Libro libro : libros) {
                    if (libro.getTitulo().toLowerCase().contains(query.toLowerCase()) ||
                            libro.getAutor().toLowerCase().contains(query.toLowerCase())) {
                        filteredBooks.add(libro);
                    }
                }
            }
            filtrarYActualizarListas(filteredBooks);
        });
    }

    private void signInWithGoogle() {
        authManager.signInWithGoogle(this, this);
    }

    @Override
    public void onAuthFailure(String errorMessage) {
        Log.e(TAG, "Error de autenticación: " + errorMessage);
        Toast.makeText(this, "Error al iniciar sesión: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private void signOutUser() {
        authManager.signOut(this, () -> {
            Log.d(TAG, "Cierre de sesión de Google (Credential Manager) completado.");
        });
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    private void bindViews() {
        rvLibrosActivos = findViewById(R.id.recyclerViewLibrosActivos);
        rvLibrosLeidos = findViewById(R.id.recyclerViewLibrosLeidos);
        etBusqueda = findViewById(R.id.etBusqueda);
        topAppBar = findViewById(R.id.topAppBar);
        headerLeidos = findViewById(R.id.headerLeidos);
        arrowLeidos = findViewById(R.id.arrowLeidos);
        dividerLeidos = findViewById(R.id.dividerLeidos);
        fabNuevo = findViewById(R.id.nuevo);
    }

    private void setupListeners() {
        topAppBar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ConfiguracionActivity.class));
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.btnCuenta) {
                if (currentUser != null) {
                    View anchorView = findViewById(R.id.btnCuenta);
                    showUserPopupMenu(anchorView);
                } else {
                    signInWithGoogle();
                }
                return true;
            } else if (itemId == R.id.btnCalendario) {
                startActivity(new Intent(MainActivity.this, CalendarioActivity.class));
                return true;
            } else if (itemId == R.id.btnEstadisticas) {
                startActivity(new Intent(MainActivity.this, EstadisticasActivity.class));
                return true;
            }
            return false;
        });

        fabNuevo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LibroActivity.class));
        });

        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadBooksFromRepository(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerViews() {
        LibroAdapter.OnItemClickListener listener = libro -> {
            Intent intent = new Intent(MainActivity.this, LibroActivity.class);
            intent.putExtra("id", libro.getId());
            intent.putExtra("titulo", libro.getTitulo());
            intent.putExtra("autor", libro.getAutor());
            intent.putExtra("notas", libro.getNotas());
            intent.putExtra("leido", libro.getLeido());
            intent.putExtra("pag_actual", libro.getPagActual());
            intent.putExtra("pag_totales", libro.getPagTotales());
            intent.putExtra("favorito", libro.getFavorito());
            startActivity(intent);
        };
        rvLibrosActivos.setLayoutManager(new LinearLayoutManager(this));
        adapterActivos = new LibroAdapter(new ArrayList<>(), this, listener);
        rvLibrosActivos.setAdapter(adapterActivos);
        rvLibrosLeidos.setLayoutManager(new LinearLayoutManager(this));
        adapterLeidos = new LibroAdapter(new ArrayList<>(), this, listener);
        rvLibrosLeidos.setAdapter(adapterLeidos);
    }

    private void filtrarYActualizarListas(List<Libro> libros) {
        List<Libro> librosActivos = new ArrayList<>();
        List<Libro> librosLeidos = new ArrayList<>();
        for (Libro libro : libros) {
            if ("leido".equals(libro.getEstado())) {
                librosLeidos.add(libro);
            } else {
                librosActivos.add(libro);
            }
        }
        adapterActivos.setLibros(librosActivos);
        adapterLeidos.setLibros(librosLeidos);

        boolean hayLeidos = !librosLeidos.isEmpty();
        dividerLeidos.setVisibility(hayLeidos ? View.VISIBLE : View.GONE);
        headerLeidos.setVisibility(hayLeidos ? View.VISIBLE : View.GONE);
        if (!hayLeidos) {
            rvLibrosLeidos.setVisibility(View.GONE);
            arrowLeidos.setRotation(0);
        }
    }

    private void showUserPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_cuenta, popup.getMenu());
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");
        popup.getMenu().findItem(R.id.menu_email).setTitle(userEmail);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                signOutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void setupHeaderLeidos() {
        headerLeidos.setOnClickListener(v -> {
            boolean isVisible = rvLibrosLeidos.getVisibility() == View.VISIBLE;
            rvLibrosLeidos.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            arrowLeidos.animate().rotation(isVisible ? 0 : 180).setDuration(300).start();
        });
    }

    private void setupFabInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(fabNuevo, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.bottomMargin = insets.bottom + (int) (16 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(mlp);
            return windowInsets;
        });
    }
    private void handleFirstLaunch() {
        boolean isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true);
        if (isFirstLaunch && currentUser == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Bienvenido a ReadLog")
                    .setMessage("Guarda tu progreso en la nube y accede desde cualquier dispositivo.")
                    .setPositiveButton("Iniciar sesión con Google", (dialog, which) -> signInWithGoogle())
                    .setNegativeButton("Continuar sin cuenta", null)
                    .setOnDismissListener(dialog -> sharedPreferences.edit().putBoolean("is_first_launch", false).apply())
                    .setCancelable(false)
                    .show();
        }
    }
}
