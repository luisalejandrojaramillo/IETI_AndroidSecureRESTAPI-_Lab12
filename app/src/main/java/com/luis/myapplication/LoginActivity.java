package com.luis.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.luis.myapplication.model.LoginWrapper;
import com.luis.myapplication.model.Token;
import com.luis.myapplication.service.AuthService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private EditText textEmail;
    private EditText textPassword;
    private Button buttonLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = (Button) findViewById(R.id.button);
        textEmail = (EditText) findViewById(R.id.editTextTextEmailAddress);
        textPassword = (EditText) findViewById(R.id.editTextTextPassword);

    }

    public void onLoginClick(View view){
        String email = textEmail.getText().toString();
        String pass = textPassword.getText().toString();

        if(email.isEmpty()){
            textEmail.setError("Ingrese email valido");
        }
        if(pass.isEmpty()){
            textPassword.setError("Ingrese contraseña valida");
        }else{
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http:/10.0.2.2:8080") //localhost for emulator
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            AuthService authService = retrofit.create(AuthService.class);
            executorService.execute(authenticate(authService,email,pass));
        }
    }

    private Runnable authenticate(AuthService authService, String email, String password) {
        return new Runnable() {
            @Override
            public void run() {
                try
                {
                    Response<Token> response =
                            authService.login( new LoginWrapper( "test@mail.com", "password" ) ).execute();
                    Token token = response.body();

                    SharedPreferences sharedPref =
                            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("TOKEN_KEY",token.getToken());
                    editor.commit();
                    if (token!=null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        });
                        finish();
                    }
                    else{
                        textEmail.setError("Verify Your email");
                        textPassword.setError("Verify Your Password");
                    }

                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        };

    }
}