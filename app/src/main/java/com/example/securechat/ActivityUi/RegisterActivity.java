package com.example.securechat.ActivityUi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.securechat.Crypto.ENCUtils;
import com.example.securechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStore;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.CipherOutputStream;

public class RegisterActivity extends AppCompatActivity {
    MaterialEditText username, email, password;
    public static KeyStore keyStore;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    public static SharedPreferences preferences;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    BCECPublicKey publicKey;
    CipherOutputStream cipherOutputStream;
    KeyPair kp;
    public static BCECPrivateKey privateKey;
    String myPubKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));

        //crypto testing



        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btnreg);

        auth = FirebaseAuth.getInstance();
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    password.setError("too short");
                    Toast.makeText(RegisterActivity.this, "passwords must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }else {
                    register(txt_username, txt_email, txt_password);

                    //Toast.makeText(RegisterActivity.this, "Sorry, Invalid email address!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register(final String username, final String email, String password) {

        final ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setMessage("Singing in ...");
        pd.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        try {
                                            kp=ENCUtils.geneateECKey(getApplicationContext());
                                            publicKey= (BCECPublicKey) kp.getPublic();
                                            privateKey=(BCECPrivateKey) kp.getPrivate();
                                            String privateKeyString =Base64.encodeToString(privateKey.getEncoded(),Base64.DEFAULT);
                                            Log.d("Register", "MyPrivateKeyString: "+privateKeyString);
                                            myPubKey=Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT);
                                            Log.d("Register", "MyPubKeyString: "+myPubKey);

                                            preferences = getApplicationContext().getSharedPreferences("ECKeyPair",MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("private_key", privateKeyString);
                                            editor.putString("public_key",myPubKey);
                                            editor.commit();
                                            Log.d("Register", "PreferencesPubKeyString:"+publicKey);
                                            Log.d("Register", "PreferencesPrvKeyString:"+privateKey);
                                            //byte[] privateKeyBytes = privateKey.getD().toByteArray();
                                        } catch (NoSuchProviderException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (InvalidAlgorithmParameterException e) {
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(RegisterActivity.this,"Please Check your email for verification!",Toast.LENGTH_LONG).show();
                                        pd.dismiss();
                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        assert firebaseUser != null;
                                        String userid = firebaseUser.getUid();
                                        //Log.d("ENCUtils", "generateKey:Public key: "+publicKey);
                                        reference = FirebaseDatabase.getInstance().getReference("User").child(userid);
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("email", email);
                                        hashMap.put("id", userid);
                                        hashMap.put("imageURl", "default");
                                        hashMap.put("search", username.toLowerCase());
                                        hashMap.put("status", "offline");
                                        hashMap.put("username", username);
                                        hashMap.put("publicKey", myPubKey);
                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }else {
                                        Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();

                                    }

                                }
                            });
//                            Cryptor cryptor = new Cryptor();
//                            try {
//                                cryptor.setIv();
//                                prefs.edit().putString("encryptedKey", cryptor.encryptText(email + password)).apply();
//                                prefs.edit().putString("keyIv", cryptor.getIv_string()).apply();
//                            } catch (NoSuchPaddingException |NoSuchAlgorithmException |NoSuchProviderException |InvalidAlgorithmParameterException | InvalidKeyException e) {
//                                e.printStackTrace();
//                            }
                        } else {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    public boolean checkEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
//
//    private boolean validate(String emailStr, String password, String repeatPassword) {
//        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
//        return password.length() > 6 && repeatPassword.equals(password) && matcher.find();
//    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

}
