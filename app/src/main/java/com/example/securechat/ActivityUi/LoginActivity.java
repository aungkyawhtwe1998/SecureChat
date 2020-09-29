package com.example.securechat.ActivityUi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.securechat.Crypto.Cryptor;
import com.example.securechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button btn_login;
    FirebaseAuth auth;
    TextView txtreset;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.l_email);
        password = findViewById(R.id.l_password);
        btn_login = findViewById(R.id.btn_login);
        txtreset = findViewById(R.id.forgot_password);

        prefs = getSharedPreferences(StartActivity.SHAREDPREF, Context.MODE_PRIVATE);
        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(LoginActivity.this, "All fields are requred", Toast.LENGTH_SHORT).show();

                } else {
                    final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                    pd.setMessage("Logging in...");
                    pd.show();
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        pd.dismiss();
                                        if(auth.getCurrentUser().isEmailVerified()){
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                        }else {
                                            pd.dismiss();
                                            Toast.makeText(LoginActivity.this, "Please,Verify your Email address!", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        txtreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showHash();
                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
            }
        });
    }
    /*private void showHash() {
        String iv = prefs.getString("keyIv", "null");
        String encrypted = prefs.getString("encryptedKey", "");

        try {
            Cryptor cryptor = new Cryptor();
            cryptor.initKeyStore();
            String decrypted = cryptor.decryptText(encrypted, iv);

            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Hash")
                    .setMessage("Encrypted: "+ encrypted +"\nDecrypted: "+ decrypted)
                    .setCancelable(false)
                    .setPositiveButton("Close",null)
                    .show();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
